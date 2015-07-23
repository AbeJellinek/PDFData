package me.abje.xmptest;

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A set of tabular data with optional column headings.
 */
public class Table {
    private static final CSVFormat FORMAT = CSVFormat.EXCEL.withHeader();
    private List<String> columns;
    private List<List<Cell>> cells;
    private int width, height;

    public Table(List<String> columns, List<List<Cell>> cells, int width, int height) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(width, table.width) &&
                Objects.equals(height, table.height) &&
                Objects.equals(columns, table.columns) &&
                Objects.equals(cells, table.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, cells, width, height);
    }

    @Override
    public String toString() {
        return "Table{" +
                "columns=" + columns +
                ", cells=" + cells +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    public static Table fromCSV(Reader reader) throws IOException {
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

        return new Table(headers, cells, width, height);
    }

    private static List<String> getHeaders(CSVParser parser) {
        if (parser.getHeaderMap() == null)
            return new ArrayList<>();

        ArrayList<Map.Entry<String, Integer>> entries = Lists.newArrayList(parser.getHeaderMap().entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

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
