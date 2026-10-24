package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffProfileDTO;
import com.smartcare.hrms.staff.service.StaffProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/profile")
public class StaffProfileController {

    @Autowired
    private StaffProfileService profileService;

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
     * Get real employee profile info.
     */
    @GetMapping("")
    public ResponseEntity<?> getProfile(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            StaffProfileDTO profile = profileService.getStaffProfile(resolvedEmpId);
            return responseGenerator.getSuccessResponse(profile, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch profile", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
