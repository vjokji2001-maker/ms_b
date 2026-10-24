package com.smartcare.hrms.common.bean.application.leave;

public interface LeaveReportBean {
    String getEmployeeCode();
    String getEmployeeName();

    String getCompany();
    String getBranch();
    String getDepartment();
    String getDesignation();
    String getLocation();
    String getGrade();
    String getCategory();

    String getRequestId();
    String getRequestType();

    String getApplicationDate();

    String getFromDate();
    String getToDate();

    String getTotalDays();
    String getStatus();
    String getReason();
}
