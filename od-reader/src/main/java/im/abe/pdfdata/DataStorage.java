package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.google.common.io.Files;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

/**
 * The common superclass for all data storage methods.
 * Supports reading and writing.
 */
public abstract class DataStorage {
    /**
     * The schema used for open data properties. Registered in this class's {@code static {}} block.
     */
    public static final String SCHEMA_OD = "http://pdf.abe.im/od/1.0/";

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace(DataStorage.SCHEMA_OD, "od");
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the triples stored in the given document.
     *
     * @param doc The document.
     * @param xmp The document's XMP metadata.
     * @return The stored triples.
     * @throws XMPException If an XMP error occurs.
     * @throws IOException  If an I/O error occurs. (Unlikely.)
     */
    public abstract List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException;

    /**
     * Return true if this file name denotes an XLS or XLSX file.  Perhaps it would be better to test the
     * content-type of the uploaded file?
     *
     * @param fileName the file name to test
     * @return true if it's an XLS or XLSX file, false otherwise
     */
    public static boolean isXlsFile(String fileName) {
        final String extension = Files.getFileExtension(fileName).toLowerCase();
        return "xls".equals(extension) || "xlsx".equals(extension);
    }

    /**
     * Return true if this MIME type denotes an XLS or XLSX file.
     *
     * @param mime the MIME type to test
     * @return true if it's an XLS or XLSX file, false otherwise
     */
    public static boolean isXlsType(String mime) {
        return "application/vnd.ms-excel".equals(mime) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mime);
    }
}
