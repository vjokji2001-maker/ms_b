package com.smartcare.hrms.migration.helper.attendance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.Year;
import java.util.stream.IntStream;

public class AttendanceTemplateGenerator {

    private AttendanceTemplateGenerator() {}

    /**
     * Builds an .xlsx template with:
     *  - Styled header row
     *  - Month dropdown (1–12) validation on column B
     *  - Year dropdown (current ± 1) validation on column C
     *  - 10 blank sample rows
     *  - Auto-sized columns
     */
    public static byte[] generate() throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Attendance");

            // ── Header style ──────────────────────────────────────────────
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)31, (byte)78, (byte)121}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);

            // ── Data style ────────────────────────────────────────────────
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // ── Header row (row 0) ────────────────────────────────────────
            String[] headers = {
                    "Employee Code",
                    "Month (1-12)",
                    "Year",
                    "Present Days",
                    "Absent Days",
                    "Week Off Days"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Sample data rows (rows 1–10) ──────────────────────────────
            int currentYear = Year.now().getValue();
            String[] sampleCodes = {"EMP001", "EMP002", "EMP003"};

            for (int r = 1; r <= 10; r++) {
                Row row = sheet.createRow(r);
                row.setHeightInPoints(16);

                // Employee Code (sample for first 3 rows, blank the rest)
                Cell codeCell = row.createCell(0);
                if (r <= sampleCodes.length) codeCell.setCellValue(sampleCodes[r - 1]);
                codeCell.setCellStyle(dataStyle);

                // Month
                Cell monthCell = row.createCell(1);
                if (r <= sampleCodes.length) monthCell.setCellValue(Month.from(java.time.LocalDate.now()).getValue());
                monthCell.setCellStyle(dataStyle);

                // Year
                Cell yearCell = row.createCell(2);
                if (r <= sampleCodes.length) yearCell.setCellValue(currentYear);
                yearCell.setCellStyle(dataStyle);

                // Present / Absent / WeekOff — blank, user fills in
                for (int c = 3; c <= 5; c++) {
                    row.createCell(c).setCellStyle(dataStyle);
                }
            }

            // ── Dropdown: Month (1–12) on rows 1–500, col B ───────────────
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();

            String monthFormula = "\"1,2,3,4,5,6,7,8,9,10,11,12\"";
            DataValidationConstraint monthConstraint =
                    dvHelper.createExplicitListConstraint(
                            IntStream.rangeClosed(1, 12)
                                    .mapToObj(Integer::toString)
                                    .toArray(String[]::new));
            CellRangeAddressList monthRange = new CellRangeAddressList(1, 500, 1, 1);
            DataValidation monthValidation  = dvHelper.createValidation(monthConstraint, monthRange);
            monthValidation.setShowErrorBox(true);
            monthValidation.createErrorBox("Invalid Month", "Please enter a value between 1 and 12.");
            sheet.addValidationData(monthValidation);

            // ── Dropdown: Year (current-1, current, current+1) on rows 1–500, col C ──
            DataValidationConstraint yearConstraint =
                    dvHelper.createExplicitListConstraint(new String[]{
                            String.valueOf(currentYear - 1),
                            String.valueOf(currentYear),
                            String.valueOf(currentYear + 1)
                    });
            CellRangeAddressList yearRange    = new CellRangeAddressList(1, 500, 2, 2);
            DataValidation yearValidation     = dvHelper.createValidation(yearConstraint, yearRange);
            yearValidation.setShowErrorBox(true);
            yearValidation.createErrorBox("Invalid Year", "Please select a valid year.");
            sheet.addValidationData(yearValidation);

            // ── Numeric constraint on columns D, E, F (0–31) ─────────────
            for (int col = 3; col <= 5; col++) {
                DataValidationConstraint numConstraint =
                        dvHelper.createIntegerConstraint(
                                DataValidationConstraint.OperatorType.BETWEEN, "0", "31");
                CellRangeAddressList numRange    = new CellRangeAddressList(1, 500, col, col);
                DataValidation numValidation     = dvHelper.createValidation(numConstraint, numRange);
                numValidation.setShowErrorBox(true);
                numValidation.createErrorBox("Invalid Value", "Enter a number between 0 and 31.");
                sheet.addValidationData(numValidation);
            }

            // ── Auto-size columns ─────────────────────────────────────────
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add a little padding after auto-size
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            // ── Instructions sheet ────────────────────────────────────────
            XSSFSheet infoSheet = workbook.createSheet("Instructions");
            String[] instructions = {
                    "ATTENDANCE UPLOAD TEMPLATE — INSTRUCTIONS",
                    "",
                    "1. Do NOT delete or rename the 'Attendance' sheet.",
                    "2. Column A : Employee Code  (must match exactly what is in the system)",
                    "3. Column B : Month          (1 = January … 12 = December)",
                    "4. Column C : Year           (e.g. 2024)",
                    "5. Column D : Present Days",
                    "6. Column E : Absent Days",
                    "7. Column F : Week Off Days",
                    "",
                    "IMPORTANT: Present + Absent + Week Off MUST equal the total days in that month.",
                    "           e.g. For April 2024 (30 days): 22 + 4 + 4 = 30  ✓",
                    "",
                    "8. Remove the 3 sample rows before uploading.",
                    "9. Save as .xlsx before uploading."
            };

            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            for (int i = 0; i < instructions.length; i++) {
                Row r = infoSheet.createRow(i);
                Cell c = r.createCell(0);
                c.setCellValue(instructions[i]);
                if (i == 0) c.setCellStyle(titleStyle);
            }
            infoSheet.setColumnWidth(0, 10000);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
