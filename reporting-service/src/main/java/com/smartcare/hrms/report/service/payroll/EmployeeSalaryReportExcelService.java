package com.smartcare.hrms.report.service.payroll;

import com.smartcare.hrms.report.modal.dto.payroll.EmployeeSalaryReportDTO;
import com.smartcare.hrms.report.modal.dto.payroll.SalaryComponentDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeSalaryReportExcelService {

    public byte[] generateSalaryReport(List<EmployeeSalaryReportDTO> reportData, String monthYear) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Salary Report - " + monthYear);
            sheet.setDefaultColumnWidth(18);

            // ── Styles ──────────────────────────────────────────
            CellStyle titleStyle      = createTitleStyle(workbook);
            CellStyle headerStyle     = createHeaderStyle(workbook);
            CellStyle subHeaderStyle  = createSubHeaderStyle(workbook);
            CellStyle dataStyle       = createDataStyle(workbook);
            CellStyle amountStyle     = createAmountStyle(workbook);
            CellStyle earningStyle    = createEarningStyle(workbook);
            CellStyle deductionStyle  = createDeductionStyle(workbook);
            CellStyle totalStyle      = createTotalStyle(workbook);

            int rowNum = 0;

            // ── Title Row ────────────────────────────────────────
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SALARY REPORT — " + monthYear);
            titleCell.setCellStyle(titleStyle);

            // collect all unique component names in order
            List<String> allEarningNames   = getUniqueComponents(reportData, "EARNINGS");
            List<String> allDeductionNames = getUniqueComponents(reportData, "DEDUCTIONS");

            int totalCols = 7 + allEarningNames.size() + allDeductionNames.size() + 4;
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));

            rowNum++; // blank row

            // ── Main Header Row ───────────────────────────────────
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);
            int col = 0;

            // Fixed headers
            createCell(headerRow, col++, "Sr No",           headerStyle);
            createCell(headerRow, col++, "Emp Code",        headerStyle);
            createCell(headerRow, col++, "Employee Name",   headerStyle);
            createCell(headerRow, col++, "Month",           headerStyle);
            createCell(headerRow, col++, "Payable Days",    headerStyle);
            createCell(headerRow, col++, "LOP Days",        headerStyle);
            createCell(headerRow, col++, "Gross Amount",    headerStyle);

            // Earning component headers
            for (String name : allEarningNames) {
                createCell(headerRow, col++, name, earningStyle);
            }
            createCell(headerRow, col++, "Total Earnings",  earningStyle);

            // Deduction component headers
            for (String name : allDeductionNames) {
                createCell(headerRow, col++, name, deductionStyle);
            }
            createCell(headerRow, col++, "Total Deductions", deductionStyle);

            // Summary headers
            createCell(headerRow, col++, "Net Payable",     totalStyle);

            // ── Data Rows ─────────────────────────────────────────
            int srNo = 1;
            for (EmployeeSalaryReportDTO emp : reportData) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(18);
                col = 0;

                // Fixed data
                createCell(row, col++, srNo++,                          dataStyle);
                createCell(row, col++, emp.getEmployeeCode(),           dataStyle);
                createCell(row, col++, emp.getEmployeeName(),           dataStyle);
                createCell(row, col++, emp.getMonthYear(),              dataStyle);
                createCell(row, col++, emp.getPayableDays(),            dataStyle);
                createCell(row, col++, emp.getLopDays(),                dataStyle);
                createCell(row, col++, emp.getGrossAmount(),            amountStyle);

                // Earnings — match by component name
                Map<String, BigDecimal> earningsMap = emp.getEarnings().stream()
                        .collect(Collectors.toMap(
                                SalaryComponentDTO::getComponentName,
                                SalaryComponentDTO::getAmount,
                                (a, b) -> a));

                for (String name : allEarningNames) {
                    BigDecimal amt = earningsMap.getOrDefault(name, BigDecimal.ZERO);
                    createCell(row, col++, amt, amountStyle);
                }
                createCell(row, col++, emp.getTotalPayableEarnings(), amountStyle);

                // Deductions — match by component name
                Map<String, BigDecimal> deductionsMap = emp.getDeductions().stream()
                        .collect(Collectors.toMap(
                                SalaryComponentDTO::getComponentName,
                                SalaryComponentDTO::getAmount,
                                (a, b) -> a));

                for (String name : allDeductionNames) {
                    BigDecimal amt = deductionsMap.getOrDefault(name, BigDecimal.ZERO);
                    createCell(row, col++, amt, amountStyle);
                }
                createCell(row, col++, emp.getTotalPayableOtherDeductions()
                        .add(emp.getTotalPayableStdDeductions()), amountStyle);

                // Net Payable
                createCell(row, col++, emp.getNetPayable(), totalStyle);

                sheet.setAutoFilter(new CellRangeAddress(
                        headerRow.getRowNum(),  // header row index
                        headerRow.getRowNum(),  // same row (just the header)
                        0,                      // first column (Sr No)
                        col - 1                 // last column (Net Payable)
                ));

            }

            // ── Totals Row ────────────────────────────────────────
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.setHeightInPoints(20);
            col = 0;
            createCell(totalRow, col++, "TOTAL", totalStyle);
            createCell(totalRow, col++, "",      totalStyle);
            createCell(totalRow, col++, "",      totalStyle);
            createCell(totalRow, col++, "",      totalStyle);
            createCell(totalRow, col++, "",      totalStyle);
            createCell(totalRow, col++, "",      totalStyle);

            // Gross total
            BigDecimal grossTotal = reportData.stream()
                    .map(EmployeeSalaryReportDTO::getGrossAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            createCell(totalRow, col++, grossTotal, totalStyle);

            // Earning component totals
            for (String name : allEarningNames) {
                BigDecimal compTotal = reportData.stream()
                        .flatMap(e -> e.getEarnings().stream())
                        .filter(c -> c.getComponentName().equals(name))
                        .map(SalaryComponentDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                createCell(totalRow, col++, compTotal, totalStyle);
            }
            BigDecimal totalEarnings = reportData.stream()
                    .map(EmployeeSalaryReportDTO::getTotalPayableEarnings)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            createCell(totalRow, col++, totalEarnings, totalStyle);

            // Deduction component totals
            for (String name : allDeductionNames) {
                BigDecimal compTotal = reportData.stream()
                        .flatMap(e -> e.getDeductions().stream())
                        .filter(c -> c.getComponentName().equals(name))
                        .map(SalaryComponentDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                createCell(totalRow, col++, compTotal, totalStyle);
            }
            BigDecimal totalDeductions = reportData.stream()
                    .map(e -> e.getTotalPayableStdDeductions().add(e.getTotalPayableOtherDeductions()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            createCell(totalRow, col++, totalDeductions, totalStyle);

            BigDecimal netTotal = reportData.stream()
                    .map(EmployeeSalaryReportDTO::getNetPayable)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            createCell(totalRow, col++, netTotal, totalStyle);

            // ── Auto size fixed columns ───────────────────────────
            for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
            sheet.setColumnWidth(2, 6000); // Employee Name wider

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate salary report", e);
        }
    }

    // ── Helper: get unique component names in order ───────────────
    private List<String> getUniqueComponents(List<EmployeeSalaryReportDTO> data, String type) {
        return data.stream()
                .flatMap(e -> "EARNINGS".equals(type)
                        ? e.getEarnings().stream()
                        : e.getDeductions().stream())
                .sorted(Comparator.comparing(SalaryComponentDTO::getSeq))
                .map(SalaryComponentDTO::getComponentName)
                .distinct()
                .collect(Collectors.toList());
    }

    // ── Cell creators ─────────────────────────────────────────────
    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof String)     cell.setCellValue((String) value);
        else if (value instanceof Integer)    cell.setCellValue((Integer) value);
        else if (value instanceof Double)     cell.setCellValue((Double) value);
        else if (value instanceof BigDecimal) cell.setCellValue(((BigDecimal) value).doubleValue());
        else if (value != null)               cell.setCellValue(value.toString());
        cell.setCellStyle(style);
    }
    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb){
        CellStyle style=wb.createCellStyle();
        Font font=wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook wb){
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }
    private CellStyle createEarningStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createDeductionStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createAmountStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(style);
        return style;
    }

    private CellStyle createTotalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setColor(IndexedColors.WHITE.getIndex());
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(style);
        return style;
    }
    private void setBorder(CellStyle style){
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

    }
}
