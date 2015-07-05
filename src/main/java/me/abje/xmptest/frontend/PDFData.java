package me.abje.xmptest.frontend;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import me.abje.xmptest.*;
import me.abje.xmptest.frontend.opt.CommandParser;
import me.abje.xmptest.frontend.opt.Options;
import me.abje.xmptest.frontend.opt.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PDFData {
    public Table read(DataStorage storage, File pdfFile) throws IOException, XMPException {
        PDDocument doc = PDDocument.load(pdfFile);
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        return storage.read(doc, xmp);
    }

    public void write(DataStorage storage, File sourceFile, File pdfFile) throws IOException, XMPException {
        PDDocument doc = PDDocument.load(pdfFile);
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        storage.write(doc, xmp, Table.fromCSV(new FileReader(sourceFile)));
    }

    public static void main(String[] argsArray) throws IOException, XMPException {
        CommandParser parser = new CommandParser();
        parser.option("help")
                .shortArg("h");
        parser.option("overwrite")
                .shortArg("O");

        Options options;
        try {
            options = parser.parse(argsArray);
        } catch (ParseException e) {
            requireThat(false, e.getMessage());
            options = new Options(null, null);
        }

        String[] args = options.getArgs();
        if (args.length == 0 || options.is("help")) {
            printHelpAndExit();
        } else if ((args.length == 3 || args.length == 4) && args[0].equals("read")) {
            String typeName = args[1];
            String pdfFileName = args[2];

            DataStorage storage = getStorage(typeName);
            File pdfFile = new File(pdfFileName);
            requireThat(pdfFile.exists(), "PDF file doesn't exist.");

            Table table = new PDFData().read(storage, pdfFile);
            if (args.length == 4) {
                File outFile = new File(args[3]);
                requireThat(options.is("overwrite") || !outFile.exists(), "Output file already exists.");

                FileWriter writer = new FileWriter(outFile);
                writer.write(table.toCSV());
                writer.close();
            } else {
                System.out.print(table.toCSV());
            }
        } else if (args.length == 4 && args[0].equals("write")) {
            String typeName = args[1];
            String sourceFileName = args[2];
            String pdfFileName = args[3];

            DataStorage storage = getStorage(typeName);
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
                "    read  <storage type> <pdf file> [output file]\n" +
                "    write <storage type> <source file> <pdf file>\n\n" +

                "Options:\n" +
                "    -h, --help:      print this help message and exit\n" +
                "    -O, --overwrite: overwrite the output file if it exists\n\n" +

                "Storage Types:\n" +
                "    annotations, ann, an: Annotation-based\n" +
                "    attachments, att, at: Attachment-based\n" +
                "    forms, form, f:       Form-based\n" +
                "    xmp, meta, x, m:      Metadata-based");
        System.exit(1);
    }

    private static DataStorage getStorage(String typeName) {
        switch (typeName) {
            case "annotations":
            case "ann":
            case "an":
                return new AnnotationDataStorage();
            case "attachments":
            case "att":
            case "at":
                return new AttachmentDataStorage();
            case "forms":
            case "form":
            case "f":
                return new FormDataStorage();
            case "xmp":
            case "meta":
            case "x":
            case "m":
                return new XMPDataStorage();
            default:
                printHelpAndExit();
                return null; // Never reached, since printHelpAndExit() exits.
        }
    }
}
