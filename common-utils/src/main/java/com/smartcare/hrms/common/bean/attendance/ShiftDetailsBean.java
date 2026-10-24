package com.smartcare.hrms.common.bean.attendance;

import com.smartcare.hrms.common.enumeration.LateMarkType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ShiftDetailsBean {

    Long getEmpShiftId();

    Long getShiftId();

    String getEmployeeId();

    LocalDateTime getShiftStartTime();

    LocalDateTime getShiftEndTime();

    Integer getGraceTimeMin();

    LocalTime getFirstHalfOutTime();

    LocalTime getSecondHalfInTime();

    Boolean getHalfDayAllowed();

    String getShiftDate();

    Integer getEndBefore();

    Integer getLateInFromMin();
    Integer getLateInToMin();
    Float  getLateInMarkCount();

    Integer getEarlyOutFromMin();
    Integer getEarlyOutToMin();
    Float  getEarlyOutMarkCount();
    LocalTime getShiftInTime();
    LocalTime getShiftOutTime();
    Double getHalfRate();
    Double getFullRate();
    Boolean getNightShiftExist();
}
