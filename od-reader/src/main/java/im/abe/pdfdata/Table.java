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
import org.apache.poi.ss.usermodel.Cell;
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
    private List<List<String>> cells;

    public Table(String name, List<String> columnNames, List<List<String>> cells) {
        this.name = name;
        this.columnNames = columnNames;
        this.cells = cells;
    }

    public String get(int row, int column) {
        if (row >= 0 && row < cells.size() && column >= 0) {
            List<String> rowList = cells.get(row);
            if (column < rowList.size()) {
                return rowList.get(column);
            } else {
                return null;
            }
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

    public List<List<String>> getCells() {
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
        for (List<String> row : table.cells) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                map.put(table.getColumnName(i), cell);
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

        for (List<String> columns : cells) {
            Resource row = model.createResource();
            for (int i = 0; i < columns.size(); i++) {
                String cell = columns.get(i);
                String escapedName = UrlEscapers.urlFragmentEscaper().escape(columnNames.get(i));
                row.addLiteral(model.createProperty("#", escapedName), cell);
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
        return Objects.equals(name, table.name) &&
                Objects.equals(columnNames, table.columnNames) &&
                Objects.equals(cells, table.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, columnNames, cells);
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", columnNames=" + columnNames +
                ", cells=" + cells +
                '}';
    }

    public static Table fromCSV(String name, Reader reader) throws IOException {
        CSVParser parser = FORMAT.parse(reader);

        List<String> headers = getHeaders(parser);
        List<List<String>> cells = new ArrayList<>();
        for (CSVRecord record : parser) {
            List<String> row = new ArrayList<>();
            for (String value : record)
                row.add(value);
            cells.add(row);
        }

        return new Table(name, headers, cells);
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
        List<List<String>> tableCells = new ArrayList<>();

        final Iterator<Row> rows = sheet.rowIterator();
        rows.next();
        while (rows.hasNext()) {
            final List<String> resultRow = new LinkedList<>();
            final Row row = rows.next();
            for (Cell cell : row) {
                resultRow.add(getCellValueAsString(cell));
            }
            tableCells.add(resultRow);
            runningWidth = Math.max(runningWidth, resultRow.size());
        }

        return new Table(name, headers, tableCells);
    }

    private static List<String> getHeaders(CSVParser parser) {
        if (parser.getHeaderMap() == null)
            return new ArrayList<>();

        ArrayList<Map.Entry<String, Integer>> entries = Lists.newArrayList(parser.getHeaderMap().entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getValue));
        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private static List<String> getHeaders(Row headerRow) {
        final Iterable<Cell> iterable = headerRow::cellIterator;
        final Stream<Cell> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.map(Table::getCellValueAsString).collect(Collectors.toList());
    }

    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK: {
                return "";
            }
            case Cell.CELL_TYPE_BOOLEAN: {
                return Boolean.toString(cell.getBooleanCellValue());
            }
            case Cell.CELL_TYPE_ERROR: {
                return "!! ERROR !!";
            }
            case Cell.CELL_TYPE_FORMULA: {
                return cell.getCellFormula();
            }
            case Cell.CELL_TYPE_NUMERIC: {
                return Double.toString(cell.getNumericCellValue());
            }
            case Cell.CELL_TYPE_STRING: {
                return cell.getStringCellValue();
            }
            default: {
                return "!! UNKNOWN CELL TYPE " + cell.getCellType() + " !!";
            }
        }
    }
}
