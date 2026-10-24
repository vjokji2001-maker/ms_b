package com.smartcare.hrms.report.modal.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeAttendanceReportDTO {

    private String employeeCode;
    private String employeeName;
    private String department;
    private String location;
    private String grade;
    private String company;
    private String branch;
    private String designation;
    private String category;
    private List<AttendanceDTO> attendanceList;
}
