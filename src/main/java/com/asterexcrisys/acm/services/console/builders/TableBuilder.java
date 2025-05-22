package com.asterexcrisys.acm.services.console.builders;

import com.asterexcrisys.acm.constants.ConsoleConstants;
import com.asterexcrisys.acm.types.console.CellSize;
import com.asterexcrisys.acm.utility.ConsoleUtility;
import com.asterexcrisys.acm.utility.GlobalUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class TableBuilder implements AutoCloseable {

    private final List<String> attributes;
    private final List<List<String>> records;
    private CellSize cellSize;

    public TableBuilder() {
        attributes = new ArrayList<>();
        records = new ArrayList<>();
        cellSize = CellSize.WRAP_MEDIUM;
    }

    public TableBuilder(CellSize cellSize) {
        attributes = new ArrayList<>();
        records = new ArrayList<>();
        this.cellSize = Objects.requireNonNull(cellSize);
    }

    public TableBuilder(List<String> attributes, List<List<String>> records) throws NullPointerException {
        this.attributes = new ArrayList<>(Objects.requireNonNull(attributes));
        this.records = new ArrayList<>(Objects.requireNonNull(records));
        cellSize = CellSize.WRAP_MEDIUM;
    }

    public TableBuilder(List<String> attributes, List<List<String>> records, CellSize cellSize) throws NullPointerException {
        this.attributes = new ArrayList<>(Objects.requireNonNull(attributes));
        this.records = new ArrayList<>(Objects.requireNonNull(records));
        this.cellSize = Objects.requireNonNull(cellSize);
    }

    public List<String> getAttributes() {
        return List.copyOf(attributes);
    }

    public List<List<String>> getRecords() {
        return List.copyOf(records);
    }

    public CellSize getCellSize() {
        return cellSize;
    }

    public void setCellSize(CellSize cellSize) {
        if (cellSize == null) {
            return;
        }
        this.cellSize = cellSize;
    }

    public TableBuilder addAttribute(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return this;
        }
        if (attributes.contains(attribute)) {
            return this;
        }
        attributes.add(attribute);
        return this;
    }

    public TableBuilder addAttributes(List<String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return this;
        }
        if (this.attributes.stream().anyMatch(attributes::contains)) {
            return this;
        }
        this.attributes.addAll(attributes);
        return this;
    }

    public TableBuilder addRecord(List<String> record) {
        if (record == null || record.isEmpty()) {
            return this;
        }
        records.add(record);
        return this;
    }

    public TableBuilder addRecords(List<List<String>> records) {
        if (records == null || records.isEmpty()) {
            return this;
        }
        if (records.stream().anyMatch(Objects::isNull)) {
            return this;
        }
        this.records.addAll(records);
        return this;
    }

    public String build() throws IllegalStateException {
        Optional<String> tableHead = createTableHead(attributes, cellSize);
        if (tableHead.isEmpty()) {
            throw new IllegalStateException();
        }
        Optional<String> tableBody = createTableBody(records, attributes.size(), cellSize);
        if (tableBody.isEmpty()) {
            throw new IllegalStateException();
        }
        return tableHead.get().concat(tableBody.get());
    }

    public void clear() {
        attributes.clear();
        records.clear();
    }

    public void close() {
        clear();
    }

    private static String createTableSeparator(int attributesCount, int attributesWidth) {
        StringBuilder tableSeparator = new StringBuilder();
        for (int i = 0; i < attributesCount; i++) {
            tableSeparator.append('+');
            tableSeparator.append("-".repeat(attributesWidth));
        }
        tableSeparator.append('+');
        return tableSeparator.toString();
    }

    private static Optional<String> createTableRow(List<String> values, CellSize cellSize) {
        if (values == null || values.isEmpty() || cellSize == null) {
            return Optional.empty();
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            return Optional.empty();
        }
        StringBuilder tableRow = new StringBuilder();
        tableRow.append(createTableSeparator(values.size(), cellSize.width()));
        tableRow.append('\n');
        List<String[]> justifiedValues = new ArrayList<>();
        int maxHeight = Integer.MIN_VALUE;
        int maxWidth = cellSize.width() - 2;
        for (String value : values) {
            justifiedValues.add(ConsoleUtility.InteractiveShell.justifyText(value, maxWidth));
            if (justifiedValues.getLast().length > maxHeight) {
                maxHeight = justifiedValues.getLast().length;
            }
        }
        for (int i = 0; i < Math.max(maxHeight, cellSize.height()); i++) {
            tableRow.append("| ");
            for (String[] justifiedValue : justifiedValues) {
                if (justifiedValue.length > i) {
                    if (justifiedValue[i] == null) {
                        tableRow.append(String.format("%-" + maxWidth + "s", ConsoleConstants.NULL_CELL));
                        break;
                    }
                    tableRow.append(String.format("%-" + maxWidth + "s", justifiedValue[i]));
                } else {
                    tableRow.append(String.format("%-" + maxWidth + "s", ConsoleConstants.EMPTY_CELL));
                }
                tableRow.append(" | ");
            }
            tableRow.append('\n');
        }
        return Optional.of(tableRow.toString());
    }

    private static Optional<String> createTableHead(List<String> attributes, CellSize cellSize) {
        StringBuilder tableHead = new StringBuilder();
        Optional<String> tableRow = createTableRow(attributes, cellSize);
        if (tableRow.isEmpty()) {
            return Optional.empty();
        }
        tableHead.append(tableRow.get());
        tableHead.append(createTableSeparator(attributes.size(), cellSize.width()));
        tableHead.append('\n');
        return Optional.of(tableHead.toString());
    }

    private static Optional<String> createTableBody(List<List<String>> records, int attributesCount, CellSize cellSize) {
        StringBuilder tableBody = new StringBuilder();
        for (List<String> record : records) {
            GlobalUtility.resizeList(record, attributesCount);
            Optional<String> tableRow = createTableRow(record, cellSize);
            if (tableRow.isEmpty()) {
                return Optional.empty();
            }
            tableBody.append(tableRow.get());
        }
        tableBody.append(createTableSeparator(attributesCount, cellSize.width()));
        tableBody.append('\n');
        return Optional.of(tableBody.toString());
    }

}
