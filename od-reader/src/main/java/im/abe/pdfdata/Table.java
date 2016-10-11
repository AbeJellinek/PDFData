package im.abe.pdfdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A set of tabular data with optional column headings.
 */
public class Table {
    private static final CSVFormat FORMAT = CSVFormat.EXCEL.withHeader();
    private String name;
    private List<String> columnNames;
    private List<List<Cell>> cells;
    private int width, height;

    public Table(String name, List<String> columnNames, List<List<Cell>> cells, int width, int height) {
        this.name = name;
        this.columnNames = columnNames;
        this.cells = cells;
        this.width = width;
        this.height = height;
    }

    public Cell get(int row, int column) {
        if (row >= 0 && row < height && column >= 0 && column < height) {
            return cells.get(row).get(column);
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String getColumnName(int column) {
        if (column >= 0 && column < columnNames.size()) {
            return columnNames.get(column);
        } else {
            return null;
        }
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<List<Cell>> getCells() {
        return cells;
    }

    public String to(Format format) {
        switch (format) {
            case JSON:
                try {
                    return toJSON(false);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            case RDF_XML:
                return toRdfFormat("RDF/XML");
            case TURTLE:
                return toRdfFormat("TURTLE");
            default:
                return toCSV();
        }
    }

    private String toCSV() {
        StringBuilder builder = new StringBuilder();
        try {
            CSVPrinter printer = FORMAT.withHeader(columnNames.toArray(new String[columnNames.size()])).print(builder);
            printer.printRecords(cells);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private static List<Map<String, String>> toJSONData(Table table) {
        List<Map<String, String>> data = new ArrayList<>();
        for (List<Cell> row : table.cells) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < row.size(); i++) {
                Cell cell = row.get(i);
                map.put(table.getColumnName(i), cell.getValue());
            }
            data.add(map);
        }
        return data;
    }

    private String toJSON(boolean prettyPrint) throws JsonProcessingException {
        List<Map<String, String>> data = toJSONData(this);
        if (prettyPrint) {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } else {
            return new ObjectMapper().writeValueAsString(data);
        }
    }

    private String toRdfFormat(String lang) {
        Model model = ModelFactory.createDefaultModel();

        for (List<Cell> columns : cells) {
            Resource row = model.createResource();
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = columns.get(i);
                String escapedName = UrlEscapers.urlFragmentEscaper().escape(columnNames.get(i));
                row.addLiteral(model.createProperty("#", escapedName), cell.getValue());
            }
        }

        StringWriter writer = new StringWriter();
        model.write(writer, lang);
        return writer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(width, table.width) &&
                Objects.equals(height, table.height) &&
                Objects.equals(name, table.name) &&
                Objects.equals(columnNames, table.columnNames) &&
                Objects.equals(cells, table.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, columnNames, cells, width, height);
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", columnNames=" + columnNames +
                ", cells=" + cells +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    public static Table fromCSV(String name, Reader reader) throws IOException {
        CSVParser parser = FORMAT.parse(reader);

        List<String> headers = getHeaders(parser);
        List<List<Table.Cell>> cells = new ArrayList<>();
        int width = 0;
        int height = 0;
        for (CSVRecord record : parser) {
            height++;

            List<Table.Cell> row = new ArrayList<>();
            int i = 0;
            for (String value : record) {
                i++;
                row.add(new Table.Cell(value));
            }
            cells.add(row);

            if (i > width)
                width = i;
        }

        return new Table(name, headers, cells, width, height);
    }

    public static Table fromXLS(String name, InputStream inputStream) throws IOException {
        final Workbook wb;
        try {
            wb = WorkbookFactory.create(inputStream);
        } catch (InvalidFormatException e) {
            throw new IOException("Reading workbook", e);
        }

        // for now, only handle the first sheet
        final Sheet sheet = wb.getSheetAt(0);

        final Row headerRow = sheet.getRow(0);
        List<String> headers = getHeaders(headerRow);

        int runningWidth = headers.size();
        List<List<Table.Cell>> tableCells = new ArrayList<>();

        final Iterator<Row> rows = sheet.rowIterator();
        rows.next();
        while (rows.hasNext()) {
            final List<Table.Cell> resultRow = new LinkedList<>();
            final Row row = rows.next();
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                resultRow.add(new Table.Cell(getCellValueAsString(cell)));
            }
            tableCells.add(resultRow);
            runningWidth = Math.max(runningWidth, resultRow.size());
        }

        return new Table(name, headers, tableCells, runningWidth, tableCells.size());
    }

    private static List<String> getHeaders(CSVParser parser) {
        if (parser.getHeaderMap() == null)
            return new ArrayList<>();

        ArrayList<Map.Entry<String, Integer>> entries = Lists.newArrayList(parser.getHeaderMap().entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private static List<String> getHeaders(Row headerRow) {
        final Iterable<org.apache.poi.ss.usermodel.Cell> iterable = headerRow::cellIterator;
        final Stream<org.apache.poi.ss.usermodel.Cell> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.map(Table::getCellValueAsString).collect(Collectors.toList());
    }

    private static String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        switch (cell.getCellType()) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK: {
                return "";
            }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN: {
                return Boolean.toString(cell.getBooleanCellValue());
            }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR: {
                return "!! ERROR !!";
            }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA: {
                return cell.getCellFormula();
            }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC: {
                return Double.toString(cell.getNumericCellValue());
            }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING: {
                return cell.getStringCellValue();
            }
            default: {
                return "!! UNKNOWN CELL TYPE " + cell.getCellType() + " !!";
            }
        }
    }

    /**
     * A cell within the table. At the moment, this class is just a wrapper around a string, but it exists in order to
     * facilitate adding typed cells in the future.
     */
    public static class Cell {
        private String value;

        public Cell(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return Objects.equals(value, cell.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
