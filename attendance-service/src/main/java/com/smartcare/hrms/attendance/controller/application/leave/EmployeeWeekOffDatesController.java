package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.service.application.leave.EmployeeWeekOffDatesService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("emp/week/off/date")
@Log4j2
public class EmployeeWeekOffDatesController {

    private @Autowired MessageResource messageResource;

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired EmployeeWeekOffDatesService employeeWeekOffDatesService;

    @PostMapping("/get")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @RequestBody LeaveODRequestDTO requestDTO){
        try {
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"), employeeWeekOffDatesService.getWeekOffDates(requestDTO), HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"),HttpStatus.BAD_REQUEST );
        }
    }
}
