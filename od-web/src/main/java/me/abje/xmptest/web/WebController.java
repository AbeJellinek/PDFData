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
import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping(value = "/upload", method = RequestMethod.POST, params = "type=table")
    public String upload(@RequestParam("file") MultipartFile file,
                         Model model) throws IOException, XMPException {
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

    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "text/csv", params = "type=csv")
    @ResponseBody
    public String uploadToCSV(@RequestParam("file") MultipartFile file,
                              HttpServletResponse response) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        StringBuilder sb = new StringBuilder();
        for (Table table : tables) {
            if (tables.size() != 1)
                sb.append("# ---- ").append(table.getName()).append(" ----\n");
            sb.append(table.toCSV());
        }

        response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");

        return sb.toString();
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json", params = "type=json")
    @ResponseBody
    public String uploadToJSON(@RequestParam("file") MultipartFile file,
                               HttpServletResponse response) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        response.setHeader("Content-Disposition", "attachment; filename=\"data.json\"");

        return Table.allToJSON(tables, false);
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public String jsonReadUrl(@RequestBody ReadRequest read) throws IOException, XMPException {
        InputStream in = new URL(read.getUrl()).openStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();
        in.close();

        return Table.allToJSON(tables, false);
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST,
            consumes = "application/x-www-form-urlencoded",
            produces = "application/json")
    @ResponseBody
    public String formReadUrl(ReadRequest read) throws IOException, XMPException {
        return jsonReadUrl(read);
    }

    @RequestMapping(value = "/read", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String readPost(@RequestParam("file") MultipartFile file) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();
        in.close();

        return Table.allToJSON(tables, false);
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
