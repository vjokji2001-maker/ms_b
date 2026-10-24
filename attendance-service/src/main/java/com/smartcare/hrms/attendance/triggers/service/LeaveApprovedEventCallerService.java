package com.smartcare.hrms.attendance.triggers.service;

import com.smartcare.hrms.attendance.triggers.event.LeaveApprovedTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LeaveApprovedEventCallerService {

    private @Autowired ApplicationEventPublisher applicationEventPublisher;

    public void triggerLeaveApprovedEvent(LeaveApprovedTrigger event){
        applicationEventPublisher.publishEvent(event);
    }
}
