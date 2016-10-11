package im.abe.pdfdata;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFMetaDataStorage extends DataStorage {
    @Override
    public List<Table> read(PDDocument doc, XMPMeta xmp) throws XMPException, IOException {
        List<String> keys = new ArrayList<>();
        List<Table.Cell> values = new ArrayList<>();

        XMPIterator iter = xmp.iterator();
        while (iter.hasNext()) {
            XMPPropertyInfo info = iter.next();

            if (info.getPath() != null) {
                keys.add(info.getPath());
                values.add(new Table.Cell(info.getValue()));
            }
        }

        List<Table> tables = new ArrayList<>();
        List<List<Table.Cell>> cells = new ArrayList<>();
        cells.add(values);
        tables.add(new Table("Metadata", keys, cells));

        return tables;
    }
}
