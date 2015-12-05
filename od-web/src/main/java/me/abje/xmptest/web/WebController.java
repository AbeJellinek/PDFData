package me.abje.xmptest.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import me.abje.xmptest.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file, Model model) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        model.addAttribute("tables", tables);
        return "readResults";
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public List<Table> jsonReadUrl(@RequestBody ReadRequest read) throws IOException, XMPException {
        InputStream in = new URL(read.getUrl()).openStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        return tables;
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    @ResponseBody
    public List<Table> formReadUrl(ReadRequest read) throws IOException, XMPException {
        return jsonReadUrl(read);
    }

    private List<Table> read(DataStorage storage, PDDocument doc) throws IOException, XMPException {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMeta xmp;
        if (catalog.getMetadata() == null) {
            xmp = XMPMetaFactory.create();
        } else {
            xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
        }

        return storage.read(doc, xmp);
    }

    @ModelAttribute("_csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

}
