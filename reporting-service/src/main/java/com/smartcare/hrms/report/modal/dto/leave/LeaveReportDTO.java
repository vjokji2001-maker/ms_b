package com.smartcare.hrms.report.modal.dto.leave;

import lombok.Data;

import java.util.List;

@Data
public class LeaveReportDTO {

    private String employeeCode;
    private String employeeName;

    private String company;
    private String branch;
    private String department;
    private String designation;
    private String location;
    private String grade;
    private String category;

    private String requestId;
    private String requestType;

    private String fromDate;
    private String toDate;

    private String totalDays;
    private String status;
    private String reason;

    private List<ApprovalDTO> approvalList;
}
