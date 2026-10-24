package com.smartcare.hrms.common.bean.master.employment;

public interface BranchDetailsBean {
    Long getId();
    String getName();
    String getCode();
    String getAddress();
    Integer getCountryId();
    String getCountryName();
    Integer getStateId();
    String getStateName();
    Integer getCityId();
    String getCityName();
}
