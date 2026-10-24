package com.smartcare.hrms.staff.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResource {

    @Autowired
    private MessageSource messageSource;

    public String getEnglishMessage(String key) {
        try {
            return messageSource.getMessage(key, null, Locale.ENGLISH);
        } catch (Exception e) {
            return key;
        }
    }

    public String getEnglishMessageWithParam(String key, Object[] params) {
        try {
            return messageSource.getMessage(key, params, Locale.ENGLISH);
        } catch (Exception e) {
            return key;
        }
    }
}
