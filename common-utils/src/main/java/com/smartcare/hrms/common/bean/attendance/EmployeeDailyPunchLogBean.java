package com.smartcare.hrms.common.bean.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmployeeDailyPunchLogBean {

    String getEmpId();

    LocalDate getPunchDate();

    LocalDateTime getPunchDateTime();

    String getDirection();

    Long getId();
}
