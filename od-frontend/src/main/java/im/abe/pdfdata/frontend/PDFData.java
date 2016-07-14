package im.abe.pdfdata.frontend;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.beust.jcommander.*;
import com.google.common.io.Files;
import im.abe.pdfdata.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * The PDF reader/writer command-line tool.
 */
public class PDFData {
    @Parameter(names = {"-h", "--help"}, description = "Print help message and exit", help = true)
    private boolean help;

    @Parameters(separators = "=", commandDescription = "Read data from a PDF file")
    private static class ReadCommand {
        @Parameter(names = "--format", description = "Output format (values: JSON, CSV, RDF/XML, TURTLE)")
        private String outputFormat = "CSV";

        @Parameter(description = "Input PDF file", required = true)
        private List<String> inputPaths;

        @Parameter(names = "-o", description = "Output file")
        private String outputPath;

        public Format getFormat() {
            switch (outputFormat.toUpperCase()) {
                case "JSON":
                    return Format.JSON;
                case "RDF":
                case "RDF_XML":
                case "RDF/XML":
                case "RDFXML":
                    return Format.RDF_XML;
                case "TURTLE":
                    return Format.TURTLE;
                default:
                    return Format.CSV;
            }
        }
    }

    @Parameters(separators = "=", commandDescription = "Write data to a PDF file")
    private static class WriteCommand {
        @Parameter(description = "Source and destination files", required = true,
                validateValueWith = PathValidator.class)
        private List<String> paths;

        private static class PathValidator implements IValueValidator<List<String>> {
            @Override
            public void validate(String name, List<String> value) throws ParameterException {
                if (value.size() != 2)
                    throw new ParameterException("Two paths must be provided.");
            }
        }
    }

    public List<Table> read(DataStorage storage, PDDocument doc) throws IOException, XMPException {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        return storage.read(doc, xmp);
    }

    public List<Table> readAll(File pdfFile) throws IOException, XMPException {
        PDDocument doc = PDDocument.load(pdfFile);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();
        return tables;
    }

    public void write(WritableDataStorage storage, File sourceFile, File pdfFile) throws IOException, XMPException {
        PDDocument doc = PDDocument.load(pdfFile);
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        storage.write(doc, xmp, Table.fromCSV("File", new FileReader(sourceFile)), Destination.document());
        doc.save(pdfFile);
    }

    public static void main(String[] args) throws IOException, XMPException {
        PDFData pdfData = new PDFData();
        ReadCommand read = new ReadCommand();
        WriteCommand write = new WriteCommand();

        JCommander jc = new JCommander(pdfData);
        jc.addCommand("read", read);
        jc.addCommand("write", write);
        jc.parse(args);

        if (pdfData.help) {
            printHelpAndExit(jc);
        } else if (Objects.equals(jc.getParsedCommand(), "read")) {
            for (String path : read.inputPaths) {
                File pdfFile = new File(path);
                requireThat(pdfFile.exists(), "PDF file doesn't exist.", jc);

                List<Table> tables = pdfData.readAll(pdfFile);

                String baseFile = read.outputPath;
                if (baseFile != null) {
                    String name = Files.getNameWithoutExtension(baseFile);
                    String extension = Files.getFileExtension(baseFile);
                    for (int i = 0; i < tables.size(); i++) {
                        Table table = tables.get(i);
                        File outFile = new File(new File(baseFile).getParent(),
                                name + (tables.size() > 1 ? ("_" + i + ".") : ".") + extension);
                        if (outFile.exists()) {
                            try (Scanner scanner = new Scanner(System.in)) {
                                System.out.print("Output file already exists. Overwrite? (y/n) ");
                                String response = scanner.next(Pattern.compile("y|n", Pattern.CASE_INSENSITIVE));

                                if (!response.equalsIgnoreCase("y")) {
                                    requireThat(false, "Output file `" + outFile.getPath() + "` already exists.", jc);
                                }
                            }
                        }

                        FileWriter writer = new FileWriter(outFile);
                        writer.write(table.to(read.getFormat()));
                        writer.close();
                    }
                } else {
                    for (Table table : tables) {
                        System.out.println(table.getName() + ":");
                        System.out.println(table.to(read.getFormat()));
                    }
                }
            }
        } else if (Objects.equals(jc.getParsedCommand(), "write")) {
            WritableDataStorage storage = new AttachmentDataStorage();
            File sourceFile = new File(write.paths.get(0));
            File pdfFile = new File(write.paths.get(1));

            requireThat(sourceFile.exists(), "Source file doesn't exist.", jc);
            requireThat(pdfFile.exists(), "PDF file doesn't exist.", jc);

            new PDFData().write(storage, sourceFile, pdfFile);
        } else {
            printHelpAndExit(jc);
        }
    }

    private static void requireThat(boolean condition, String error, JCommander jc) {
        if (!condition) {
            System.err.println("Error: " + error + "\n");
            printHelpAndExit(jc);
        }
    }

    private static void printHelpAndExit(JCommander jc) {
        // I'm really not sure of how this should be formatted, but it's fine for now.

        System.out.println("Usage: pdfdata\n" +
                "    read  <pdf file> [output file]\n" +
                "    write <source file> -o <pdf file>\n\n" +

                "Options:\n" +
                "    -h, --help: print this help message and exit");
        System.exit(1);
    }
}
