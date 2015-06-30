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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class XMPDataStorageTest {
    private XMPDataStorage dataStorage = new XMPDataStorage();
    private PDDocument doc;
    private XMPMeta xmp;

    @Before
    public void setUp() throws Exception {
        doc = PDDocument.load(new FileInputStream("test.pdf"));

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
    }

    @Test
    public void testRead() throws Exception {
        List<List<String>> table = dataStorage.read(doc, xmp);
        assertThat(table, equalTo(Arrays.asList(
                Arrays.asList("Saturday, 13 November 2010", "2"),
                Arrays.asList("Sunday, 14 November 2010", "4"),
                Arrays.asList("Monday, 15 November 2010", "7"))));
    }

    @Test
    public void testWrite() throws Exception {
        dataStorage.write(doc, xmp, Arrays.asList(
                Arrays.asList("Saturday, 13 November 2010", "2"),
                Arrays.asList("Sunday, 14 November 2010", "4"),
                Arrays.asList("Monday, 15 November 2010", "7")), 2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));

        assertThat(xmp.getPropertyInteger(DataStorage.SCHEMA_OD, XMPDataStorage.PROP_ROW_SIZE), equalTo(2));
        assertThat(xmp.getArray(DataStorage.SCHEMA_OD, XMPDataStorage.PROP_DATA).toString(),
                equalTo("[Saturday, 13 November 2010, 2, Sunday, 14 November 2010, 4, Monday, 15 November 2010, 7]"));
    }
}
