package com.smartcare.hrms.common.bean.application.leave;

import com.smartcare.hrms.common.enumeration.DayType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LeaveRequestBean {
     Long getYearId();
     String getLeaveRequestId();
     Long getLeaveNameId();
     String getLeaveName();
     LocalDate getFromDate();
     DayType getFromDateType();
     LocalDate getToDate();
    DayType getToDateType();
     String getEmpId();
     Double getTotalLeaveCount();
     String getReason();
    LocalDate getApplicationDate();
     //Approver name list
    String getApproverName();
    String getApproverCode();
    String getStatus();
    String getRemark();
    LocalDateTime getActionDateTime();
    String getApproverStatus();
    String getApplicationType();
    String getLeaveCode();

}
