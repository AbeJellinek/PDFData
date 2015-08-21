package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.google.common.collect.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An annotation-based data storage method. Data is stored as PDF annotations.
 * Writing is not yet supported.
 */
public class AnnotationDataStorage extends DataStorage {
    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<String> columns = Collections.singletonList("Annotation");
        List<List<Table.Cell>> cells = new ArrayList<>();
        // Really. Generics.
        @SuppressWarnings("unchecked") List<PDPage> pages = doc.getDocumentCatalog().getAllPages();

        boolean hasAnnotations = false;
        for (PDPage page : pages) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation.getContents() == null) {
                    continue;
                } else if (!hasAnnotations) {
                    hasAnnotations = true;
                }

                cells.add(Collections.singletonList(new Table.Cell(annotation.getContents())));
            }
        }

        if (!hasAnnotations)
            return new ArrayList<>();
        return Lists.newArrayList(new Table("Annotations", columns, cells, columns.size(), cells.size()));
    }

    /**
     * Throws an exception and exits.
     *
     * @param doc  The document.
     * @param xmp  The document's metadata. Can be modified to update it on disk.
     * @param data The data to be stored.
     * @deprecated Annotation-based storage cannot yet be written. This method will throw an exception.
     */
    @Override
    @Deprecated
    public void write(PDDocument doc, XMPMeta xmp, Table data) {
        throw new RuntimeException("Not yet implemented.");
    }
}
