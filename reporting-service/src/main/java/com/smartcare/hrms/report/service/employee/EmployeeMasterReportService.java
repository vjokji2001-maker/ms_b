package com.smartcare.hrms.report.service.employee;

import com.smartcare.hrms.report.bean.employee.EmployeeMasterReportBean;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeMasterReportDTO;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.repo.EmployeeMasterReportRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeMasterReportService {
    @Autowired
    private EmployeeMasterReportRepository repository;

    public List<EmployeeMasterReportDTO> getEmployeeMasterReport(EmployeeReposrtSearchDTO searchDTO) {

        List<EmployeeMasterReportBean> list = repository.getEmployeeMasterReport(searchDTO.getMonthYear(),searchDTO.getName(),searchDTO.getCode(),
                searchDTO.getCompanyId(), searchDTO.getBranchId(), searchDTO.getDepartmentId(), searchDTO.getDesignationId(), searchDTO.getCategoryId(),
                searchDTO.getWorkLocationId(), searchDTO.getGradeId());

        return list.stream().map(bean ->

                EmployeeMasterReportDTO.builder()
                        .employeeId(bean.getId())
                        .employeeCode(bean.getEmpCode())
                        .employeeName(bean.getFormalName())
                        .gender(bean.getGender())
                        .dob(bean.getDob())
                        .contactNumber(bean.getContactNumber())
                        .alternateNumber(bean.getAlternateNumber())
                        .formalEmail(bean.getFormalEmail())
                        .personalEmail(bean.getPersonalEmail())
                        .aadhaarNumber(bean.getAadhaarNumber())
                        .panNumber(bean.getPanNumber())
                        .nationality(bean.getNationality())
                        .bloodGroup(bean.getBloodGroup())
                        .joiningDate(bean.getJoiningDate())
                        .departmentName(bean.getDepartmentName())
                        .designationName(bean.getDesignationName())
                        .branchName(bean.getBranchName())
                        .workLocationName(bean.getWorkLocationName())
                        .categoryName(bean.getCategoryName())
                        .gradeName(bean.getGradeName())
                        .status(bean.getStatus())
                        .lastWorkingDate(bean.getLastWorkingDate())
                        .shiftId(bean.getShiftId())
                        .shiftStartTime(bean.getShiftStrtTime())
                        .shiftEndTime(bean.getShiftEndTime())
                        .graceTimeMins(bean.getGraceTimeMins())
                        .build()

        ).toList();
    }

    public void exportEmployeeMasterExcel(EmployeeReposrtSearchDTO searchDTO, HttpServletResponse response) throws Exception {

        List<EmployeeMasterReportDTO> data = getEmployeeMasterReport(searchDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Master Report");

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
        createHeaderCell(header, col++, "Gender", headerStyle);
        createHeaderCell(header, col++, "DOB", headerStyle);
        createHeaderCell(header, col++, "Mobile Number", headerStyle);
        createHeaderCell(header, col++, "Alternate Mobile", headerStyle);
        createHeaderCell(header, col++, "Official Email", headerStyle);
        createHeaderCell(header, col++, "Personal Email", headerStyle);
        createHeaderCell(header, col++, "Aadhaar Number", headerStyle);
        createHeaderCell(header, col++, "PAN Number", headerStyle);
        createHeaderCell(header, col++, "Nationality", headerStyle);
        createHeaderCell(header, col++, "Blood Group", headerStyle);
        createHeaderCell(header, col++, "Joining Date", headerStyle);
        createHeaderCell(header, col++, "Department", headerStyle);
        createHeaderCell(header, col++, "Designation", headerStyle);
        createHeaderCell(header, col++, "Branch", headerStyle);
        createHeaderCell(header, col++, "Work Location", headerStyle);
        createHeaderCell(header, col++, "Category", headerStyle);
        createHeaderCell(header, col++, "Grade", headerStyle);
        createHeaderCell(header, col++, "Status", headerStyle);
        createHeaderCell(header, col++, "Last Working Date", headerStyle);
        createHeaderCell(header, col++, "Shift Id", headerStyle);
        createHeaderCell(header, col++, "Shift Start Time", headerStyle);
        createHeaderCell(header, col++, "Shift End Time", headerStyle);
        createHeaderCell(header, col++, "Grace Time", headerStyle);

        int rowIndex = 1;
        int srNo = 1;

        for (EmployeeMasterReportDTO emp : data) {

            Row row = sheet.createRow(rowIndex++);
            col = 0;

            row.createCell(col++).setCellValue(srNo++);
            row.createCell(col++).setCellValue(emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "");
            row.createCell(col++).setCellValue(emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            row.createCell(col++).setCellValue(emp.getGender() != null ? emp.getGender() : "");
            row.createCell(col++).setCellValue(emp.getDob() != null ? emp.getDob().toString() : "");
            row.createCell(col++).setCellValue(emp.getContactNumber() != null ? emp.getContactNumber() : "");
            row.createCell(col++).setCellValue(emp.getAlternateNumber() != null ? emp.getAlternateNumber() : "");
            row.createCell(col++).setCellValue(emp.getFormalEmail() != null ? emp.getFormalEmail() : "");
            row.createCell(col++).setCellValue(emp.getPersonalEmail() != null ? emp.getPersonalEmail() : "");
            row.createCell(col++).setCellValue(emp.getAadhaarNumber() != null ? emp.getAadhaarNumber() : "");
            row.createCell(col++).setCellValue(emp.getPanNumber() != null ? emp.getPanNumber() : "");
            row.createCell(col++).setCellValue(emp.getNationality() != null ? emp.getNationality() : "");
            row.createCell(col++).setCellValue(emp.getBloodGroup() != null ? emp.getBloodGroup() : "");
            row.createCell(col++).setCellValue(emp.getJoiningDate() != null ? emp.getJoiningDate().toString() : "");
            row.createCell(col++).setCellValue(emp.getDepartmentName() != null ? emp.getDepartmentName() : "");
            row.createCell(col++).setCellValue(emp.getDesignationName() != null ? emp.getDesignationName() : "");
            row.createCell(col++).setCellValue(emp.getBranchName() != null ? emp.getBranchName() : "");
            row.createCell(col++).setCellValue(emp.getWorkLocationName() != null ? emp.getWorkLocationName() : "");
            row.createCell(col++).setCellValue(emp.getCategoryName() != null ? emp.getCategoryName() : "");
            row.createCell(col++).setCellValue(emp.getGradeName() != null ? emp.getGradeName() : "");
            row.createCell(col++).setCellValue(emp.getStatus() != null ? emp.getStatus() : "");
            row.createCell(col++).setCellValue(emp.getLastWorkingDate() != null ? emp.getLastWorkingDate().toString() : "");
            row.createCell(col++).setCellValue(emp.getShiftId() != null ? emp.getShiftId() : 0);
            row.createCell(col++).setCellValue(emp.getShiftStartTime() != null ? emp.getShiftStartTime().toString() : "");
            row.createCell(col++).setCellValue(emp.getShiftEndTime() != null ? emp.getShiftEndTime().toString() : "");
            row.createCell(col++).setCellValue(emp.getGraceTimeMins() != null ? emp.getGraceTimeMins() : 0);
        }

        for (int i = 0; i < 26; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=employee_master_report.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void createHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
