package com.smartcare.hrms.migration.service.attendance;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AttendanceExcelHelper {
    public static final String SHEET_ATTENDANCE = "Attendance Upload";

    public static boolean isValidExcelFile(org.springframework.web.multipart.MultipartFile file) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                .equals(file.getContentType());
    }

    // ─── Template Generator ───────────────────────────────────────────────────────

    public static byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            buildSheet(workbook, SHEET_ATTENDANCE,
                    new String[]{
                            "Employee Code",
                            "Attendance Date",
                            "In Time",
                            "Out Time"
                    },
                    new String[]{
                            "EMP001",
                            "2026-04-01",
                            "09:00:00",
                            "18:00:00"
                    },
                    new int[]{ 1 }   // column index 1 = "Attendance Date" → force Text
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Sheet Builder ────────────────────────────────────────────────────────────

    private static void buildSheet(Workbook workbook, String sheetName,
                                   String[] headers, String[] sample,
                                   int[] textColumns) {
        Sheet sheet = workbook.createSheet(sheetName);

        // ── Text Cell Style (forces Excel to treat cell as plain text) ────────
        CellStyle textStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        textStyle.setDataFormat(dataFormat.getFormat("@"));  // "@" = Text format in Excel

        // ── Header Style ──────────────────────────────────────────────────────
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setWrapText(true);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        // ── Sample Style ──────────────────────────────────────────────────────
        CellStyle sampleStyle = workbook.createCellStyle();
        sampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        sampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sampleStyle.setBorderBottom(BorderStyle.THIN);
        sampleStyle.setBorderTop(BorderStyle.THIN);
        sampleStyle.setBorderLeft(BorderStyle.THIN);
        sampleStyle.setBorderRight(BorderStyle.THIN);
        sampleStyle.setAlignment(HorizontalAlignment.CENTER);
        Font sampleFont = workbook.createFont();
        sampleFont.setItalic(true);
        sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        sampleStyle.setFont(sampleFont);

        // ── Sample Style with Text format (for date column) ───────────────────
        CellStyle sampleTextStyle = workbook.createCellStyle();
        sampleTextStyle.cloneStyleFrom(sampleStyle);
        sampleTextStyle.setDataFormat(dataFormat.getFormat("@")); // force Text

        // ── Apply Text format to entire date column(s) ────────────────────────
        Set<Integer> textColSet = new java.util.HashSet<>();
        for (int col : textColumns) {
            textColSet.add(col);
            sheet.setDefaultColumnStyle(col, textStyle); // entire column = Text
        }

        // ── Header Row ────────────────────────────────────────────────────────
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(40);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        // ── Sample Row ────────────────────────────────────────────────────────
        Row sampleRow = sheet.createRow(1);
        for (int i = 0; i < sample.length; i++) {
            Cell cell = sampleRow.createCell(i);
            cell.setCellValue(sample[i]);
            // use text style for date columns, normal sample style for others
            cell.setCellStyle(textColSet.contains(Integer.valueOf(i))? sampleTextStyle : sampleStyle);
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