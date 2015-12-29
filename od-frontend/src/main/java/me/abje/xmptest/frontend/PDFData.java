package me.abje.xmptest.frontend;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.google.common.io.Files;
import me.abje.xmptest.*;
import me.abje.xmptest.frontend.opt.CommandParser;
import me.abje.xmptest.frontend.opt.Form;
import me.abje.xmptest.frontend.opt.Options;
import me.abje.xmptest.frontend.opt.ParseException;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The PDF reader/writer command-line tool.
 */
public class PDFData {
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

    public void write(WritableDataStorage storage, File sourceFile, File pdfFile) throws IOException, XMPException, COSVisitorException {
        PDDocument doc = PDDocument.load(pdfFile);
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        storage.write(doc, xmp, Table.fromCSV("File", new FileReader(sourceFile)), -1);
        doc.save(pdfFile);
    }

    public static void main(String[] argsArray) throws IOException, XMPException, COSVisitorException {
        CommandParser parser = new CommandParser();
        parser.option("help")
                .shortArg("h");
        parser.option("overwrite")
                .shortArg("O");
        parser.option("csv");

        parser.form("read")
                .arg("pdf file")
                .arg("output file").optional();

        parser.form("write")
                .arg("source file")
                .arg("pdf file");

        Options options;
        try {
            options = parser.parse(argsArray);
        } catch (ParseException e) {
            requireThat(false, e.getMessage());
            return;
        }

        Form form = options.getForm();

        if (form == null || options.is("help")) {
            printHelpAndExit();
        } else if (form.is("read")) {
            String pdfFileName = options.get("pdf file");

            File pdfFile = new File(pdfFileName);
            requireThat(pdfFile.exists(), "PDF file doesn't exist.");

            PDFData pdfData = new PDFData();
            List<Table> tables = pdfData.readAll(pdfFile);

            if (options.formHas("output file")) {
                String baseFile = options.get("output file");
                String name = Files.getNameWithoutExtension(baseFile);
                String extension = Files.getFileExtension(baseFile);
                for (int i = 0; i < tables.size(); i++) {
                    Table table = tables.get(i);
                    File outFile = new File(new File(baseFile).getParent(),
                            name + (tables.size() > 1 ? ("_" + i + ".") : ".") + extension);
                    requireThat(options.is("overwrite") || !outFile.exists(),
                            "Output file `" + outFile.getPath() + "` already exists.");

                    FileWriter writer = new FileWriter(outFile);
                    writer.write(options.is("csv") ? table.toCSV() : table.toJSON(false));
                    writer.close();
                }
            } else {
                for (Table table : tables) {
                    System.out.println(table.getName() + ":");
                    System.out.println(options.is("csv") ? table.toCSV() : table.toFormat("Turtle"));
                }
            }
        } else if (form.is("write")) {
            String sourceFileName = options.get("source file");
            String pdfFileName = options.get("pdf file");

            WritableDataStorage storage = new AttachmentDataStorage();
            File sourceFile = new File(sourceFileName);
            File pdfFile = new File(pdfFileName);

            requireThat(sourceFile.exists(), "Source file doesn't exist.");
            requireThat(pdfFile.exists(), "PDF file doesn't exist.");

            new PDFData().write(storage, sourceFile, pdfFile);
        } else {
            printHelpAndExit();
        }
    }

    private static void requireThat(boolean condition, String error) {
        if (!condition) {
            System.err.println("Error: " + error + "\n");
            printHelpAndExit();
        }
    }

    private static void printHelpAndExit() {
        // I'm really not sure of how this should be formatted, but it's fine for now.

        System.out.println("Usage: pdfdata\n" +
                "    read  <pdf file> [output file]\n" +
                "    write <source file> <pdf file>\n\n" +

                "Options:\n" +
                "    -h, --help:      print this help message and exit\n" +
                "    -O, --overwrite: overwrite the output file if it exists\n");
        System.exit(1);
    }
}
