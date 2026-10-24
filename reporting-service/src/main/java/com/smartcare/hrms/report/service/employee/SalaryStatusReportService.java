package com.smartcare.hrms.report.service.employee;

import com.smartcare.hrms.report.bean.employee.SalaryStatusReportBean;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.modal.dto.employee.SalaryStatusReportDTO;
import com.smartcare.hrms.report.repo.SalaryStatusReportRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaryStatusReportService {
    @Autowired
    private SalaryStatusReportRepository repository;

    public List<SalaryStatusReportDTO> getSalaryStatusReport(EmployeeReposrtSearchDTO searchDTO) {

        List<SalaryStatusReportBean> list = repository.getSalaryStatusReport(searchDTO.getMonthYear(),searchDTO.getName(),searchDTO.getCode(),
                searchDTO.getCompanyId(), searchDTO.getBranchId(), searchDTO.getDepartmentId(), searchDTO.getDesignationId(), searchDTO.getCategoryId(),
                searchDTO.getWorkLocationId(), searchDTO.getGradeId());

        return list.stream().map(bean ->

                SalaryStatusReportDTO.builder()
                        .employeeId(bean.getEmployeeId())
                        .employeeCode(bean.getEmployeeCode())
                        .employeeName(bean.getEmployeeName())
                        .department(bean.getDepartment())
                        .designation(bean.getDesignation())
                        .grossSalary(bean.getGrossSalary())
                        .deductions(bean.getDeductions())
                        .netSalary(bean.getNetSalary())
                        .salaryStatus(bean.getSalaryStatus())
                        .attendanceStatus(bean.getAttendanceStatus())
                        .salaryMonth(bean.getSalaryMonth())
                        .paymentDate(bean.getPaymentDate())
                        .paymentMode(bean.getPaymentMode())
                        .build()

        ).toList();
    }

    public void exportSalaryStatusExcel(EmployeeReposrtSearchDTO searchDTO, HttpServletResponse response) throws Exception {

        List<SalaryStatusReportDTO> data = getSalaryStatusReport(searchDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Salary Status Report");

        CellStyle headerStyle = workbook.createCellStyle();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row header = sheet.createRow(0);

        int col = 0;

        createHeaderCell(header, col++, "Sr No", headerStyle);
        createHeaderCell(header, col++, "Employee Code", headerStyle);
        createHeaderCell(header, col++, "Employee Name", headerStyle);
        createHeaderCell(header, col++, "Department", headerStyle);
        createHeaderCell(header, col++, "Designation", headerStyle);
        createHeaderCell(header, col++, "Gross Salary", headerStyle);
        createHeaderCell(header, col++, "Deductions", headerStyle);
        createHeaderCell(header, col++, "Net Salary", headerStyle);
        createHeaderCell(header, col++, "Salary Status", headerStyle);
        createHeaderCell(header, col++, "Attendance Status", headerStyle);
        createHeaderCell(header, col++, "Salary Month", headerStyle);
        createHeaderCell(header, col++, "Payment Date", headerStyle);
        createHeaderCell(header, col++, "Payment Mode", headerStyle);

        int rowIndex = 1;
        int srNo = 1;

        for (SalaryStatusReportDTO report : data) {

            Row row = sheet.createRow(rowIndex++);
            col = 0;

            row.createCell(col++).setCellValue(srNo++);
            row.createCell(col++).setCellValue(report.getEmployeeCode() != null ? report.getEmployeeCode() : "");
            row.createCell(col++).setCellValue(report.getEmployeeName() != null ? report.getEmployeeName() : "");
            row.createCell(col++).setCellValue(report.getDepartment() != null ? report.getDepartment() : "");
            row.createCell(col++).setCellValue(report.getDesignation() != null ? report.getDesignation() : "");
            row.createCell(col++).setCellValue(report.getGrossSalary() != null ? report.getGrossSalary() : 0);
            row.createCell(col++).setCellValue(report.getDeductions() != null ? report.getDeductions() : 0);
            row.createCell(col++).setCellValue(report.getNetSalary() != null ? report.getNetSalary() : 0);
            row.createCell(col++).setCellValue(report.getSalaryStatus() != null ? report.getSalaryStatus() : "");
            row.createCell(col++).setCellValue(report.getAttendanceStatus() != null ? report.getAttendanceStatus() : "");
            row.createCell(col++).setCellValue(report.getSalaryMonth() != null ? report.getSalaryMonth() : "");
            row.createCell(col++).setCellValue(report.getPaymentDate() != null ? report.getPaymentDate() : "");
            row.createCell(col++).setCellValue(report.getPaymentMode() != null ? report.getPaymentMode() : "");
        }

        for (int i = 0; i < 13; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=salary_status_report.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void createHeaderCell(Row row, int col, String value, CellStyle style) {

        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
