package com.smartcare.hrms.report.service.payroll;

import com.smartcare.hrms.common.bean.payroll.SalaryReportBean;
import com.smartcare.hrms.common.repository.payroll.salary.EmpMonthlyPayrollSummaryRepository;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeSearchDTO;
import com.smartcare.hrms.report.modal.dto.payroll.EmployeeSalaryReportDTO;
import com.smartcare.hrms.report.modal.dto.payroll.SalaryComponentDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeSalaryReportService {

    private @Autowired EmpMonthlyPayrollSummaryRepository summaryRepository;

    public List<EmployeeSalaryReportDTO> getSalaryReport(EmployeeSearchDTO searchDTO){

        List<SalaryReportBean> list= summaryRepository.fetchSalaryReport(searchDTO.getMonthYear(),searchDTO.getName(),searchDTO.getCode(),
                searchDTO.getCompanyId(), searchDTO.getBranchId(), searchDTO.getDepartmentId(), searchDTO.getDesignationId(), searchDTO.getCategoryId(),
                searchDTO.getWorkLocationId(), searchDTO.getGradeId());

        Map<String, List<SalaryReportBean>> grouped=list.stream().collect(Collectors.groupingBy(SalaryReportBean::getEmployeeId));

        return grouped.values().stream().map(rows ->{
            SalaryReportBean first= rows.get(0);

            List<SalaryComponentDTO> earning= rows.stream().filter(r->"EARNINGS".equals(r.getComponentType()))
                    .map(r ->SalaryComponentDTO.builder()
                            .seq(r.getComponentSeq())
                            .componentName(r.getComponentName())
                            .componentCode(r.getComponentCode())
                            .componentType(r.getComponentType())
                            .calculationType(r.getCalculationType())
                            .amount(r.getComponentAmount())
                            .build()).toList();

            List<SalaryComponentDTO> deduction= rows.stream().filter(r->"DEDUCTIONS".equals(r.getComponentType()))
                    .map(r ->SalaryComponentDTO.builder()
                            .seq(r.getComponentSeq())
                            .componentName(r.getComponentName())
                            .componentCode(r.getComponentCode())
                            .componentType(r.getComponentType())
                            .calculationType(r.getCalculationType())
                            .amount(r.getComponentAmount())
                            .build()).toList();

            return EmployeeSalaryReportDTO.builder()
                    .employeeName(first.getEmployeeName())
                    .employeeCode(first.getEmployeeCode())
                    .monthYear(first.getMonthYear())
                    .payableDays(first.getPayableDays())
                    .lopDays(first.getLopDays())
                    .totalEarnings(first.getTotalEarnings())
                    .totalStdDeductions(first.getTotalStdDeductions())
                    .totalOtherDeductions(first.getTotalOtherDeductions())
                    .totalPayableEarnings(first.getTotalPayableEarnings())
                    .totalPayableStdDeductions(first.getTotalPayableStdDeductions())
                    .totalPayableOtherDeductions(first.getTotalPayableOtherDeductions())
                    .netPayable(first.getNetPayable())
                    .grossAmount(first.getGrossAmount())
                    .stdDeductionAmount(first.getStdDeductionAmount())
                    .otherDeductionAmount(first.getOtherDeductionAmount())
                    .netAmount(first.getNetAmount())
                    .earnings(earning)
                    .deductions(deduction)
                    .build();

        }).toList();
    }


    public void exportSalaryExcel(EmployeeSearchDTO searchDTO, HttpServletResponse response) throws Exception {

        List<EmployeeSalaryReportDTO> data = getSalaryReport(searchDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Salary Report");

        // ── Styles ────────────────────────────────────────────────
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle earningHeaderStyle = workbook.createCellStyle();
        Font earningFont = workbook.createFont();
        earningFont.setBold(true);
        earningFont.setColor(IndexedColors.WHITE.getIndex());
        earningHeaderStyle.setFont(earningFont);
        earningHeaderStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        earningHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        earningHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle deductionHeaderStyle = workbook.createCellStyle();
        Font deductionFont = workbook.createFont();
        deductionFont.setBold(true);
        deductionFont.setColor(IndexedColors.WHITE.getIndex());
        deductionHeaderStyle.setFont(deductionFont);
        deductionHeaderStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        deductionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        deductionHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle amountStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("#,##0.00"));
        amountStyle.setAlignment(HorizontalAlignment.RIGHT);

        // ── Get unique component names in order ───────────────────
        List<String> earningNames = data.stream()
                .flatMap(e -> e.getEarnings().stream())
                .sorted(Comparator.comparing(SalaryComponentDTO::getSeq))
                .map(SalaryComponentDTO::getComponentName)
                .distinct()
                .collect(Collectors.toList());

        List<String> deductionNames = data.stream()
                .flatMap(e -> e.getDeductions().stream())
                .sorted(Comparator.comparing(SalaryComponentDTO::getSeq))
                .map(SalaryComponentDTO::getComponentName)
                .distinct()
                .collect(Collectors.toList());

        // ── Header Row ────────────────────────────────────────────
        Row header = sheet.createRow(0);
        int col = 0;

        // Fixed headers
        createHeaderCell(header, col++, "Sr No",          headerStyle);
        createHeaderCell(header, col++, "Emp Code",       headerStyle);
        createHeaderCell(header, col++, "Employee Name",  headerStyle);
        createHeaderCell(header, col++, "Month Year",     headerStyle);
        createHeaderCell(header, col++, "Payable Days",   headerStyle);
        createHeaderCell(header, col++, "LOP Days",       headerStyle);
        createHeaderCell(header, col++, "Gross Amount",   headerStyle);

        // Earning component headers
        for (String name : earningNames) {
            createHeaderCell(header, col++, name, earningHeaderStyle);
        }
        createHeaderCell(header, col++, "Total Earnings", earningHeaderStyle);

        // Deduction component headers
        for (String name : deductionNames) {
            createHeaderCell(header, col++, name, deductionHeaderStyle);
        }
        createHeaderCell(header, col++, "Total Deductions", deductionHeaderStyle);

        // Net payable header
        createHeaderCell(header, col++, "Net Payable", headerStyle);

        // ── Data Rows ─────────────────────────────────────────────
        int rowIndex = 1;
        int srNo = 1;

        for (EmployeeSalaryReportDTO emp : data) {
            Row row = sheet.createRow(rowIndex++);
            col = 0;

            // Fixed data
            row.createCell(col++).setCellValue(srNo++);
            row.createCell(col++).setCellValue(emp.getEmployeeCode()  != null ? emp.getEmployeeCode()  : "");
            row.createCell(col++).setCellValue(emp.getEmployeeName()  != null ? emp.getEmployeeName()  : "");
            row.createCell(col++).setCellValue(emp.getMonthYear()     != null ? emp.getMonthYear()     : "");
            row.createCell(col++).setCellValue(emp.getPayableDays()   != null ? emp.getPayableDays()   : 0);
            row.createCell(col++).setCellValue(emp.getLopDays()       != null ? emp.getLopDays()       : 0);

            Cell grossCell = row.createCell(col++);
            grossCell.setCellValue(emp.getGrossAmount() != null ? emp.getGrossAmount().doubleValue() : 0);
            grossCell.setCellStyle(amountStyle);

            // Earnings map
            Map<String, BigDecimal> earningsMap = emp.getEarnings().stream()
                    .collect(Collectors.toMap(
                            SalaryComponentDTO::getComponentName,
                            SalaryComponentDTO::getAmount,
                            (a, b) -> a));

            for (String name : earningNames) {
                Cell c = row.createCell(col++);
                c.setCellValue(earningsMap.getOrDefault(name, BigDecimal.ZERO).doubleValue());
                c.setCellStyle(amountStyle);
            }

            Cell totEarnCell = row.createCell(col++);
            totEarnCell.setCellValue(emp.getTotalPayableEarnings() != null ? emp.getTotalPayableEarnings().doubleValue() : 0);
            totEarnCell.setCellStyle(amountStyle);

            // Deductions map
            Map<String, BigDecimal> deductionsMap = emp.getDeductions().stream()
                    .collect(Collectors.toMap(
                            SalaryComponentDTO::getComponentName,
                            SalaryComponentDTO::getAmount,
                            (a, b) -> a));

            for (String name : deductionNames) {
                Cell c = row.createCell(col++);
                c.setCellValue(deductionsMap.getOrDefault(name, BigDecimal.ZERO).doubleValue());
                c.setCellStyle(amountStyle);
            }

            BigDecimal totalDed = (emp.getTotalPayableStdDeductions()  != null ? emp.getTotalPayableStdDeductions()  : BigDecimal.ZERO)
                    .add(emp.getTotalPayableOtherDeductions() != null ? emp.getTotalPayableOtherDeductions() : BigDecimal.ZERO);
            Cell totDedCell = row.createCell(col++);
            totDedCell.setCellValue(totalDed.doubleValue());
            totDedCell.setCellStyle(amountStyle);

            Cell netCell = row.createCell(col++);
            netCell.setCellValue(emp.getNetPayable() != null ? emp.getNetPayable().doubleValue() : 0);
            netCell.setCellStyle(amountStyle);
        }

        // ── Auto size all columns ─────────────────────────────────
        int totalCols = 7 + earningNames.size() + 1 + deductionNames.size() + 1 + 1;
        for (int i = 0; i < totalCols; i++) {
            sheet.autoSizeColumn(i);
        }

        // ── Write to response — same as LeaveReportService ────────
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=salary_report_" + searchDTO.getMonthYear() + ".xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ── Helper ────────────────────────────────────────────────────
    private void createHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
