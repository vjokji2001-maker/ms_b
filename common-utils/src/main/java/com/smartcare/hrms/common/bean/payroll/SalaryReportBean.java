package com.smartcare.hrms.common.bean.payroll;

import java.math.BigDecimal;

public interface SalaryReportBean {

    String getEmployeeId();
    String getEmployeeCode();
    String getEmployeeName();
    String getAttendanceSalaryLinkId();
    String getMonthYear();
    Double getPayableDays();
    Double getLopDays();
    BigDecimal getTotalEarnings();
    BigDecimal getTotalStdDeductions();
    BigDecimal getTotalOtherDeductions();
    BigDecimal getTotalPayableEarnings();
    BigDecimal getTotalPayableStdDeductions();
    BigDecimal getTotalPayableOtherDeductions();
    BigDecimal getNetPayable();
    BigDecimal getGrossAmount();
    BigDecimal getStdDeductionAmount();
    BigDecimal getOtherDeductionAmount();
    BigDecimal getNetAmount();
    Integer getComponentSeq();
    Long getComponentId();
    String getComponentType();
    String getCalculationType();
    BigDecimal getComponentAmount();
    String getComponentName();
    String getComponentCode();
}
