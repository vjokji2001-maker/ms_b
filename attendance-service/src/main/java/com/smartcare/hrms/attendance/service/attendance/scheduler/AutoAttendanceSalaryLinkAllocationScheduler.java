package com.smartcare.hrms.attendance.service.attendance.scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AutoAttendanceSalaryLinkAllocationScheduler {

    @Autowired
    private AutoAttendanceSalaryLinkScheduler autoAttendanceSalaryLinkScheduler;

    public void allocateAutoAttendanceSalaryLink(){
        autoAttendanceSalaryLinkScheduler.linkAttendanceToSalary();
    }

//    @PostConstruct
//    public void runOnStartup() {
//        System.out.println("🚀 Running auto shift allocation at startup...");
//        autoAttendanceSalaryLinkScheduler.linkAttendanceToSalary();
//    }
}
