package com.smartcare.hrms.common.bean.attendance;

public interface AttendanceBean {
    String getEmployeeCode();
    String getEmployeeName();

    String getCompany();
    String getBranch();
    String getDepartment();
    String getDesignation();
    String getLocation();
    String getGrade();

    String getFormattedDate();
    String getDay();

    String getShiftCode();
    String getShiftInTime();
    String getShiftOutTime();

    String getEmpInTime();
    String getEmpOutTime();

    Integer getLateInMark();
    Integer getEarlyOutMark();
    Integer getTotalMarks();

    String getTotalHours();
    String getStatus();
    String getDayType();
    String getDayType1();

    Long getDepartmentId();
    Long getWorkLocationId();
    Long getGradeId();
}
