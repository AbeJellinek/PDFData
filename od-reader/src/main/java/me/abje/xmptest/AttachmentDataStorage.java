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
import java.io.StringReader;
import java.util.*;

/**
 * An attachment-based data storage method. Data is stored as an attachment in the PDF file, and linked using XMP.
 */
public class AttachmentDataStorage extends DataStorage {
    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<Table> tables = new ArrayList<>();
        Map<String, COSObjectable> embeddedFiles = doc.getDocumentCatalog().getNames().
                getEmbeddedFiles().getNames();
        for (Map.Entry<String, COSObjectable> entry : embeddedFiles.entrySet()) {
            if (!entry.getKey().startsWith("META_"))
                continue;
            PDComplexFileSpecification complexFile = (PDComplexFileSpecification) entry.getValue();
            tables.add(Table.fromCSV(new StringReader(complexFile.getEmbeddedFile().getInputStreamAsString().trim())));
        }
        return tables;
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table table) throws XMPException, IOException {
        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setFile("META_" + UUID.randomUUID().toString());

        byte[] data = table.toCSV().getBytes("UTF-8");

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
    }
}
