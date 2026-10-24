package com.smartcare.hrms.attendance.triggers.listeners;

import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.attendance.triggers.event.LeaveCancelTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LeaveCancelSaveListener {

    private @Autowired EmployeeAttendanceRepository employeeAttendanceRepository;

        @EventListener
    public void triggerSaveLeaveCancel(LeaveCancelTrigger event){

        LocalDate date= event.getFromDate();

        while(!date.isAfter(event.getToDate())){

            EmployeeAttendance attendance= employeeAttendanceRepository.findByEmployeeIdAndAttendanceDate(event.getEmployeeId(),date)
                    .orElse(null);

            if(attendance!=null && attendance.getDayType()!=null ||attendance.getDayType1()!=null){

                attendance.setDayType("ABS");
                attendance.setDayType1(null);
                attendance.setPortion(1.0);

                employeeAttendanceRepository.save(attendance);
            }

            date = date.plusDays(1);
        }
    }
}
