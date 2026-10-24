package com.smartcare.hrms.attendance.controller.application.leave;

import com.netflix.discovery.converters.Auto;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveBalanceDTO;
import com.smartcare.hrms.attendance.service.application.leave.LeaveBalanceService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("emp/leave/balance")
@Log4j2
@CrossOrigin()
public class LeaveBalanceController {

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired LeaveBalanceService leaveBalanceService;

    @PostMapping("/fetch")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @RequestBody LeaveBalanceDTO dto) {
        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), leaveBalanceService.get(dto), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

}
