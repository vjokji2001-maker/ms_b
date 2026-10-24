package com.smartcare.hrms.migration.service.employee;

import com.smartcare.hrms.migration.dto.employee.EmployeeUploadDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeExcelHelper {


    public static final String SHEET_EMPLOYEE = "Employee";

    public static boolean isValidExcelFile(MultipartFile file) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                .equals(file.getContentType());
    }

    // ─── Employee Parser ──────────────────────────────────────────────────────────

    public static List<EmployeeUploadDTO> parseEmployee(Workbook workbook) {
        List<EmployeeUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_EMPLOYEE);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(EmployeeUploadDTO.builder()
                    .salutation(fmt.formatCellValue(row.getCell(0)).trim())
                    .firstName(fmt.formatCellValue(row.getCell(1)).trim())
                    .middleName(fmt.formatCellValue(row.getCell(2)).trim())
                    .lastName(fmt.formatCellValue(row.getCell(3)).trim())
                    .formalName(fmt.formatCellValue(row.getCell(4)).trim())
                    .gender(fmt.formatCellValue(row.getCell(5)).trim())
                    .dob(fmt.formatCellValue(row.getCell(6)).trim())
                    .maritalStatus(fmt.formatCellValue(row.getCell(7)).trim())
                    .contactCountryCode(fmt.formatCellValue(row.getCell(8)).trim())
                    .contactNumber(fmt.formatCellValue(row.getCell(9)).trim())
                    .aadhaarNumber(fmt.formatCellValue(row.getCell(10)).trim())
                    .nationality(fmt.formatCellValue(row.getCell(11)).trim())
                    .birthCountryName(fmt.formatCellValue(row.getCell(12)).trim())
                    .stateName(fmt.formatCellValue(row.getCell(13)).trim())
                    .cityName(fmt.formatCellValue(row.getCell(14)).trim())
                    .companyCode(fmt.formatCellValue(row.getCell(15)).trim())
                    .branchCode(fmt.formatCellValue(row.getCell(16)).trim())
                    .designationCode(fmt.formatCellValue(row.getCell(17)).trim())
                    .departmentCode(fmt.formatCellValue(row.getCell(18)).trim())
                    .workLocationCode(fmt.formatCellValue(row.getCell(19)).trim())
                    .subWorkLocationCode(fmt.formatCellValue(row.getCell(20)).trim())
                    .gradeCode(fmt.formatCellValue(row.getCell(21)).trim())
                    .categoryCode(fmt.formatCellValue(row.getCell(22)).trim())   // BUG FIX: was cell(21), same as gradeName
                    .status(fmt.formatCellValue(row.getCell(23)).trim())         // BUG FIX: shifted by 1
                    .empCode(fmt.formatCellValue(row.getCell(24)).trim())        // BUG FIX: shifted by 1
                    .biometricCode(fmt.formatCellValue(row.getCell(25)).trim())  // BUG FIX: shifted by 1
                    .joiningDate(fmt.formatCellValue(row.getCell(26)).trim())    // BUG FIX: shifted by 1
                    .contractStartDate(fmt.formatCellValue(row.getCell(27)).trim()) // BUG FIX: shifted by 1
                    .build());
        }
        return list;
    }

    // ─── Template Generator ───────────────────────────────────────────────────────

    public static byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            buildSheet(workbook, SHEET_EMPLOYEE,
                    new String[]{
                            "Salutation",
                            "First Name",
                            "Middle Name",
                            "Last Name",
                            "Formal Name",
                            "Gender",
                            "Date of Birth (YYYY-MM-DD)",
                            "Marital Status",
                            "Contact Country Code",
                            "Contact Number",
                            "Aadhaar Number",
                            "Nationality",
                            "Birth Country Name",
                            "State Name",
                            "City Name",
                            "Company Code",
                            "Branch Code",
                            "Designation Code",
                            "Department Code",
                            "Work Location Code",
                            "Sub Work Location Code",
                            "Grade Code",
                            "Category Code",
                            "Status",
                            "Employee Code",
                            "Biometric Code",
                            "Joining Date (YYYY-MM-DD)",
                            "Contract Start Date (YYYY-MM-DD)"
                    },
                    new String[]{
                            "Mr.",
                            "John",
                            "A",
                            "Doe",
                            "John A. Doe",
                            "Male",
                            "1990-05-15",
                            "Single",
                            "+91",
                            "9876543210",
                            "1234 5678 9012",
                            "Indian",
                            "India",
                            "Maharashtra",
                            "Mumbai",
                            "SC001",
                            "MUM001",
                            "SE",
                            "HR",
                            "HO",
                            "FL1",
                            "SR",
                            "MGT2",
                            "Active",
                            "EMP001",
                            "BIO001",
                            "2022-01-10",
                            "2022-01-10"
                    });

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Sheet Builder ────────────────────────────────────────────────────────────

    private static void buildSheet(Workbook workbook, String sheetName, String[] headers, String[] sample) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        CellStyle sampleStyle = workbook.createCellStyle();
        sampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        sampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sampleStyle.setBorderBottom(BorderStyle.THIN);
        sampleStyle.setBorderTop(BorderStyle.THIN);
        sampleStyle.setBorderLeft(BorderStyle.THIN);
        sampleStyle.setBorderRight(BorderStyle.THIN);
        Font sampleFont = workbook.createFont();
        sampleFont.setItalic(true);
        sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        sampleStyle.setFont(sampleFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 7000);
        }

        Row sampleRow = sheet.createRow(1);
        for (int i = 0; i < sample.length; i++) {
            Cell cell = sampleRow.createCell(i);
            cell.setCellValue(sample[i]);
            cell.setCellStyle(sampleStyle);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}

