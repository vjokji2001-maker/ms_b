package com.smartcare.hrms.master.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageResource {
    @Autowired private MessageSource messageSource;

    public String getEnglishMessage(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }
    public String getEnglishMessageWithParam(String key, String[] params) {
        return messageSource.getMessage(key, params, Locale.ENGLISH);
    }

}
