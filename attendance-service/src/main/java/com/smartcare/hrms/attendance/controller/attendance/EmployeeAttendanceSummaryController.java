package com.smartcare.hrms.attendance.controller.attendance;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.attendance.EmployeeSummaryRequestDTO;
import com.smartcare.hrms.attendance.service.attendance.EmployeeAttendanceSummaryService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Log4j2
@CrossOrigin("*")
@RestController
@RequestMapping("/summary")
public class EmployeeAttendanceSummaryController {

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired EmployeeAttendanceSummaryService employeeAttendanceSummaryService;

    @GetMapping(value="/get/{monthYear}")
    public ResponseEntity<?> calculateEmployeeAttendanceSummary(@RequestHeader HttpHeaders headers, @PathVariable("monthYear") String monthYear ){
        try{
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

            YearMonth ym = YearMonth.parse(monthYear, inputFormatter);
            String formattedMonthYear = ym.format(outputFormatter);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"),employeeAttendanceSummaryService.calculateEmployeeAttendanceSummary(formattedMonthYear), HttpStatus.OK );
        } catch (Exception e) {
        String cause=e.getLocalizedMessage();
        return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/lock/employee/summary")
    public ResponseEntity<?> localEmployeeSummary(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSummaryRequestDTO dto ){
        try {
            employeeAttendanceSummaryService.lockedAttendanceSummary(dto.getAttendanceSalaryLinkIds(), dto.getStatus(), headers);
            return  responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("data.locked.success"),null,HttpStatus.OK );

        } catch (Exception e) {
        String cause=e.getLocalizedMessage();
        return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("data.locked.failed"),HttpStatus.BAD_REQUEST );
        }
    }
}
