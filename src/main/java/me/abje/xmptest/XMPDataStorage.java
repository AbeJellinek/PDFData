package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPProperty;
import com.google.common.collect.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * An XMP-based data storage method. Data is stored as an array in the PDF's metadata.
 */
public class XMPDataStorage extends DataStorage {
    public static final String PROP_ROW_SIZE = "RowSize";
    public static final String PROP_DATA = "Data";

    @Override
    public Table read(PDDocument doc, XMPMeta xmp) throws XMPException {
        if (xmp.getProperty(SCHEMA_OD, PROP_DATA) != null) {
            int rowSize = xmp.getPropertyInteger(SCHEMA_OD, PROP_ROW_SIZE);
            List<XMPProperty> items = xmp.getArray(SCHEMA_OD, PROP_DATA);

            List<List<Table.Cell>> cells = new ArrayList<>();
            List<Table.Cell> currentRow = new ArrayList<>();
            int index = 0;

            for (XMPProperty prop : items) {
                currentRow.add(new Table.Cell(prop.getValue()));
                index++;

                if (index >= rowSize) {
                    cells.add(currentRow);
                    index = 0;
                    currentRow = new ArrayList<>();
                }
            }

            return new Table(Lists.newArrayList(), cells, rowSize, cells.size());
        } else {
            return null;
        }
    }

    @Override
    public void write(PDDocument doc, XMPMeta xmp, Table data) throws XMPException {
        PropertyOptions options = new PropertyOptions(PropertyOptions.ARRAY | PropertyOptions.ARRAY_ORDERED);
        xmp.deleteProperty(SCHEMA_OD, PROP_DATA); // Ensure that the data array does not exist.
        xmp.setPropertyInteger(SCHEMA_OD, PROP_ROW_SIZE, data.getWidth());

        for (List<Table.Cell> row : data.getCells()) {
            for (Table.Cell cell : row) {
                xmp.appendArrayItem(SCHEMA_OD, PROP_DATA, options, cell.getValue(), null);
            }
        }
    }
}
