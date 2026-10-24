package com.smartcare.hrms.hiring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartcare.hrms.hiring.dto.CareerJobDTO;
import com.smartcare.hrms.hiring.dto.HiringRequestDTO;
import com.smartcare.hrms.hiring.dto.HiringStatusResponse;
import com.smartcare.hrms.hiring.helper.HiringMessageSource;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.hiring.service.HiringRequestService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

@RestController
@RequestMapping(value = "/requests")
@CrossOrigin("*")
public class HiringRequestController {

    private @Autowired HiringRequestService hiringRequestService;
    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired HiringMessageSource hiringMessageSource;

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody HiringRequestDTO requestDTO) {
        try {
            HiringRequestDTO response = hiringRequestService.create(requestDTO);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.created"),
                    response,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.error"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }


    @GetMapping(value = "/list")
    public ResponseEntity<?> getAll(
            @RequestHeader HttpHeaders headers) {
        try {
            List<HiringRequestDTO> response = hiringRequestService.getAll();

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.list.success"),
                    response,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.error"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> getById(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id) {
        try {
            HiringRequestDTO response = hiringRequestService.getById(id);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.details.success"),
                    response,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.not.found"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/careerportal")
    public ResponseEntity<?> getApprovedJobs(
            @RequestHeader HttpHeaders headers) {

        try {
            List<CareerJobDTO> response = hiringRequestService.getApprovedJobs();

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.approved.list.success"),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.approved.list.error"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/APPROVE/{id}")
    public ResponseEntity<?> approve(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id) {

        try {
            HiringStatusResponse response =
                    hiringRequestService.approve(id);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.approved"),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.error"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/REJECT/{id}")
    public ResponseEntity<?> reject(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id) {

        try {
            HiringStatusResponse response =
                    hiringRequestService.reject(id);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage("hiring.request.rejected"),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage("hiring.request.error"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}