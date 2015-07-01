package me.abje.xmptest;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        byte[] docBytes = dataStorage.read(doc, xmp);
        BufferedReader docBytesReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(docBytes)));
        List<String> docLines = CharStreams.readLines(docBytesReader);
        List<String> sourceLines =
                CharStreams.readLines(new InputStreamReader(getClass().getResourceAsStream("/data/2.csv")));

        assertThat(docLines, equalTo(sourceLines));
    }

    @Test
    public void testWrite() throws Exception {
        dataStorage.write(doc, xmp, ByteStreams.toByteArray(getClass().getResourceAsStream("/data/2.csv")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));
    }
}
