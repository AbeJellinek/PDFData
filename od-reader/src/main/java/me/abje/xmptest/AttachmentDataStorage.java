package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * An attachment-based data storage method. Data is stored as an attachment in the PDF file, and linked using XMP.
 */
public class AttachmentDataStorage extends WritableDataStorage {
    private static final String STORED_DATA = "Stored Data";

    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<Table> tables = new ArrayList<>();
        PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
        if (names == null)
            return tables;
        PDEmbeddedFilesNameTreeNode node = names.getEmbeddedFiles();
        if (node == null)
            return tables;
        Map<String, COSObjectable> files = node.getNames();
        for (Map.Entry<String, COSObjectable> entry : files.entrySet()) {
            if (!entry.getKey().startsWith("META_"))
                continue;
            PDComplexFileSpecification complexFile = (PDComplexFileSpecification) entry.getValue();
            tables.add(Table.fromCSV("Attachment", new StringReader(complexFile.getEmbeddedFile().getInputStreamAsString().trim())));
        }

        int pageNum = 1;
        //noinspection unchecked
        for (PDPage page : (List<PDPage>) doc.getDocumentCatalog().getAllPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationFileAttachment) {
                    PDAnnotationFileAttachment fileAttachment = (PDAnnotationFileAttachment) annotation;
                    if (fileAttachment.getSubject().equals(STORED_DATA)) {
                        PDComplexFileSpecification complexFile = (PDComplexFileSpecification) fileAttachment.getFile();
                        tables.add(Table.fromCSV("Page " + pageNum + " Attachment",
                                new StringReader(complexFile.getEmbeddedFile().getInputStreamAsString().trim())));
                    }
                }
            }

            pageNum++;
        }

        return tables;
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table table, int page) throws XMPException, IOException {
        if (page != -1) { // if page is provided
            PDComplexFileSpecification fs = new PDComplexFileSpecification();
            fs.setFile(table.getName() + ".csv");
            fs.setFileDescription("META_" + UUID.randomUUID().toString());

            byte[] data = table.toCSV().getBytes("UTF-8");

            PDEmbeddedFile ef = new PDEmbeddedFile(doc, new ByteArrayInputStream(data));
            ef.setSize(data.length);
            ef.setCreationDate(new GregorianCalendar());
            fs.setEmbeddedFile(ef);

            PDPage pdPage = (PDPage) doc.getDocumentCatalog().getAllPages().get(page);
            PDAnnotationFileAttachment annotation = new PDAnnotationFileAttachment();
            annotation.setFile(fs);
            annotation.setPage(pdPage);
            annotation.setAttachementName(PDAnnotationFileAttachment.ATTACHMENT_NAME_PAPERCLIP);
            annotation.setSubject(STORED_DATA);

            PDRectangle rect = new PDRectangle();
            rect.setLowerLeftX(5);
            rect.setLowerLeftY(5);
            rect.setUpperRightX(15);
            rect.setUpperRightY(25);
            annotation.setRectangle(rect);

            List<PDAnnotation> annotations = pdPage.getAnnotations();
            annotations.add(annotation);
            pdPage.setAnnotations(annotations);
        } else {
            PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
            if (names == null)
                names = new PDDocumentNameDictionary(doc.getDocumentCatalog());

            PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
            if (efTree == null)
                efTree = new PDEmbeddedFilesNameTreeNode();

            PDComplexFileSpecification fs = new PDComplexFileSpecification();
            fs.setFile(table.getName() + ".csv");
            fs.setFileDescription("META_" + UUID.randomUUID().toString());

            byte[] data = table.toCSV().getBytes("UTF-8");

            PDEmbeddedFile ef = new PDEmbeddedFile(doc, new ByteArrayInputStream(data));
            ef.setSize(data.length);
            ef.setCreationDate(new GregorianCalendar());
            fs.setEmbeddedFile(ef);

            Map<String, COSObjectable> efMap = intoMap(efTree.getNames());
            efMap.put(fs.getFileDescription(), fs);
            efTree.setNames(efMap);

            names.setEmbeddedFiles(efTree);
            doc.getDocumentCatalog().setNames(names);
        }
    }

    private <K, V> Map<K, V> intoMap(Map<K, V> firstMap) {
        if (firstMap == null) {
            return new HashMap<>();
        } else {
            return new HashMap<>(firstMap);
        }
    }
}
