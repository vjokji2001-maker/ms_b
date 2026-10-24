package com.smartcare.hrms.attendance.controller.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeDailyPunchLogDTO;
import com.smartcare.hrms.attendance.service.application.swipe.EmployeeDailyPunchLogService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@Log4j2
@RequestMapping("/daily/punch/log")
public class EmployeeDailyPunchLogController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private EmployeeDailyPunchLogService employeeDailyPunchLogService;


    @GetMapping(value = "/get/by/{empId}")
    public ResponseEntity<?> getPunchLog(@RequestHeader HttpHeaders headers, @PathVariable("empId") String empId, @RequestParam(required = true) String date) {
        try {
            List<EmployeeDailyPunchLogDTO> employeeDailyPunchLogDTO = employeeDailyPunchLogService.getPunchLog(empId, date);
            return responseGenerator.getSuccessResponse(employeeDailyPunchLogDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
