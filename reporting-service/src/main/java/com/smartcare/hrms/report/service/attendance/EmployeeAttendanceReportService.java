package com.smartcare.hrms.report.service.attendance;

import com.smartcare.hrms.common.bean.attendance.AttendanceBean;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.report.modal.dto.attendance.AttendanceDTO;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeAttendanceReportDTO;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeSearchDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeAttendanceReportService {

    private @Autowired EmployeeAttendanceRepository attendanceRepository;

    public List<EmployeeAttendanceReportDTO> getReport(EmployeeSearchDTO searchDTO) {

        List<AttendanceBean> list = attendanceRepository.getAttendanceReport(searchDTO.getFromDate(), searchDTO.getToDate(),
                searchDTO.getName(), searchDTO.getCode(), searchDTO.getDepartmentId(), searchDTO.getCompanyId(), searchDTO.getBranchId(),
                searchDTO.getDesignationId(), searchDTO.getCategoryId(), searchDTO.getWorkLocationId(), searchDTO.getGradeId());

        Map<String, EmployeeAttendanceReportDTO> map = new LinkedHashMap<>();

        for (AttendanceBean row : list) {
            EmployeeAttendanceReportDTO emp = map.computeIfAbsent(row.getEmployeeCode(), k -> {
                EmployeeAttendanceReportDTO e = new EmployeeAttendanceReportDTO();
                e.setEmployeeName(row.getEmployeeName());
                e.setEmployeeCode(row.getEmployeeCode());
                e.setDepartment(row.getDepartment());
                e.setLocation(row.getLocation());
                e.setBranch(row.getBranch());
                e.setCategory(row.getCompany());
                e.setDesignation(row.getDesignation());
                e.setGrade(row.getGrade());
                e.setCompany(row.getCompany());
                e.setAttendanceList(new ArrayList<>());
                return e;

            });

            AttendanceDTO dto = new AttendanceDTO();
            dto.setDate(row.getFormattedDate());
            dto.setDay(row.getDay());
            dto.setShiftCode(row.getShiftCode());
            dto.setInTime(row.getEmpInTime() != null ? row.getEmpInTime() : "");
            dto.setOutTime(row.getEmpOutTime() != null ? row.getEmpOutTime() : "");
            dto.setWorkHours(row.getTotalHours() != null ? row.getTotalHours() : "");
            dto.setEarly(row.getEarlyOutMark() != null ? row.getEarlyOutMark() : 0);
            dto.setLate(row.getLateInMark() != null ? row.getLateInMark() : 0);
            dto.setStatus(row.getStatus() != null ? row.getStatus() : "");
            dto.setDayType(row.getDayType() != null ? row.getDayType() : "");
            dto.setDayType1(row.getDayType1() != null ? row.getDayType1() : "");
            emp.getAttendanceList().add(dto);
        }
        return new ArrayList<>(map.values());
    }
        public void exportAttendanceExcel(EmployeeSearchDTO dto, HttpServletResponse response) throws IOException {

            List<EmployeeAttendanceReportDTO> data = getReport(dto);

            SXSSFWorkbook workbook = new SXSSFWorkbook();
            SXSSFSheet sheet = workbook.createSheet("Attendance Report");
            sheet.trackAllColumnsForAutoSizing();

            int rowNum = 0;

            // 🔹 Header Style
            CellStyle headerStyle = workbook.createCellStyle();

            // Background Color
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Font Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);

            headerStyle.setFont(headerFont);

            // Border
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Alignment
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 🔹 Header Row
            Row header = sheet.createRow(rowNum++);
            header.setHeightInPoints(22);

            String[] columns = {
                    "Employee Code", "Employee Name", "Department", "Location", "Grade",
                    "Date", "Day", "Shift", "In Time", "Out Time",
                    "Work Hours", "Late", "Early","DayType", "DayType1", "Status"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);

                // Apply Header Style
                cell.setCellStyle(headerStyle);
            }

            // 🔹 Data Rows
            for (EmployeeAttendanceReportDTO emp : data) {

                if (emp.getAttendanceList() == null)
                    continue;

                for (AttendanceDTO att : emp.getAttendanceList()) {

                    Row row = sheet.createRow(rowNum++);

                    int col = 0;

                    row.createCell(col++).setCellValue(emp.getEmployeeCode());
                    row.createCell(col++).setCellValue(emp.getEmployeeName());
                    row.createCell(col++).setCellValue(emp.getDepartment());
                    row.createCell(col++).setCellValue(emp.getLocation());
                    row.createCell(col++).setCellValue(emp.getGrade());

                    row.createCell(col++).setCellValue(att.getDate());
                    row.createCell(col++).setCellValue(att.getDay());
                    row.createCell(col++).setCellValue(att.getShiftCode());
                    row.createCell(col++).setCellValue(att.getInTime());
                    row.createCell(col++).setCellValue(att.getOutTime());

                    row.createCell(col++).setCellValue(att.getWorkHours());
                    row.createCell(col++).setCellValue(att.getLate());
                    row.createCell(col++).setCellValue(att.getEarly());
                    row.createCell(col++).setCellValue(att.getDayType());
                    row.createCell(col++).setCellValue(att.getDayType1());
                    row.createCell(col++).setCellValue(att.getStatus());
                }
            }

            // 🔹 Auto Size Columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔹 Response Setup
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=attendance_report.xlsx");

            workbook.write(response.getOutputStream());
            workbook.close();
        }
}
