package com.smartcare.hrms.migration.dto.attendance;

import lombok.Data;

@Data
public class AttendanceUploadRequestDTO {

    private String  employeeCode;
    private Integer month;
    private Integer year;
    private Integer presentDays;
    private Integer absentDays;
    private Integer weekOffDays;
}
