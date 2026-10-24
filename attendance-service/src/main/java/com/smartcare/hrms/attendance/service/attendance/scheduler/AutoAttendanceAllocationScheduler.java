package com.smartcare.hrms.attendance.service.attendance.scheduler;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AutoAttendanceAllocationScheduler {

    private @Autowired AutoAttendanceScheduler autoAttendanceScheduler;


    public void allocateDefaultAttendance(){
        autoAttendanceScheduler.assignDefaultAttendance();
    }

//    @PostConstruct
//    public void runOnstart(){
//        System.out.println("🚀 Running auto shift allocation at startup...");
//        autoAttendanceScheduler.assignDefaultAttendance();
//    }
}
