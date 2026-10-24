package com.smartcare.hrms.common.bean.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public interface SwipeRequestApproverDetailsBean {

    //  Approver Details
    Long getApproverDtlId();

    Integer getSequence();

    String getApproverId();

    String getApproverName();

    String getApproverCode();

    String getSwipeStatus();

    LocalDateTime getActionDateTime();

    String getApproverRemark();

    // Employee Swipe Request
    UUID getEmpSwpRqstId();

    String getEmpId();

    String getName();

    String getCode();

    LocalDate getSwpDate();

    LocalDate getApplicationDate();

    String getDayType();

    String getSwipeDirection();

    LocalTime getReqInTime();

    LocalTime getReqOutTime();

    String getEmpSwpStatus();

    String getEmpSwpRemark();

    LocalDateTime getCreatedDateTime();

    LocalDateTime getModifiedDateTime();

    String getCreatedBy();

    String getModifiedBy();

    // Employee Shift
    Long getEmpShiftPkId();

    String getEmpShiftEmpId();

    Long getShiftId();

    String getShiftName();

    String getShiftCode();

    LocalTime getShiftInTime();

    LocalTime getShiftOutTime();

    LocalTime getFirstHalfOutTime();

    LocalTime getSecondHalfInTime();

    LocalDate getEmpShiftDate();

    Boolean getIsDefault();
}
