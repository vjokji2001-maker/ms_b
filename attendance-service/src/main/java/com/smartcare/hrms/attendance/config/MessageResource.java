package com.smartcare.hrms.attendance.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResource {

    @Autowired
    private MessageSource messageSource;


    public String getEnglishMessage(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    public String getEnglishMessageWithParam(String key, String[] params) {
        return messageSource.getMessage(key, params, Locale.ENGLISH);
    }
}
