package me.abje.xmptest;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class XMPDataStorageTest {
    private XMPDataStorage dataStorage = new XMPDataStorage();
    private PDDocument doc;
    private XMPMeta xmp;

    @Before
    public void setUp() throws Exception {
        doc = PDDocument.load(getClass().getResourceAsStream("/docs/1.pdf"));

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        if (catalog.getMetadata() == null)
            xmp = XMPMetaFactory.create();
        else
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
    }

    @Test
    public void testRead() throws Exception {
        Table table = dataStorage.read(doc, xmp);
        assertThat(table.getCells(), equalTo(Arrays.asList(
                Arrays.asList(new Table.Cell("Saturday, 13 November 2010"), new Table.Cell("2")),
                Arrays.asList(new Table.Cell("Sunday, 14 November 2010"), new Table.Cell("4")),
                Arrays.asList(new Table.Cell("Monday, 15 November 2010"), new Table.Cell("7")))));
    }

    @Test
    public void testWrite() throws Exception {
        dataStorage.write(doc, xmp, new Table(Arrays.asList("Temperature forecast for Galway, Ireland", ""),
                Arrays.asList(
                        Arrays.asList(new Table.Cell("Saturday, 13 November 2010"), new Table.Cell("2")),
                        Arrays.asList(new Table.Cell("Sunday, 14 November 2010"), new Table.Cell("4")),
                        Arrays.asList(new Table.Cell("Monday, 15 November 2010"), new Table.Cell("7"))),
                2, 3));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));

        assertThat(xmp.getPropertyInteger(DataStorage.SCHEMA_OD, XMPDataStorage.PROP_ROW_SIZE), equalTo(2));
        assertThat(xmp.getArray(DataStorage.SCHEMA_OD, XMPDataStorage.PROP_DATA).toString(),
                equalTo("[Saturday, 13 November 2010, 2, Sunday, 14 November 2010, 4, Monday, 15 November 2010, 7]"));
    }
}
