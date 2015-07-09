package me.abje.xmptest.frontend;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import me.abje.xmptest.*;
import me.abje.xmptest.frontend.opt.*;
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

        Choice<DataStorage> dataStorageChoice = Choice.<DataStorage>builder()
                .option(new AnnotationDataStorage(), "annotations", "ann", "an")
                .option(new AttachmentDataStorage(), "attachments", "att", "at")
                .option(new FormDataStorage(), "forms", "form", "f")
                .option(new XMPDataStorage(), "xmp", "meta", "x", "m")
                .build();

        parser.form("read")
                .arg("storage type").choice(dataStorageChoice)
                .arg("pdf file")
                .arg("output file").optional();

        parser.form("write")
                .arg("storage type").choice(dataStorageChoice)
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

            DataStorage storage = dataStorageChoice.get(options);
            File pdfFile = new File(pdfFileName);
            requireThat(pdfFile.exists(), "PDF file doesn't exist.");

            Table table = new PDFData().read(storage, pdfFile);
            if (options.formHas("output file")) {
                File outFile = new File(options.get("output file"));
                requireThat(options.is("overwrite") || !outFile.exists(), "Output file already exists.");

                FileWriter writer = new FileWriter(outFile);
                writer.write(table.toCSV());
                writer.close();
            } else {
                System.out.print(table.toCSV());
            }
        } else if (form.is("write")) {
            String sourceFileName = options.get("source file");
            String pdfFileName = options.get("pdf file");

            DataStorage storage = dataStorageChoice.get(options);
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
}
