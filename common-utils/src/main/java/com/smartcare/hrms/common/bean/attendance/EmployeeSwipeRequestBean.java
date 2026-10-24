package com.smartcare.hrms.common.bean.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public interface EmployeeSwipeRequestBean {

    // Employee Swipe Request Base Info
    UUID getId();

    String getRequestEmpId();

    LocalDate getSwipeDate();

    LocalDate getApplicationDate();

    String getDayType();

    String getSwipeDirection();

    LocalTime getReqInTime();

    LocalTime getReqOutTime();

    String getSwipeStatus();

    String getRemark();

    LocalDateTime getCreatedDateTime();

    LocalDateTime getModifiedDateTime();

    String getCreatedBy();

    String getModifiedBy();

    // Employee Shift Info
    Long getEmpShiftId();

    String getShiftEmpId();

    Long getShiftId();

    String getShiftName();

    String getShiftCode();

    LocalTime getShiftInTime();

    LocalTime getShiftOutTime();

    LocalTime getFirstHalfOutTime();

    LocalTime getSecondHalfInTime();

    LocalDate getEmpShiftDate();

    Boolean getIsDefault();

    // Approver Details
    Long getSwipeRequestApproverId();

    Integer getSequence();

    String getApproverId();

    String getApproverName();

    String getApproverCode();

    LocalDateTime getActionDateTime();

    String getApproverSwipeStatus();

    String getApproverRemark();
}
