package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffLeaveApplyDTO;
import com.smartcare.hrms.staff.dto.StaffLeaveBalanceDTO;
import com.smartcare.hrms.staff.dto.StaffLeaveRequestItemDTO;
import com.smartcare.hrms.staff.service.StaffLeaveService;
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
@RequestMapping("/leave")
public class StaffLeaveController {

    @Autowired
    private StaffLeaveService leaveService;

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
     * Get real leave balances for employee.
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getLeaveBalance(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<StaffLeaveBalanceDTO> balances = leaveService.getLeaveBalances(resolvedEmpId);
            return responseGenerator.getSuccessResponse(balances, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch leave balance", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Apply for leave.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(
            @RequestHeader HttpHeaders headers,
            @RequestBody StaffLeaveApplyDTO applyDTO) {
        try {
            if (applyDTO.getEmpId() == null || applyDTO.getEmpId().trim().isEmpty()) {
                applyDTO.setEmpId(resolveEmpId(headers, null));
            }
            Map<String, Object> result = leaveService.applyLeave(applyDTO);
            return responseGenerator.getSuccessResponse(result, messageResource.getEnglishMessage("leave.applied.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to apply leave", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get employee's leave request history.
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<StaffLeaveRequestItemDTO> list = leaveService.getMyLeaveHistory(resolvedEmpId);
            return responseGenerator.getSuccessResponse(list, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch leave history", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
