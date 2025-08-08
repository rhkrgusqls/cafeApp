package com.example.demo1.controller.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static void writeExcelFromMapList(List<Map<String, Object>> data, String filePath) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Empty data");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            // Create Header Row
            Map<String, Object> firstRow = data.get(0);
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(key);
            }

            // Create Data Rows
            int rowIndex = 1;
            for (Map<String, Object> rowMap : data) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;
                for (Object value : rowMap.values()) {
                    Cell cell = row.createCell(colIndex++);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < firstRow.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
    }
}
