package me.abje.xmptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A set of tabular data with optional column headings.
 */
public class Table {
    private static final CSVFormat FORMAT = CSVFormat.EXCEL.withHeader();
    private String name;
    private List<String> columns;
    private List<List<Cell>> cells;
    private int width, height;

    public Table(String name, List<String> columns, List<List<Cell>> cells, int width, int height) {
        this.name = name;
        this.columns = columns;
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

    public String getColumnName(int column) {
        if (column >= 0 && column < columns.size()) {
            return columns.get(column);
        } else {
            return null;
        }
    }

    public List<String> getColumnNames() {
        return columns;
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

    public String toCSV() {
        StringBuilder builder = new StringBuilder();
        try {
            CSVPrinter printer = FORMAT.withHeader(columns.toArray(new String[columns.size()])).print(builder);
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

    public String toJSON(boolean prettyPrint) throws JsonProcessingException {
        List<Map<String, String>> data = toJSONData(this);
        if (prettyPrint) {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } else {
            return new ObjectMapper().writeValueAsString(data);
        }
    }

    public static String allToJSON(List<Table> tables, boolean prettyPrint) throws JsonProcessingException {
        List<List<Map<String, String>>> data = tables.stream().map(Table::toJSONData).collect(Collectors.toList());
        if (prettyPrint) {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } else {
            return new ObjectMapper().writeValueAsString(data);
        }
    }

    public String toFormat(String lang) {
        Model model = ModelFactory.createDefaultModel();

        List<RDFNode> nodes = new ArrayList<>();
        for (List<Cell> rowList : cells) {
            Resource row = model.createResource();
            for (int i = 0; i < rowList.size(); i++) {
                Cell cell = rowList.get(i);
                row.addLiteral(model.createProperty(DataStorage.SCHEMA_OD, "_c" + i), cell.getValue());
            }
            nodes.add(row);
        }

        RDFList rows = model.createList(nodes.iterator());
        model.createResource().addProperty(model.createProperty(DataStorage.SCHEMA_OD, "Data"), rows);

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
                Objects.equals(columns, table.columns) &&
                Objects.equals(cells, table.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, columns, cells, width, height);
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
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

    private static List<String> getHeaders(CSVParser parser) {
        if (parser.getHeaderMap() == null)
            return new ArrayList<>();

        ArrayList<Map.Entry<String, Integer>> entries = Lists.newArrayList(parser.getHeaderMap().entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
