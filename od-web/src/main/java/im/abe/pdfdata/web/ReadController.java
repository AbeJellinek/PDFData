package im.abe.pdfdata.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import im.abe.pdfdata.*;
import me.abje.xmptest.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class ReadController {
    @RequestMapping(value = "/read/upload", method = RequestMethod.POST, params = "type=table")
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

    @RequestMapping(value = "/read/upload", method = RequestMethod.POST, params = "type=csv")
    @ResponseBody
    public Resource uploadToCSV(@RequestParam("file") MultipartFile file,
                                HttpServletResponse response) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);

        if (tables.size() > 1) {
            for (Table table : tables) {
                zip.putNextEntry(new ZipEntry(table.getName() + ".csv"));
                zip.write(table.toCSV().getBytes("UTF-8"));
                zip.closeEntry();
            }

            zip.close();

            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"data.zip\"");
            return new ByteArrayResource(out.toByteArray());
        } else {
            response.setHeader("Content-Type", "text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");
            return new ByteArrayResource(tables.get(0).toCSV().getBytes("UTF-8"));
        }
    }

    @RequestMapping(value = "/read/upload", method = RequestMethod.POST, produces = "application/json", params = "type=json")
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

        return Table.allToJSON(tables, true);
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

        return Table.allToJSON(tables, true);
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

        return Table.allToJSON(tables, true);
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
}
