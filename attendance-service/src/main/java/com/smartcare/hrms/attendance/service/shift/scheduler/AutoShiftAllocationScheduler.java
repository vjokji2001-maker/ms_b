package com.smartcare.hrms.attendance.service.shift.scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class AutoShiftAllocationScheduler {

    private @Autowired AutoShiftScheduler autoShiftScheduler;



    @Transactional
    public void allocateDefaultShiftToEmployee(){
        autoShiftScheduler.assignDefaultShiftToEmployees();
    }

//    @PostConstruct
//    public void runOnStartup() {
//        System.out.println("🚀 Running auto shift allocation at startup...");
//        autoShiftScheduler.assignDefaultShiftToEmployees();
//    }
}
