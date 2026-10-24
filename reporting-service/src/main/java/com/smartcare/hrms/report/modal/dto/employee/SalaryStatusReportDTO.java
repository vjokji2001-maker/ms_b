package com.smartcare.hrms.report.modal.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStatusReportDTO {

    private String employeeId;
    private String employeeCode;
    private String employeeName;

    private String department;
    private String designation;

    private Double grossSalary;
    private Double deductions;
    private Double netSalary;

    private String salaryStatus;
    private String attendanceStatus;
    private String salaryMonth;

    private String paymentDate;
    private String paymentMode;

}
