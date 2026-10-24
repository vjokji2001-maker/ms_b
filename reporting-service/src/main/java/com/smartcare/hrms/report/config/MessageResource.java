package com.smartcare.hrms.report.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class MessageResource {
    private  @Autowired MessageSource messageSource;

    public String getEnglishMessage(String key){
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    public String getEnglishMessageWithParam(String key, String[] params){
        return messageSource.getMessage(key, params, Locale.ENGLISH);
    }
}
