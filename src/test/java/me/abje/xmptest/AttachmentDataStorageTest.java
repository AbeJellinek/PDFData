package me.abje.xmptest;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AttachmentDataStorageTest {
    private AttachmentDataStorage dataStorage = new AttachmentDataStorage();
    private PDDocument doc;
    private XMPMeta xmp;

    @Before
    public void setUp() throws Exception {
        doc = PDDocument.load(getClass().getResourceAsStream("/docs/2.pdf"));

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        if (catalog.getMetadata() == null)
            xmp = XMPMetaFactory.create();
        else
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
    }

    @Test
    public void testRead() throws Exception {
        Table docTable = dataStorage.read(doc, xmp);
        Table sourceTable = Table.fromCSV(new InputStreamReader(getClass().getResourceAsStream("/data/2.csv")));
        assertThat(docTable, equalTo(sourceTable));
    }

    @Test
    public void testWrite() throws Exception {
        dataStorage.write(doc, xmp, Table.fromCSV(new InputStreamReader(getClass().getResourceAsStream("/data/2.csv"))));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));
        doc.save(new File("src/test/resources/docs/2.pdf"));
    }
}
