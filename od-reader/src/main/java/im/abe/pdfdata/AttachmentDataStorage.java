package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.google.common.io.Files;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * An attachment-based data storage method. Data is stored as an attachment in the PDF file, and linked using XMP.
 */
public class AttachmentDataStorage extends WritableDataStorage {
    public static final String STORED_DATA = "Stored Data";

    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<Table> tables = new ArrayList<>();
        PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
        if (names == null)
            return tables;
        PDEmbeddedFilesNameTreeNode node = names.getEmbeddedFiles();
        if (node == null)
            return tables;
        Map<String, PDComplexFileSpecification> files = node.getNames();
        for (Map.Entry<String, PDComplexFileSpecification> entry : files.entrySet()) {
            if (!entry.getKey().startsWith("#"))
                continue;
            PDComplexFileSpecification complexFile = entry.getValue();
            tables.add(Table.fromCSV(Files.getNameWithoutExtension(complexFile.getFilename()),
                    new StringReader(complexFile.getEmbeddedFile().getCOSObject().toTextString().trim())));
        }

        for (PDPage page : doc.getDocumentCatalog().getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationFileAttachment) {
                    PDAnnotationFileAttachment fileAttachment = (PDAnnotationFileAttachment) annotation;
                    if (fileAttachment.getSubject().equals(STORED_DATA)) {
                        PDComplexFileSpecification complexFile = (PDComplexFileSpecification) fileAttachment.getFile();
                        tables.add(Table.fromCSV(Files.getNameWithoutExtension(complexFile.getFilename()),
                                new StringReader(complexFile.getEmbeddedFile().getCOSObject()
                                        .toTextString().trim())));
                    }
                }
            }
        }

        return tables;
    }

    public Table find(PDDocument doc, String fileName, String fragment) throws XMPException, IOException {
        PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
        if (names == null)
            return null;
        PDEmbeddedFilesNameTreeNode node = names.getEmbeddedFiles();
        if (node == null)
            return null;
        Map<String, PDComplexFileSpecification> files = node.getNames();
        for (Map.Entry<String, PDComplexFileSpecification> entry : files.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(fragment)) {
                PDComplexFileSpecification complexFile = entry.getValue();
                return Table.fromCSV(Files.getNameWithoutExtension(complexFile.getFilename()),
                        new StringReader(complexFile.getEmbeddedFile().getCOSObject().toTextString().trim()));
            }
        }

        for (PDPage page : doc.getDocumentCatalog().getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationFileAttachment) {
                    PDAnnotationFileAttachment fileAttachment = (PDAnnotationFileAttachment) annotation;
                    if (fileAttachment.getSubject().equals(STORED_DATA)) {
                        PDComplexFileSpecification complexFile = (PDComplexFileSpecification) fileAttachment.getFile();
                        if (complexFile.getFilename().equals(fileName)) {
                            return Table.fromCSV(Files.getNameWithoutExtension(complexFile.getFilename()),
                                    new StringReader(complexFile.getEmbeddedFile().getCOSObject()
                                            .toTextString().trim()));
                        }
                    }
                }
            }
        }

        return null;
    }

    public List<FilePreview> preview(PDDocument doc) throws XMPException, IOException {
        List<FilePreview> results = new ArrayList<>();

        PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
        if (names == null)
            return results;
        PDEmbeddedFilesNameTreeNode node = names.getEmbeddedFiles();
        if (node == null)
            return results;
        Map<String, PDComplexFileSpecification> files = node.getNames();

        if (files != null) {
            for (Map.Entry<String, PDComplexFileSpecification> entry : files.entrySet()) {
                if (!entry.getKey().startsWith("#"))
                    continue;
                PDComplexFileSpecification complexFile = entry.getValue();
                results.add(new FilePreview(complexFile.getFilename(), complexFile.getFileDescription()));
            }
        }

        for (PDPage page : doc.getDocumentCatalog().getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationFileAttachment) {
                    PDAnnotationFileAttachment fileAttachment = (PDAnnotationFileAttachment) annotation;
                    if (fileAttachment.getSubject().equals(STORED_DATA)) {
                        PDComplexFileSpecification complexFile = (PDComplexFileSpecification) fileAttachment.getFile();
                        results.add(new FilePreview(complexFile.getFilename(), complexFile.getFileDescription()));
                    }
                }
            }
        }

        return results;
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table table, Destination destination) throws XMPException, IOException {
        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile(table.getName() + ".csv");
        fs.setFileDescription(destination.toFragmentIdentifier());

        byte[] data = table.to(Format.CSV).getBytes("UTF-8");
        PDEmbeddedFile ef = new PDEmbeddedFile(doc, new ByteArrayInputStream(data));
        ef.setSize(data.length);
        ef.setCreationDate(new GregorianCalendar());
        fs.setEmbeddedFile(ef);

        destination.writeAttachment(doc, fs);
    }

    /**
     * A simple wrapper class for file location information.
     * Holds a file name and its location (#fragment) in the file.
     */
    public static class FilePreview {
        private String fileName;
        private String fragment;

        public FilePreview(String fileName, String fragment) {
            this.fileName = fileName;
            this.fragment = fragment;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFragment() {
            return fragment;
        }
    }
}
