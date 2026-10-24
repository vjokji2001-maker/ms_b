package com.smartcare.hrms.attendance.controller.attendance;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceSalaryLinkDTO;
import com.smartcare.hrms.attendance.service.attendance.AttendanceSalaryLinkService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/attendance/salary/link")
public class AttendanceSalaryLinkController {

    @Autowired
    private ResponseGenerator responseGenerator;
    @Autowired
    private MessageResource messageResource;
    @Autowired
    private AttendanceSalaryLinkService salaryLinkService;

    @PostMapping (value="/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody AttendanceSalaryLinkDTO dto){

        try{
            salaryLinkService.create(dto);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("create.api.success"),null, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"),HttpStatus.BAD_REQUEST );
        }
    }
}
