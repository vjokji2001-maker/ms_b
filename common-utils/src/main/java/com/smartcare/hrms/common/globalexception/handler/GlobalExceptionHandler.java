package com.smartcare.hrms.common.globalexception.handler;

import com.smartcare.hrms.common.dto.ResponseDTO;
import com.smartcare.hrms.common.globalexception.ResourceConflictException;
import com.smartcare.hrms.common.response.ResponseGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ResponseGenerator responseGenerator;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return responseGenerator.errorResponse(
                new HttpHeaders(),
                "VALIDATION_ERROR",
                ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ResponseDTO> handleConflict(ResourceConflictException ex) {
        return responseGenerator.errorResponse(
                new HttpHeaders(),
                "RESOURCE_CONFLICT",
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDTO> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return responseGenerator.errorResponse(
                new HttpHeaders(),
                "FAILED",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

}
