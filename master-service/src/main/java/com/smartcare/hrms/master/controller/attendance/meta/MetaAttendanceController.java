package com.smartcare.hrms.master.controller.attendance.meta;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.attendance.meta.MetaAttendanceDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.meta.MetaAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin()
public class MetaAttendanceController {

    private @Autowired MessageResource messageResource;
    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MetaAttendanceService metaAttendanceService;

    @GetMapping("/attendance/metadata")
    public ResponseEntity<?> getAllMasterData(@RequestHeader HttpHeaders headers){
        try {
            MetaAttendanceDTO metaAttendanceDTO= metaAttendanceService.getEmployeementAndAttendanceData();
            return responseGenerator.getSuccessResponse(metaAttendanceDTO, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        }catch(Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }

    }
}
