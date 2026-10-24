package com.smartcare.hrms.common.bean.attendance;

public interface EmployeeLeaveDataBean {
    Integer getYearlyCount();
    Integer getQuarterlyCount();
    Integer getMonthlyCount();
    Integer getPastDaysAllowed();
    Integer getFutureDaysAllowed();
    Boolean getHalfDayAllowed();
    String getLeaveName();
    Long getLeaveNameId();

}
