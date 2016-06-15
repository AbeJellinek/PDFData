package me.abje.xmptest.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import me.abje.xmptest.AttachmentDataStorage;
import me.abje.xmptest.Destination;
import me.abje.xmptest.Table;
import me.abje.xmptest.WritableDataStorage;
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
                           @RequestParam("page") int page,
                           @RequestParam("namedDest") String namedDest,
                           @RequestParam("fragment") String fragment,
                           HttpServletResponse response) throws IOException, XMPException {

        InputStream pdfIn = pdf.getInputStream();
        PDDocument doc = PDDocument.load(pdfIn);

        InputStream dataIn = data.getInputStream();
        Destination destination;
        if (!fragment.isEmpty())
            destination = Destination.fragment(fragment);
        else if (!namedDest.isEmpty())
            destination = Destination.named(namedDest);
        else if (page != 0)
            destination = Destination.page(page - 1);
        else
            destination = Destination.document();

        write(new AttachmentDataStorage(), doc, Table.fromCSV(data.getOriginalFilename().replaceFirst("(.*)\\.csv$", "$1"),
                new InputStreamReader(dataIn)), destination);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        doc.save(bytes);
        doc.close();

        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                pdf.getOriginalFilename().replace(".pdf", "_data.pdf") + "\"");

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
