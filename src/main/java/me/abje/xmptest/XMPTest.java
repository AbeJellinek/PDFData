package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.PropertyOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.io.*;

public class XMPTest {
    public static final String SCHEMA_OD = "http://ns.example.com/od/1.0/";

    public static void main(String[] args) throws IOException, XMPException {
        new XMPTest().run();
    }

    public void run() throws IOException, XMPException {
        XMPMetaFactory.getSchemaRegistry().registerNamespace(SCHEMA_OD, "od");

        PDDocument doc = PDDocument.load(new FileInputStream("test.pdf"));
        PDDocumentCatalog catalog = doc.getDocumentCatalog();

        XMPMeta xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        xmp.appendArrayItem(SCHEMA_OD, "Data", new PropertyOptions(PropertyOptions.ARRAY |
                PropertyOptions.ARRAY_ORDERED),
                "Hello, world!", null);
        saveMetadata(doc, xmp);
        System.out.println(catalog.getMetadata().getInputStreamAsString());
    }

    private void saveMetadata(PDDocument doc, XMPMeta xmp) throws IOException, XMPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));
    }
}
