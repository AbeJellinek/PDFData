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

    public static void main(String[] args) throws IOException, XMPException {
        boolean isHelp = args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"));
        if (args.length == 0 || isHelp) {
            printHelpAndExit();
        } else if ((args.length == 3 || args.length == 4) && args[0].equals("read")) {
            String typeName = args[1];
            String pdfFileName = args[2];

            DataStorage storage = getStorage(typeName);
            File pdfFile = new File(pdfFileName);

            if (!pdfFile.exists()) {
                System.err.println("Error: PDF file doesn't exist.");
                printHelpAndExit();
            }

            Table table = new PDFData().read(storage, pdfFile);
            if (args.length == 4) {
                File outFile = new File(args[3]);

                if (outFile.exists()) {
                    System.err.println("Error: Output file already exists.");
                    printHelpAndExit();
                }

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

            if (!sourceFile.exists()) {
                System.err.println("Error: PDF file doesn't exist.");
                printHelpAndExit();
            }

            if (!pdfFile.exists()) {
                System.err.println("Error: PDF file doesn't exist.");
                printHelpAndExit();
            }

            new PDFData().write(storage, sourceFile, pdfFile);
        } else {
            printHelpAndExit();
        }
    }

    private static void printHelpAndExit() {
        System.out.println("Usage: pdfdata\n" +
                "    read  <type> <file> [outfile]\n" +
                "    write <type> <source> <file>");
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
