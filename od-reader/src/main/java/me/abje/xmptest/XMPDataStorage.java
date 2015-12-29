package me.abje.xmptest;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPProperty;
import com.google.common.collect.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An XMP-based data storage method. Data is stored as an array in the PDF's metadata.
 */
public class XMPDataStorage extends DataStorage {
    public static final String PROP_ROW_SIZE = "RowSize";
    public static final String PROP_DATA = "Data";
    private static final String PROP_COLUMNS = "Columns";

    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException {
        if (xmp.getProperty(SCHEMA_OD, PROP_DATA) != null) {
            int rowSize = xmp.getPropertyInteger(SCHEMA_OD, PROP_ROW_SIZE);
            List<XMPProperty> items = xmp.getArray(SCHEMA_OD, PROP_DATA);
            List<XMPProperty> columnProperties = xmp.getArray(SCHEMA_OD, PROP_COLUMNS);

            if (items == null || columnProperties == null)
                return Lists.newArrayList();

            List<String> columns = columnProperties.stream().map(XMPProperty::getValue).collect(Collectors.toList());

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

            return Lists.newArrayList(new Table("Metadata", columns, cells, rowSize, cells.size()));
        } else {
            return Lists.newArrayList();
        }
    }
}
