package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPProperty;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * An XMP-based data storage method. Data is stored as an array in the PDF's metadata.
 */
public class XMPDataStorage extends DataStorage<List<List<String>>> {
    public static final String PROP_ROW_SIZE = "RowSize";
    public static final String PROP_DATA = "Data";

    @Override
    public List<List<String>> read(PDDocument doc, XMPMeta xmp) throws XMPException {
        if (xmp.getProperty(SCHEMA_OD, PROP_DATA) != null) {
            int rowSize = xmp.getPropertyInteger(SCHEMA_OD, PROP_ROW_SIZE);
            List<XMPProperty> items = xmp.getArray(SCHEMA_OD, PROP_DATA);

            List<List<String>> table = new ArrayList<>();
            List<String> currentRow = new ArrayList<>();
            int index = 0;

            for (XMPProperty prop : items) {
                currentRow.add(prop.getValue());
                index++;

                if (index >= rowSize) {
                    table.add(currentRow);
                    index = 0;
                    currentRow = new ArrayList<>();
                }
            }

            return table;
        } else {
            return null;
        }
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, List<List<String>> data) throws XMPException {
        PropertyOptions options = new PropertyOptions(PropertyOptions.ARRAY | PropertyOptions.ARRAY_ORDERED);
        xmp.deleteProperty(SCHEMA_OD, PROP_DATA); // Ensure that the data array does not exist.
        xmp.setPropertyInteger(SCHEMA_OD, PROP_ROW_SIZE, data.isEmpty() ? 0 : data.get(0).size());

        for (List<String> row : data) {
            for (String cell : row) {
                xmp.appendArrayItem(SCHEMA_OD, PROP_DATA, options, cell, null);
            }
        }
    }
}
