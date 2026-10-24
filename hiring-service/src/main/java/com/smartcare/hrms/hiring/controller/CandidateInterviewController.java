package com.smartcare.hrms.hiring.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.hiring.dto.CandidateInterviewHistoryDTO;
import com.smartcare.hrms.hiring.dto.InterviewResultDTO;
import com.smartcare.hrms.hiring.dto.InterviewScheduleDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationResponseDTO;
import com.smartcare.hrms.hiring.helper.HiringMessageSource;
import com.smartcare.hrms.hiring.service.CandidateInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/interview")
@CrossOrigin("*")
public class CandidateInterviewController {

    private @Autowired CandidateInterviewService candidateInterviewService;
    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired HiringMessageSource hiringMessageSource;

    @PostMapping("/schedule/{candidateId}")
    public ResponseEntity<?> scheduleInterview(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long candidateId,
            @RequestBody InterviewScheduleDTO requestDTO) {

        try {

            InterviewScheduleDTO response =
                    candidateInterviewService.scheduleInterview(
                            candidateId,
                            requestDTO
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    "Interview scheduled successfully",
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    "Failed to schedule interview",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/result/{candidateId}")
    public ResponseEntity<?> updateInterviewResult(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long candidateId,
            @RequestBody InterviewResultDTO requestDTO) {

        try {

            candidateInterviewService.updateInterviewResult(
                    candidateId,
                    requestDTO
            );

            return responseGenerator.postSuccessResponse(
                    headers,
                    "Interview result updated successfully",
                    null,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    "Failed to update interview result",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/rounds/{candidateId}")
    public ResponseEntity<?> getInterviewHistory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long candidateId) {

        try {

            List<CandidateInterviewHistoryDTO> response =
                    candidateInterviewService.getInterviewHistory(
                            candidateId
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    "Interview history fetched successfully",
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    "Failed to fetch interview history",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}