package me.abje.xmptest;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotationDataStorageTest {
    private AnnotationDataStorage dataStorage = new AnnotationDataStorage();
    private PDDocument doc;
    private XMPMeta xmp;

    @Before
    public void setUp() throws Exception {
        doc = PDDocument.load(getClass().getResourceAsStream("/docs/4.pdf"));

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
        assertThat(table.getCells(), equalTo(Arrays.asList(
                Collections.singletonList(new Table.Cell("Tuesday, 16 November 2010: 4")),
                Collections.singletonList(new Table.Cell("Wednesday, 17 November 2010: 6")))));
    }
}
