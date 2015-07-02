package me.abje.xmptest;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormDataStorageTest {
    private FormDataStorage dataStorage = new FormDataStorage();
    private PDDocument doc;
    private XMPMeta xmp;

    @Before
    public void setUp() throws Exception {
        doc = PDDocument.load(getClass().getResourceAsStream("/docs/3.pdf"));

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        if (catalog.getMetadata() == null)
            xmp = XMPMetaFactory.create();
        else
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
    }

    @After
    public void tearDown() throws Exception {
        doc.close();
    }

    @Test
    public void testRead() throws Exception {
        Table table = dataStorage.read(doc, xmp);
        assertThat(table.getCells(), equalTo(Collections.singletonList(
                Arrays.asList(new Table.Cell("Saturday, 13 November 2010"), new Table.Cell("2")))));
    }

    @Test
    public void testWrite() throws Exception {
        dataStorage.write(doc, xmp, new Table(Arrays.asList("Day", "Lowest Temperature (C)"),
                Collections.singletonList(
                        Arrays.asList(new Table.Cell("Saturday, 13 November 2010"), new Table.Cell("2"))),
                2, 1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));
    }
}
