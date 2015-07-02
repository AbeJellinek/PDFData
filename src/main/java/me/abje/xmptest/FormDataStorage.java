package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormDataStorage extends DataStorage {
    @Override
    public Table read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
        if (form == null)
            return null;

        List<String> columns = new ArrayList<>();
        List<Table.Cell> cells = new ArrayList<>();
        for (Object obj : form.getFields()) {
            PDField field = (PDField) obj; // Generics... Use them.
            columns.add(field.getPartialName());
            try {
                String value = field.getValue();
                cells.add(new Table.Cell(
                        value == null || value.equals("\u00fe\u00ff") ? "" : value));
                // "\u00fe\u00ff" seems to be the null code for PDF form fields. Need to research.
            } catch (RuntimeException e) {
                // getValue() can throw a wide variety of interesting exceptions,
                // even though it promises only to throw IOException (and never does). We don't want any of them.
                cells.add(new Table.Cell(""));
            }
        }
        return new Table(columns, Collections.singletonList(cells), form.getFields().size(), cells.size());
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table data) throws IOException {
        if (data.getHeight() != 1)
            throw new RuntimeException("Too many rows in data.");

        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
        if (form == null) {
            form = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(form);
        }

        for (List<Table.Cell> row : data.getCells()) {
            int i = 0;
            for (Table.Cell cell : row) {
                PDField field = form.getField(data.getColumnName(i));
                if (field != null) {
                    field.setValue(cell.getValue());
                }
                i++;
            }
        }
    }
}
