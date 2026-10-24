package com.smartcare.hrms.common.globalexception;

public class ValidationException extends RuntimeException{
    public ValidationException(String message){
        super(message);
    }
}
