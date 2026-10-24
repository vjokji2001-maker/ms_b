package com.smartcare.hrms.hiring.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.hiring.dto.CandidateApplicationDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationListDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationResponseDTO;
import com.smartcare.hrms.hiring.helper.HiringMessageSource;
import com.smartcare.hrms.hiring.service.CandidateApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/candapp")
@CrossOrigin("*")
public class CandidateApplicationController {

    private @Autowired CandidateApplicationService candidateApplicationService;
    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired HiringMessageSource hiringMessageSource;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> create(
            @RequestHeader HttpHeaders headers,
            @ModelAttribute CandidateApplicationDTO requestDTO,
            @RequestPart("resume") MultipartFile resume) {

        try {
            CandidateApplicationResponseDTO response =
                    candidateApplicationService.create(
                            requestDTO,
                            resume
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage(
                            "candidate.application.created"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage(
                            "candidate.application.error"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping(value = "/list")
    public ResponseEntity<?> getAll(
            @RequestHeader HttpHeaders headers) {

        try {
            List<CandidateApplicationListDTO> response =
                    candidateApplicationService.getAll();

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage(
                            "candidate.application.list.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage(
                            "candidate.application.error"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> getById(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id) {

        try {
            CandidateApplicationResponseDTO response =
                    candidateApplicationService.getById(id);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage(
                            "candidate.application.details.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage(
                            "candidate.application.not.found"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping(
            value = "/update/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @ModelAttribute CandidateApplicationDTO requestDTO,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {

        try {
            CandidateApplicationResponseDTO response =
                    candidateApplicationService.update(
                            id,
                            requestDTO,
                            resume
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage(
                            "candidate.application.updated"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage(
                            "candidate.application.error"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id) {

        try {
            candidateApplicationService.delete(id);

            return responseGenerator.postSuccessResponse(
                    headers,
                    hiringMessageSource.getMessage(
                            "candidate.application.deleted"
                    ),
                    null,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    hiringMessageSource.getMessage(
                            "candidate.application.error"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}