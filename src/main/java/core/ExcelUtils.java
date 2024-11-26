package core;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {

    private Workbook workbook;
    private Sheet sheet;

    // Constructor to load Excel file and sheet
    public ExcelUtils(String filePath, String sheetName) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(sheetName);
    }

    // Method to get data from a specific cell
    public String getCellData(int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(colIndex);
            if (cell != null) {
                return cell.toString();
            }
        }
        return null;
    }

    // Method to get all data as a Map (key-value pair)
    public Map<String, String> getRowDataAsMap(int rowIndex) {
        Map<String, String> rowData = new HashMap<>();
        Row headerRow = sheet.getRow(0); // Assuming first row contains headers
        Row dataRow = sheet.getRow(rowIndex);

        if (headerRow != null && dataRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String key = headerRow.getCell(i).toString();
                String value = dataRow.getCell(i) != null ? dataRow.getCell(i).toString() : "";
                rowData.put(key, value);
            }
        }
        return rowData;
    }

    // Method to get the number of rows in the sheet
    public int getRowCount() {
        return sheet.getPhysicalNumberOfRows();
    }

    // Close the workbook
    public void closeWorkbook() throws IOException {
        workbook.close();
    }
}
