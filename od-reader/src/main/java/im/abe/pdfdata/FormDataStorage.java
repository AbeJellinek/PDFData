package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.google.common.collect.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An annotation-based data storage method. Data is stored in the PDF's form.
 * In order to be written to, a PDF must already have a form present. This storage method will not create one.
 */
public class FormDataStorage extends DataStorage {
    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
        if (form == null || form.getFields().isEmpty())
            return new ArrayList<>();

        List<String> columns = new ArrayList<>();
        List<Table.Cell> cells = new ArrayList<>();
        for (PDField field : form.getFields()) {
            columns.add(field.getPartialName());
            try {
                String value = field.getValueAsString();
                cells.add(new Table.Cell(
                        value == null || value.equals("\u00fe\u00ff") ? "" : value));
                // "\u00fe\u00ff" seems to be the null code for PDF form fields. Need to research.
            } catch (RuntimeException e) {
                // getValue() can throw a wide variety of interesting exceptions,
                // even though it promises only to throw IOException (and never does). We don't want any of them.
                cells.add(new Table.Cell(""));
            }
        }
        return Lists.newArrayList(new Table("Form", columns, Collections.singletonList(cells), columns.size(), cells.size()));
    }
}
