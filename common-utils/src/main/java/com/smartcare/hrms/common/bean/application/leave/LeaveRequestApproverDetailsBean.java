package com.smartcare.hrms.common.bean.application.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface LeaveRequestApproverDetailsBean {

    Long getApproverDtlId();
    UUID getLeaveRqstId();
    Integer getSequence();
    String  getApproverId();
    String getApproverName();
    String getApproverCode();
    String getStatus();
    String getApproverRemark();
    LocalDateTime getActionDateTime();
    String getEmpLeaveRqstId();
    String  getEmpId();
    String getName();
    String getCode();
    LocalDate getApplicationDate();
    LocalDate getFromDate();
    String getFromDateDuration();
    LocalDate getToDate();
    String getToDateDuration();
    String getReason();
    String getRemark();
    String getApplicationType();
    String getLeaveName();
}
