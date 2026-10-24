package com.smartcare.hrms.hiring.helper;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class HiringMessageSource {

    private final org.springframework.context.MessageSource messageSource;

    public HiringMessageSource(org.springframework.context.MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key) {
        return messageSource.getMessage(
                key,
                null,
                Locale.getDefault()
        );
    }
}