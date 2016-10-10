package im.abe.pdfdata.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.google.common.io.Files;
import im.abe.pdfdata.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class WriteController {
    @RequestMapping(value = "/write/upload", method = RequestMethod.POST)
    @ResponseBody
    public Resource upload(@RequestParam("pdf") MultipartFile pdf,
                           @RequestParam("data") MultipartFile data,
                           @RequestParam("fragment") String fragment,
                           HttpServletResponse response) throws IOException, XMPException {

        InputStream pdfIn = pdf.getInputStream();
        PDDocument doc = PDDocument.load(pdfIn);

        InputStream dataIn = data.getInputStream();
        Destination destination = Destination.fragment(fragment);

        final String fileName = data.getOriginalFilename();
        final String nameWithoutExtension = Files.getNameWithoutExtension(fileName);

        final Table table;
        if (DataStorage.isXlsFile(fileName)) {
            table = Table.fromXLS(nameWithoutExtension, dataIn);
        } else {
            // assume CSV
            table = Table.fromCSV(nameWithoutExtension, new InputStreamReader(dataIn));
        }

        write(new AttachmentDataStorage(), doc, table, destination);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        doc.save(bytes);
        doc.close();

        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                nameWithoutExtension + "_data.pdf" + "\"");

        return new ByteArrayResource(bytes.toByteArray());
    }

    private void write(WritableDataStorage storage, PDDocument doc, Table table, Destination destination)
            throws IOException, XMPException {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        storage.write(doc, xmp, table, destination);
    }

}
