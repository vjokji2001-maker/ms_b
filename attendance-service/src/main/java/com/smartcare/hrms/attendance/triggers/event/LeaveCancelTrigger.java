package com.smartcare.hrms.attendance.triggers.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class LeaveCancelTrigger extends ApplicationEvent {


    private final String employeeId;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    public LeaveCancelTrigger(Object source, String employeeId, LocalDate fromDate, LocalDate toDate) {
        super(source);
        this.employeeId = employeeId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
}
