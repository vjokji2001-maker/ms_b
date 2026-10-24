package com.smartcare.hrms.common.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ResponseDTO {


    private Object data;
    private CustomErrorDTO error;
    private LocalDateTime timestamp;
    private String message;
    private Integer statusCode;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public CustomErrorDTO getError() {
        return error;
    }

    public void setError(CustomErrorDTO error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

}
