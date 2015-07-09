package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationDataStorage extends DataStorage {
    @Override
    public Table read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<String> columns = Collections.singletonList("Annotation");
        List<List<Table.Cell>> cells = new ArrayList<>();
        // Really. Generics.
        @SuppressWarnings("unchecked") List<PDPage> pages = doc.getDocumentCatalog().getAllPages();

        for (PDPage page : pages) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation.getContents() == null)
                    continue;
                cells.add(Collections.singletonList(new Table.Cell(annotation.getContents())));
            }
        }

        return new Table(columns, cells, columns.size(), cells.size());
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
