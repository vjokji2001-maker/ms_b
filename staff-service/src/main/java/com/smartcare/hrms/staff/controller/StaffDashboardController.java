package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffDashboardSummaryDTO;
import com.smartcare.hrms.staff.service.StaffDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/dashboard")
public class StaffDashboardController {

    @Autowired
    private StaffDashboardService dashboardService;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MessageResource messageResource;

    /**
     * Mobile Home Screen summary API (Profile, Today's Attendance, Leave Balances, Upcoming Holidays, Shift).
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getDashboardOverview(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            if (empId == null || empId.trim().isEmpty()) {
                empId = headers.getFirst("empid");
            }
            if (empId == null || empId.trim().isEmpty()) {
                empId = headers.getFirst("userid");
            }
            StaffDashboardSummaryDTO summary = dashboardService.getDashboardOverview(empId);
            return responseGenerator.getSuccessResponse(summary, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to load staff dashboard overview", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
