package com.smartcare.hrms.common.response;

import com.smartcare.hrms.common.dto.CustomErrorDTO;
import com.smartcare.hrms.common.dto.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ResponseGenerator {

    public ResponseEntity<ResponseDTO> getSuccessResponse(Object object, String message, HttpStatus statusCode){
        HttpHeaders followingHeaders = new HttpHeaders();
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setData(object);
        responseDTO.setMessage(message);
        responseDTO.setStatusCode(statusCode.value());
        responseDTO.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(responseDTO, followingHeaders, statusCode);
    }


    public ResponseEntity<ResponseDTO> postSuccessResponse(HttpHeaders headers, String message , Object object, HttpStatus statuscode){
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setData(object);
        responseDTO.setMessage(message);
        responseDTO.setStatusCode(statuscode.value());
        responseDTO.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(responseDTO,statuscode);
    }


    public ResponseEntity<ResponseDTO> errorResponse(HttpHeaders headers, String cause, String message, HttpStatus statuscode){
        ResponseDTO responseDTO = new ResponseDTO();
        CustomErrorDTO error = new CustomErrorDTO();
        error.setCause(cause);
        responseDTO.setError(error);
        responseDTO.setMessage(message);
        responseDTO.setStatusCode(statuscode.value());
        responseDTO.setTimestamp(LocalDateTime.now());
        HttpHeaders followingHeaders = new HttpHeaders();
    	followingHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(responseDTO,followingHeaders,statuscode);
    }
}
