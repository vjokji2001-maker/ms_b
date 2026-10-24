package com.smartcare.hrms.staff.controller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffHolidayDTO;
import com.smartcare.hrms.staff.dto.StaffShiftRosterDTO;
import com.smartcare.hrms.staff.service.StaffShiftHolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/roster")
public class StaffShiftHolidayController {

    @Autowired
    private StaffShiftHolidayService shiftHolidayService;

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
     * Get monthly roster (daily shifts, week-offs, and holidays).
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyRoster(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<StaffShiftRosterDTO> roster = shiftHolidayService.getMonthlyRoster(resolvedEmpId, year, month);
            return responseGenerator.getSuccessResponse(roster, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch monthly roster", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get assigned holidays list for employee.
     */
    @GetMapping("/holidays")
    public ResponseEntity<?> getHolidays(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<StaffHolidayDTO> holidays = shiftHolidayService.getAssignedHolidays(resolvedEmpId);
            return responseGenerator.getSuccessResponse(holidays, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch holidays", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get weekly off days.
     */
    @GetMapping("/weekly-off")
    public ResponseEntity<?> getWeeklyOff(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "empId", required = false) String empId) {
        try {
            String resolvedEmpId = resolveEmpId(headers, empId);
            List<String> weekOffDays = shiftHolidayService.getWeeklyOffDays(resolvedEmpId);
            return responseGenerator.getSuccessResponse(weekOffDays, messageResource.getEnglishMessage("api.success"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch weekly off days", e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
