package com.smartcare.hrms.common.bean.attendance;

import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;

import java.math.BigDecimal;

public interface EmployeeAttendanceSummaryBean {

    String getAttendanceSalaryLinkId();
    String getEmpId();
    String getEmpName();
    String getEmpCode();
    BigDecimal getTotalMonthDays();
    BigDecimal getTotalPresentDays();
    BigDecimal getTotalWeekOfdDays();
    BigDecimal getTotalPaidLeaves();
    BigDecimal getTotalHolidays();
    BigDecimal getTotalLateMark();
    BigDecimal getTotalLateAbs();
    BigDecimal getTotalLateLeaves();
    BigDecimal getTotalAbs();
    BigDecimal getTotalUnpaidLeaves();
    AttendanceStatusEnum getStatus();
}
