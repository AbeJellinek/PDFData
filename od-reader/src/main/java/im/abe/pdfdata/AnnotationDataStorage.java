package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.google.common.collect.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
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
        List<List<String>> cells = new ArrayList<>();
        PDPageTree pages = doc.getDocumentCatalog().getPages();

        boolean hasAnnotations = false;
        for (PDPage page : pages) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation.getContents() == null) {
                    continue;
                } else if (!hasAnnotations) {
                    hasAnnotations = true;
                }

                cells.add(Collections.singletonList(annotation.getContents()));
            }
        }

        if (!hasAnnotations)
            return new ArrayList<>();
        return Lists.newArrayList(new Table("Annotations", columns, cells));
    }
}
