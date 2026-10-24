package com.smartcare.hrms.common.bean.attendance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface EmployeeShiftDetailBean {

    Long getEmpShiftId();

    Long getShiftId();

    Long getShiftTemplateId();

    String getShiftName();

    String getShiftCode();

    LocalTime getShiftInTime();

    LocalTime getShiftOutTime();

    LocalTime getFirstHalfOutTime();

    LocalTime getSecondHalfInTime();

    LocalDate getEmpShiftDate();

    BigDecimal getCutoffHr();
    Integer getGraceTimeMins();
    LocalTime getAutoAdjustTime();


}
