package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
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
import java.util.*;

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
            if (!entry.getKey().startsWith("META_"))
                continue;
            PDComplexFileSpecification complexFile = entry.getValue();
            tables.add(Table.fromCSV("Attachment", new StringReader(complexFile.getEmbeddedFile().getCOSObject()
                    .toTextString().trim())));
        }

        int pageNum = 1;
        for (PDPage page : doc.getDocumentCatalog().getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationFileAttachment) {
                    PDAnnotationFileAttachment fileAttachment = (PDAnnotationFileAttachment) annotation;
                    if (fileAttachment.getSubject().equals(STORED_DATA)) {
                        PDComplexFileSpecification complexFile = (PDComplexFileSpecification) fileAttachment.getFile();
                        tables.add(Table.fromCSV("Page " + pageNum + " Attachment",
                                new StringReader(complexFile.getEmbeddedFile().getCOSObject()
                                        .toTextString().trim())));
                    }
                }
            }

            pageNum++;
        }

        return tables;
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table table, Destination destination) throws XMPException, IOException {
        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile(table.getName() + ".csv");
        fs.setFileDescription("META_" + UUID.randomUUID().toString());

        byte[] data = table.toCSV().getBytes("UTF-8");
        PDEmbeddedFile ef = new PDEmbeddedFile(doc, new ByteArrayInputStream(data));
        ef.setSize(data.length);
        ef.setCreationDate(new GregorianCalendar());
        fs.setEmbeddedFile(ef);

        destination.writeAttachment(doc, fs);
    }
}
