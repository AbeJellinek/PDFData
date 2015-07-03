package me.abje.xmptest.frontend;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import me.abje.xmptest.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
        List<String> args = new ArrayList<>(Arrays.asList(argsArray));
        boolean isHelp = args.size() == 1 && (args.get(0).equals("-h") || args.get(0).equals("--help"));
        boolean isOverwrite = false;

        for (ListIterator<String> iterator = args.listIterator(); iterator.hasNext(); ) {
            String arg = iterator.next();
            if (arg.startsWith("-")) {
                iterator.remove();
                String option = arg.startsWith("--") ? arg.substring(2) : arg.substring(1);
                if (option.equals("O") || option.equals("overwrite")) {
                    isOverwrite = true;
                } else {
                    requireThat(false, "Invalid option.");
                }
            }
        }

        if (args.size() == 0 || isHelp) {
            printHelpAndExit();
        } else if ((args.size() == 3 || args.size() == 4) && args.get(0).equals("read")) {
            String typeName = args.get(1);
            String pdfFileName = args.get(2);

            DataStorage storage = getStorage(typeName);
            File pdfFile = new File(pdfFileName);
            requireThat(pdfFile.exists(), "PDF file doesn't exist.");

            Table table = new PDFData().read(storage, pdfFile);
            if (args.size() == 4) {
                File outFile = new File(args.get(3));
                requireThat(isOverwrite || !outFile.exists(), "Output file already exists.");

                FileWriter writer = new FileWriter(outFile);
                writer.write(table.toCSV());
                writer.close();
            } else {
                System.out.print(table.toCSV());
            }
        } else if (args.size() == 4 && args.get(0).equals("write")) {
            String typeName = args.get(1);
            String sourceFileName = args.get(2);
            String pdfFileName = args.get(3);

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
            System.err.println("Error: " + error);
            printHelpAndExit();
        }
    }

    private static void printHelpAndExit() {
        // I'm really not sure of how this should be formatted, but it's fine for now.

        System.out.println("Usage: pdfdata\n" +
                "    read  <storage type> <pdf file> [output file]\n" +
                "    write <storage type> <source file> <pdf file>\n\n" +

                "Options:\n" +
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
