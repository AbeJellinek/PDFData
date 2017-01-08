package im.abe.pdfdata.web;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import im.abe.pdfdata.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class WriteController {
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir", "pdfdata_tmp"));

    static {
        if (!TEMP_DIR.exists() && !TEMP_DIR.mkdirs()) {
            throw new RuntimeException("Failed to create temporary upload directory!");
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String write() {
        return "index";
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST)
    public String edit(@RequestParam("pdf") MultipartFile pdf, @RequestParam("download") boolean download,
                       Model model, HttpServletResponse response) throws IOException, XMPException {

        InputStream in = pdf.getInputStream();
        PDDocument doc = PDDocument.load(in);
        in.close();

        if (download) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            XMPMeta xmp;
            if (catalog.getMetadata() == null) {
                xmp = XMPMetaFactory.create();
            } else {
                xmp = XMPMetaFactory.parse(catalog.getMetadata().createInputStream());
            }

            List<Table> tables = new ArrayList<>();

            tables.addAll(new AnnotationDataStorage().read(doc, xmp));
            tables.addAll(new AttachmentDataStorage().read(doc, xmp));
            tables.addAll(new FormDataStorage().read(doc, xmp));
            tables.addAll(new XMPDataStorage().read(doc, xmp));

            zipAll(tables, Format.CSV, response);
        }

        // File.getName() is called because Opera sometimes sends the full path. Ew.
        model.addAttribute("fileName", new File(pdf.getOriginalFilename()).getName());
        model.addAttribute("contents", getPreview(doc));

        doc.close();

        StringBuilder tokenBuilder = new StringBuilder("file_");
        Random random = new Random();
        for (int i = 0; i < 32; i++) {
            int choice = random.nextInt(8);
            if (choice < 6) {
                tokenBuilder.append((char) ('a' + random.nextInt(26)));
            } else if (choice == 6) {
                tokenBuilder.append((char) ('A' + random.nextInt(26)));
            } else if (choice == 7) {
                if (tokenBuilder.charAt(tokenBuilder.length() - 1) == '_')
                    tokenBuilder.append(random.nextInt(10));
                else
                    tokenBuilder.append('_');
            }
        }

        String token = tokenBuilder.toString();
        File file = new File(TEMP_DIR, token);
        file.deleteOnExit();
        pdf.transferTo(file);
        model.addAttribute("token", token);

        return "editor";
    }

    @RequestMapping(value = "/write/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("token") String token,
                         @RequestParam("fileName") String fileName,
                         @RequestParam("data") MultipartFile data,
                         @RequestParam("fragment") String fragment,
                         Model model) throws IOException, XMPException {

        if (Pattern.matches("[^a-zA-Z0-9_]", token))
            throw new IllegalArgumentException("Invalid token!");

        File pdf = new File(TEMP_DIR, token);

        InputStream pdfIn = new FileInputStream(pdf);
        PDDocument doc = PDDocument.load(pdfIn);
        pdfIn.close();

        InputStream dataIn = data.getInputStream();
        Destination destination = Destination.fragment(fragment);

        final String name = destination.nameAttachment(doc, fileName);
        final Table table;
        if (DataStorage.isXlsFile(data.getOriginalFilename())) {
            table = Table.fromXLS(name, dataIn);
        } else {
            // assume CSV
            table = Table.fromCSV(name, new InputStreamReader(dataIn));
        }

        write(new AttachmentDataStorage(), doc, table, destination);

        OutputStream pdfOut = new FileOutputStream(pdf);
        doc.save(pdfOut);

        model.addAttribute("token", token);
        model.addAttribute("fileName", fileName);
        model.addAttribute("contents", getPreview(doc));

        doc.close();

        return "editor";
    }

    @RequestMapping(value = "/write/find", method = RequestMethod.POST)
    @ResponseBody
    public String find(@RequestParam("token") String token,
                       @RequestParam("fileName") String fileName,
                       @RequestParam("fragment") String fragment,
                       HttpServletResponse response) throws IOException, XMPException {

        if (Pattern.matches("[^a-zA-Z0-9_]", token))
            throw new IllegalArgumentException("Invalid token!");

        File pdf = new File(TEMP_DIR, token);

        InputStream pdfIn = new FileInputStream(pdf);
        PDDocument doc = PDDocument.load(pdfIn);
        pdfIn.close();

        Table found = new AttachmentDataStorage().find(doc, fileName, fragment);
        if (found == null)
            throw new IllegalArgumentException("Unknown attachment.");

        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        return found.to(Format.CSV);
    }

    @RequestMapping(value = "/write/download/{token}/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> download(
            @PathVariable("token") String token,
            @PathVariable("fileName") String fileName) throws IOException, XMPException {

        if (Pattern.matches("[^a-zA-Z0-9_]", token))
            throw new IllegalArgumentException("Invalid token!");

        File pdf = new File(TEMP_DIR, token);

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentLength(pdf.length());
        respHeaders.setContentDispositionFormData("attachment", fileName + ".pdf");

        InputStreamResource isr = new InputStreamResource(new FileInputStream(pdf));
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    private List<AttachmentDataStorage.FilePreview> getPreview(PDDocument doc) throws IOException, XMPException {
        return new AttachmentDataStorage().preview(doc);
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

    private void zipAll(List<Table> tables, Format format, HttpServletResponse response) throws IOException {
        if (tables.size() != 1) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"download.zip\"");
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
