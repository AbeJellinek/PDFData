package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

/**
 * The common superclass for all data storage methods.
 * Supports reading and writing.
 */
public abstract class DataStorage {
    public static final String SCHEMA_OD = "http://xmptest.abje.me/od/1.0/";

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
     */
    public abstract List<List<String>> read(PDDocument doc, XMPMeta xmp) throws XMPException;

    /**
     * Writes a list of triples to the given document.
     *
     * @param doc     The document.
     * @param xmp     The document's metadata. Can be modified to update it on disk.
     * @param data    The data to be stored.
     * @param rowSize The size of each row.
     * @throws XMPException If an XMP error occurs.
     */
    public abstract void write(PDDocument doc, XMPMeta xmp, List<List<String>> data, int rowSize) throws XMPException;
}
