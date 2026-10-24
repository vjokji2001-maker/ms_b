package com.smartcare.hrms.migration.service.matser.employeement;


import com.smartcare.hrms.migration.dto.master.employment.BranchUploadDTO;
import com.smartcare.hrms.migration.dto.master.employment.CompanyUploadDTO;
import com.smartcare.hrms.migration.dto.master.employment.MasterUploadDTO;
import com.smartcare.hrms.migration.dto.master.employment.SubWorkLocationUploadDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombinedMasterExcelHelper {


    public static final String SHEET_CATEGORY         = "Category";
    public static final String SHEET_GRADE            = "Grade";
    public static final String SHEET_DEPARTMENT       = "Department";
    public static final String SHEET_DESIGNATION      = "Designation";
    public static final String SHEET_WORK_LOCATION    = "WorkLocation";
    public static final String SHEET_SUB_WORK_LOCATION = "SubWorkLocation";
    public static final String SHEET_BRANCH           = "Branch";
    public static final String SHEET_COMPANY          = "Company";




    public static boolean isValidExcelFile(MultipartFile file) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                .equals(file.getContentType());
    }

    // ─── Single generic parser for all simple name+code sheets ───────────────────

    public static List<MasterUploadDTO> parseSimple(Workbook workbook, String sheetName) {
        List<MasterUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(MasterUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .build());
        }
        return list;
    }

    // ─── SubWorkLocation parser ───────────────────────────────────────────────────

    public static List<SubWorkLocationUploadDTO> parseSubWorkLocation(Workbook workbook) {
        List<SubWorkLocationUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_SUB_WORK_LOCATION);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(SubWorkLocationUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .workLocationCode(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }

    // ─── Branch parser ────────────────────────────────────────────────────────────

    public static List<BranchUploadDTO> parseBranch(Workbook workbook) {
        List<BranchUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_BRANCH);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(BranchUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .address(fmt.formatCellValue(row.getCell(2)).trim())
                    .countryName(fmt.formatCellValue(row.getCell(3)).trim())
                    .stateName(fmt.formatCellValue(row.getCell(4)).trim())
                    .cityName(fmt.formatCellValue(row.getCell(5)).trim())
                    .build());
        }
        return list;
    }

    // ─── Company parser ───────────────────────────────────────────────────────────

    public static List<CompanyUploadDTO> parseCompany(Workbook workbook) {
        List<CompanyUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_COMPANY);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(CompanyUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .registeredName(fmt.formatCellValue(row.getCell(2)).trim())
                    .registeredNumber(fmt.formatCellValue(row.getCell(3)).trim())
                    .incorporationDate(fmt.formatCellValue(row.getCell(4)).trim())
                    .panNumber(fmt.formatCellValue(row.getCell(5)).trim())
                    .tanNumber(fmt.formatCellValue(row.getCell(6)).trim())
                    .cinNumber(fmt.formatCellValue(row.getCell(7)).trim())
                    .gstin(fmt.formatCellValue(row.getCell(8)).trim())
                    .ptRegNumber(fmt.formatCellValue(row.getCell(9)).trim())
                    .pfRegistrationNumber(fmt.formatCellValue(row.getCell(10)).trim())
                    .esicRegNumber(fmt.formatCellValue(row.getCell(11)).trim())
                    .msmeRegNo(fmt.formatCellValue(row.getCell(12)).trim())
                    .iecCode(fmt.formatCellValue(row.getCell(13)).trim())
                    .ptZone(fmt.formatCellValue(row.getCell(14)).trim())
                    .pfZone(fmt.formatCellValue(row.getCell(15)).trim())
                    .esicZone(fmt.formatCellValue(row.getCell(16)).trim())
                    .industryType(fmt.formatCellValue(row.getCell(17)).trim())
                    .address(fmt.formatCellValue(row.getCell(18)).trim())
                    .countryName(fmt.formatCellValue(row.getCell(19)).trim())
                    .stateName(fmt.formatCellValue(row.getCell(20)).trim())
                    .cityName(fmt.formatCellValue(row.getCell(21)).trim())
                    .pincode(fmt.formatCellValue(row.getCell(22)).trim())
                    .contactPersonName(fmt.formatCellValue(row.getCell(23)).trim())
                    .designationCode(fmt.formatCellValue(row.getCell(24)).trim())
                    .contactPersonEmail(fmt.formatCellValue(row.getCell(25)).trim())
                    .countryCode(fmt.formatCellValue(row.getCell(26)).trim())
                    .phone(fmt.formatCellValue(row.getCell(27)).trim())
                    .email(fmt.formatCellValue(row.getCell(28)).trim())
                    .website(fmt.formatCellValue(row.getCell(29)).trim())
                    .twitter(fmt.formatCellValue(row.getCell(30)).trim())
                    .facebook(fmt.formatCellValue(row.getCell(31)).trim())
                    .instagram(fmt.formatCellValue(row.getCell(32)).trim())
                    .build());
        }
        return list;
    }

    // ─── Template Generator ───────────────────────────────────────────────────────

    public static byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            buildSheet(workbook, SHEET_CATEGORY,
                    new String[]{"Category Name", "Category Code"},
                    new String[]{"Management", "MGT"});

            buildSheet(workbook, SHEET_GRADE,
                    new String[]{"Garde Name", "Garde Code"},
                    new String[]{"Senior", "SR"});

            buildSheet(workbook, SHEET_DEPARTMENT,
                    new String[]{"Department Name", "Department Code"},
                    new String[]{"Human Resources", "HR"});

            buildSheet(workbook, SHEET_DESIGNATION,
                    new String[]{"Designation Name", "Designation Code"},
                    new String[]{"Software Engineer", "SE"});

            buildSheet(workbook, SHEET_WORK_LOCATION,
                    new String[]{"Work Location Name", "Work Location Code"},
                    new String[]{"Head Office", "HO"});

            buildSheet(workbook, SHEET_SUB_WORK_LOCATION,
                    new String[]{"Sub Work Location Name", "Sub Work Location Code", "Work Location Code (from WorkLocation sheet)"},
                    new String[]{"Floor 1", "FL1", "HO"});

            buildSheet(workbook, SHEET_BRANCH,
                    new String[]{"Branch Name", "Branch Code", "Address", "Country Name", "State Name", "City Name"},
                    new String[]{"Mumbai Branch", "MUM001", "123 Main St", "India", "Maharashtra", "Mumbai"});

            buildSheet(workbook, SHEET_COMPANY,
                    new String[]{
                            "Company Name", "Company Code", "Registered Name", "Registered Number",
                            "Incorporation Date (YYYY-MM-DD)", "PAN Number", "TAN Number",
                            "CIN Number", "GSTIN", "PT Reg Number", "PF Reg Number",
                            "ESIC Reg Number", "MSME Reg No", "IEC Code", "PT Zone",
                            "PF Zone", "ESIC Zone", "Industry Type", "Address",
                            "Country Name", "State Name", "City Name", "Pincode",
                            "Contact Person Name", "Designation Code (from Designation sheet)",
                            "Contact Person Email", "Country Code", "Phone", "Email",
                            "Website", "Twitter", "Facebook", "Instagram"
                    },
                    new String[]{
                            "Smartcare Pvt Ltd", "SC001", "Smartcare Private Limited", "REG12345",
                            "2010-06-15", "ABCDE1234F", "ABCDE12345A", "U12345MH2010PTC12345",
                            "27ABCDE1234F1Z5", "PT123", "PF123456", "ESIC123456", "MSME123",
                            "IEC123", "Zone A", "Zone B", "Zone C", "IT Services",
                            "123 Main Street", "India", "Maharashtra", "Mumbai", "400001",
                            "John Doe", "SE", "john@smartcare.com", "+91", "9876543210",
                            "info@smartcare.com", "https://smartcare.com", "@smartcare",
                            "smartcare", "smartcare"
                    });

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Shared Sheet Builder ─────────────────────────────────────────────────────

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

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }


}
