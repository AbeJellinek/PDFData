package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.io.*;

public class XMPTest {
    public static void main(String[] args) throws IOException, XMPException {
        new XMPTest().run();
    }

    public void run() throws IOException, XMPException {
        PDDocument doc = PDDocument.load(new FileInputStream("test.pdf"));
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
    }

    private void saveMetadata(PDDocument doc, XMPMeta xmp) throws IOException, XMPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(xmp, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        doc.getDocumentCatalog().setMetadata(new PDMetadata(doc, in, false));
    }
}
