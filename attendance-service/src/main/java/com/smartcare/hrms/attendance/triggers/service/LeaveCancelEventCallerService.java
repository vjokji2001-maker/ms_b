package com.smartcare.hrms.attendance.triggers.service;

import com.smartcare.hrms.attendance.triggers.event.LeaveCancelTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LeaveCancelEventCallerService {

    private @Autowired ApplicationEventPublisher applicationEventPublisher;

    public void triggerLeaveCancelEvent(LeaveCancelTrigger event){
        applicationEventPublisher.publishEvent(event);
    }
}
