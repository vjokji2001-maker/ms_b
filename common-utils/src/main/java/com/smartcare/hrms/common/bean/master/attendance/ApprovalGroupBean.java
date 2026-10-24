package com.smartcare.hrms.common.bean.master.attendance;

import java.util.UUID;

public interface ApprovalGroupBean {
    UUID getId();
    String getApprovalName();
    String getApprovalCode();
    Long getApprovalGroupDetailId();
    Integer getApprovalSequence();
    String getEmployeeName();
    String getEmployeeId();
    String getEmployeeCode();
    String getApproverEmpId();
}
