package im.abe.pdfdata.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import im.abe.pdfdata.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class ReadController {
    @RequestMapping(value = "/read/upload", method = RequestMethod.POST, params = "type=table")
    public String readAsTable(@RequestParam("file") MultipartFile file,
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

    @RequestMapping(value = "/read/upload", method = RequestMethod.POST, params = "type!=table")
    public void readAsFormat(@RequestParam("file") MultipartFile file,
                             @RequestParam("type") String type,
                             HttpServletResponse response) throws IOException, XMPException {
        InputStream in = file.getInputStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();

        writeAll(tables, Format.find(type), response);
        response.flushBuffer();
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json")
    public void apiReadUrlFromJson(@RequestBody ReadRequest read,
                                   HttpServletResponse response) throws IOException, XMPException {
        InputStream in = new URL(read.getUrl()).openStream();

        PDDocument doc = PDDocument.load(in);
        List<Table> tables = new ArrayList<>();

        tables.addAll(read(new AnnotationDataStorage(), doc));
        tables.addAll(read(new AttachmentDataStorage(), doc));
        tables.addAll(read(new FormDataStorage(), doc));
        tables.addAll(read(new XMPDataStorage(), doc));

        doc.close();
        in.close();

        writeAll(tables, Format.JSON, response);
        response.flushBuffer();
    }

    @RequestMapping(value = "/read/url", method = RequestMethod.POST,
            consumes = "application/x-www-form-urlencoded",
            produces = "application/json")
    public void apiReadUrlFromForm(ReadRequest read, HttpServletResponse response) throws IOException, XMPException {
        apiReadUrlFromJson(read, response);
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

    private void writeAll(List<Table> tables, Format format, HttpServletResponse response) throws IOException {
        if (tables.size() != 1) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"download.zip\""); // todo: use actual filename
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            Map<String, Integer> nameCounts = new HashMap<>();
            for (Table table : tables) {
                int count = nameCounts.getOrDefault(table.getName(), 0);
                nameCounts.put(table.getName(), count + 1);

                ZipEntry entry = new ZipEntry(table.getName() + (count == 0 ? "" : "_" + count)
                        + format.getExtension());
                byte[] serialized = table.to(format).getBytes("UTF-8");
                entry.setSize(serialized.length);
                zos.putNextEntry(entry);
                zos.write(serialized);
                zos.closeEntry();
            }

            zos.close();
        } else {
            response.setContentType(format.getMime());
            response.getOutputStream().write(tables.get(0).to(format).getBytes("UTF-8"));
        }
    }
}
