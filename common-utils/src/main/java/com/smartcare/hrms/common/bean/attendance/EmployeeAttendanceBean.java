package com.smartcare.hrms.common.bean.attendance;

import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;

import java.time.LocalTime;

public interface EmployeeAttendanceBean {
    String getEmployeeId();
    String getDate();
    String getDay();
    String getShiftCode();
    LocalTime getShiftInTime();
    LocalTime getShiftOutTime();
    LocalTime getEmpInTime();
    LocalTime getEmpOutTime();
    String getDayType1();
    String getDayType2();
    Double getPortion();
    Double getLateInMark();
    Double getEarlyOutMark();
    Double getTotalMarks();
    String getTotalHours();
    AttendanceStatusEnum getStatus();
    LocalTime getInTime();
    LocalTime getOutTime();
    String getAttendanceSalaryLinkId();
    Double getNightShiftAllowanceAmount();
    Double getNightShiftDays();



}
