package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An attachment-based data storage method. Data is stored as an attachment in the PDF file, and linked using XMP.
 */
public class AttachmentDataStorage extends DataStorage<byte[]> {
    public static final String PROP_FILENAME = "Filename";

    @Override
    public byte[] read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        if (xmp.getProperty(SCHEMA_OD, PROP_FILENAME) != null) {
            Map<String, COSObjectable> embeddedFiles = doc.getDocumentCatalog().getNames().
                    getEmbeddedFiles().getNames();

            String key = xmp.getPropertyString(SCHEMA_OD, PROP_FILENAME);

            PDComplexFileSpecification complexFile = (PDComplexFileSpecification) embeddedFiles.get(key);
            return complexFile.getEmbeddedFile().getByteArray();
        } else {
            return null;
        }
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, byte[] data) throws XMPException, IOException {
        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile(UUID.randomUUID().toString());

        PDEmbeddedFile ef = new PDEmbeddedFile(doc, new ByteArrayInputStream(data));
        ef.setSize(data.length);
        ef.setCreationDate(new GregorianCalendar());
        fs.setEmbeddedFile(ef);

        Map<String, COSObjectable> efMap = new HashMap<>();
        efMap.put(fs.getFile(), fs);
        efTree.setNames(efMap);

        PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
        names.setEmbeddedFiles(efTree);
        doc.getDocumentCatalog().setNames(names);

        xmp.setProperty(SCHEMA_OD, PROP_FILENAME, fs.getFile());
    }
}
