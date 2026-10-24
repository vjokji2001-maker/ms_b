package com.smartcare.hrms.common.bean.application.leave;

import com.smartcare.hrms.common.enumeration.DayType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LeaveODRequestBean {

    String getRequestId();
    String getRequestEmpId();
    LocalDate getApplicationDate();
    LocalDate getFromDate();
    LocalDate getToDate();
    DayType getFromDateType();
    DayType getToDateType();
    Double getTotalCount();
    String getReason();

    String getApproverId();
    String getApproverName();
    String getApproverCode();
    String getApproverStatus();
    String getRemark();
    String getStatus();
    LocalDateTime getActionDateTime();

}
