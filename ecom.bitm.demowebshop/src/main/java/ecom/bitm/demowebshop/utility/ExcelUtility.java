
package ecom.bitm.demowebshop.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtility implements AutoCloseable {

    private Workbook workbook;
    private Sheet sheet;
    private final DataFormatter formatter = new DataFormatter();

    // Opens the Excel file and selects the required sheet
    public ExcelUtility(String filePath, String sheetName)
            throws IOException {

        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = WorkbookFactory.create(fis);
        }

        sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
            workbook.close();
            throw new IllegalArgumentException(
                    "Sheet not found: " + sheetName);
        }
    }

    // Returns the number of data rows, excluding the header
    public int getRowCount() {
        return Math.max(0, sheet.getLastRowNum());
    }

    // Returns the number of columns in the header row
    public int getColumnCount() {
        Row header = sheet.getRow(0);
        return header == null ? 0 : header.getLastCellNum();
    }

    // Finds a column using its header name
    private int getColumnIndex(String columnName) {

        Row header = sheet.getRow(0);

        if (header == null) {
            throw new IllegalStateException(
                    "Excel header row is missing.");
        }

        for (int i = 0; i < header.getLastCellNum(); i++) {
            String headerName = formatter.formatCellValue(
                    header.getCell(i)).trim();

            if (headerName.equalsIgnoreCase(columnName.trim())) {
                return i;
            }
        }

        throw new IllegalArgumentException(
                "Column not found: " + columnName);
    }

    // dataRowIndex 0 means the first row below the header
    public String getCellData(int dataRowIndex, String columnName) {

        int excelRowIndex = dataRowIndex + 1;

        if (dataRowIndex < 0
                || excelRowIndex > sheet.getLastRowNum()) {
            throw new IndexOutOfBoundsException(
                    "Invalid data row index: " + dataRowIndex);
        }

        Row row = sheet.getRow(excelRowIndex);

        if (row == null) {
            return "";
        }

        return formatter.formatCellValue(
                row.getCell(getColumnIndex(columnName))).trim();
    }

    // Fetches all rows matching a TestCaseID
    public List<Map<String, String>> getRowsByTestCaseID(
            String testCaseID) {

        List<Map<String, String>> matchingRows = new ArrayList<>();
        Row header = sheet.getRow(0);

        if (header == null) {
            return matchingRows;
        }

        int testCaseColumn = getColumnIndex("TestCaseID");

        for (int rowIndex = 1;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++) {

            Row row = sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            String currentID = formatter.formatCellValue(
                    row.getCell(testCaseColumn)).trim();

            if (currentID.equalsIgnoreCase(testCaseID)) {

                Map<String, String> data = new LinkedHashMap<>();

                for (int col = 0;
                        col < header.getLastCellNum();
                        col++) {

                    String columnName = formatter.formatCellValue(
                            header.getCell(col)).trim();

                    String cellValue = formatter.formatCellValue(
                            row.getCell(col)).trim();

                    data.put(columnName, cellValue);
                }

                matchingRows.add(data);
            }
        }

        return matchingRows;
    }

    // Closes the workbook after reading the data
    public void closeWorkbook() throws IOException {
        if (workbook != null) {
            workbook.close();
            workbook = null;
        }
    }

    // Allows ExcelUtility to work with try-with-resources
    @Override
    public void close() throws IOException {
        closeWorkbook();
    }
}
