package com.smartcare.hrms.common.bean.attendance;

import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.DayType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface AttendanceDetailsBean {
    String getShiftCode();
    String getShiftName();
    LocalTime getShiftInTime();
    LocalTime getShiftOutTime();
    LocalDate getShiftDate();
    LocalTime getEmpInTime();
    LocalTime getEmpOutTime();
    String getLeaveName();
    String getAppType();
    LocalDate getLeaveDate();
    String getLeaveReason();
    LocalDate getSwipeDate();
    LocalTime getSwipeRqstInTime();
    LocalTime getSwipeRqstOutTime();
    String getSwipeRemark();
    LocalDateTime getPunchTime();
    String getPunchType();
    String getDirection();
    String getHolidayName();
    DayType getLeaveType();
    ApplicationStatus getStatus();
    String getEmpName();
    String getEmpCode();
}
