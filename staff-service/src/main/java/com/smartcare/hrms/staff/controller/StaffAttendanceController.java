package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffPunchRequestDTO;
import com.smartcare.hrms.staff.dto.StaffPunchResponseDTO;
import com.smartcare.hrms.staff.dto.StaffTodayAttendanceDTO;
import com.smartcare.hrms.staff.service.StaffAttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/attendance")
public class StaffAttendanceController {

    @Autowired
    private StaffAttendanceService attendanceService;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MessageResource messageResource;

    private String resolveEmpId(HttpHeaders headers, String empId) {
        if (empId != null && !empId.trim().isEmpty()) {
            return empId;
        }
        String headerEmp = headers.getFirst("empid");
        if (headerEmp != null && !headerEmp.trim().isEmpty()) {
            return headerEmp;
        }
        return headers.getFirst("userid");
    }

    /**
     * Check-In Punch (Location validation + Face Authentication)
     */
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(value = "clinicid", required = false) String clinicId,
            @RequestBody StaffPunchRequestDTO request) {
        try {
            if (request.getEmpId() == null || request.getEmpId().trim().isEmpty()) {
                request.setEmpId(resolveEmpId(headers, null));
            }
            request.setPunchDirection("IN");
            StaffPunchResponseDTO response = attendanceService.processPunch(request, clinicId);
            return responseGenerator.getSuccessResponse(response, response.getMessage(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Check-in failed", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Check-Out Punch (Location validation + Face Authentication)
     */
    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(value = "clinicid", required = false) String clinicId,
            @RequestBody StaffPunchRequestDTO request) {
        try {
            if (request.getEmpId() == null || request.getEmpId().trim().isEmpty()) {
                request.setEmpId(resolveEmpId(headers, null));
            }
            request.setPunchDirection("OUT");
            StaffPunchResponseDTO response = attendanceService.processPunch(request, clinicId);
            return responseGenerator.getSuccessResponse(response, response.getMessage(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Check-out failed", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get Today's attendance status and punch times.
     */
    @GetMapping("/today-status")
    public ResponseEntity<?> getTodayStatus(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            StaffTodayAttendanceDTO status = attendanceService.getTodayAttendance(resolvedEmpId);
            return responseGenerator.getSuccessResponse(status, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch today status", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get Daily Punch Logs history for a given date.
     */
    @GetMapping("/punch-history")
    public ResponseEntity<?> getPunchHistory(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId,
            @RequestParam(value = "date", required = false) String date) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<Map<String, Object>> history = attendanceService.getPunchHistory(resolvedEmpId, date);
            return responseGenerator.getSuccessResponse(history, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch punch history", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
