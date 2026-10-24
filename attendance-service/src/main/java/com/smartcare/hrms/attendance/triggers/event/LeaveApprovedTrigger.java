package com.smartcare.hrms.attendance.triggers.event;

import com.smartcare.hrms.common.enumeration.DayType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class LeaveApprovedTrigger extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final DayType   fromDateType;
    private final DayType  toDateType;
    private final String leaveCode;
    private final Long shiftId;
    private final UUID leaveId;

    public LeaveApprovedTrigger(Object source,String employeeId, LocalDate fromDate,LocalDate toDate,DayType  fromDateType,
                                DayType  toDateType, String leaveCode,Long shiftId, UUID leaveId) {

        super(source);
        this.employeeId = employeeId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromDateType = fromDateType;
        this.toDateType = toDateType;
        this.leaveCode = leaveCode;
        this.shiftId = shiftId;
        this.leaveId= leaveId;
    }
}
