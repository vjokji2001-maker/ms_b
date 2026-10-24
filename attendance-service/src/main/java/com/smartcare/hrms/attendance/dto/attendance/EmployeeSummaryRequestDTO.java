package com.smartcare.hrms.attendance.dto.attendance;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeSummaryRequestDTO {
    private List<String> attendanceSalaryLinkIds;
    private String status;
}
