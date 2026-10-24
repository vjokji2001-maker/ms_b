package com.smartcare.hrms.report.bean.employee;

public interface SalaryStatusReportBean {

    String getEmployeeId();
    String getEmployeeCode();
    String getEmployeeName();

    String getDepartment();
    String getDesignation();

    Double getGrossSalary();
    Double getDeductions();
    Double getNetSalary();

    String getSalaryStatus();
    String getAttendanceStatus();
    String getSalaryMonth();

    String getPaymentDate();
    String getPaymentMode();

}
