package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public abstract class WritableDataStorage extends DataStorage {
    /**
     * Writes a list of triples to the given document.
     *
     * @param doc  The document.
     * @param xmp  The document's metadata. Can be modified to update it on disk.
     * @param data The data to be stored.
     * @param page The page to write to. If -1, write to the entire document.
     * @throws XMPException If an XMP error occurs.
     * @throws IOException  If an I/O error occurs. (Unlikely.)
     */
    public abstract void write(PDDocument doc, XMPMeta xmp, Table data, int page) throws XMPException, IOException;
}
