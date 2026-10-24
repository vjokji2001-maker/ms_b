package com.smartcare.hrms.report.bean.employee;

import java.util.Date;

public interface EmployeeMasterReportBean {

    String getId();
    String getEmpCode();
    String getFormalName();
    String getGender();
    Date getDob();
    String getContactNumber();
    String getAlternateNumber();
    String getFormalEmail();
    String getPersonalEmail();
    String getAadhaarNumber();
    String getPanNumber();
    String getNationality();
    String getBloodGroup();
    Date getJoiningDate();
    String getStatus();
    Date getLastWorkingDate();

    String getDepartmentName();
    String getDesignationName();
    String getWorkLocationName();
    String getBranchName();
    String getCategoryName();
    String getGradeName();

    Long getShiftId();
    Boolean getIsDefault();
    Date getShiftStrtTime();
    Date getShiftEndTime();
    Integer getGraceTimeMins();

}
