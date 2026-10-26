package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffPayrollSlipDTO;
import com.smartcare.hrms.staff.service.StaffPayrollService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/payroll")
public class StaffPayrollController {

    @Autowired
    private StaffPayrollService payrollService;

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
     * Get list of processed monthly salary slips for staff.
     */
    @GetMapping("/slips")
    public ResponseEntity<?> getSalarySlips(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<StaffPayrollSlipDTO> slips = payrollService.getMySalarySlips(resolvedEmpId);
            return responseGenerator.getSuccessResponse(slips, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch salary slips", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get detailed payslip with earnings & deductions components.
     */
    @GetMapping("/slip-details")
    public ResponseEntity<?> getSlipDetails(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId,
            @RequestParam("monthYear") String monthYear) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            StaffPayrollSlipDTO details = payrollService.getPayslipDetails(resolvedEmpId, monthYear);
            return responseGenerator.getSuccessResponse(details, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch payslip details", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
