package com.smartcare.hrms.common.bean.application.leave;

public interface ApprovalBean {

    String getRequestId();
    Integer getLevel();
    String getStatus();
    String getRemark();
    String getActionDateTime();
    String getApproverName();
}
