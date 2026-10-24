package com.smartcare.hrms.attendance.controller.application;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.*;
import com.smartcare.hrms.attendance.service.application.ApproverDashboardService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approver")
@CrossOrigin
@Log4j2
public class ApproverDashboardController {

    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;
    private @Autowired ApproverDashboardService approverDashboardService;

    @PostMapping(value = "/approved/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody ApproverDashboardSearchDTO dto) {
        try {
            PaginatedDataResponse<ApproverDashboardDTO> responseData= approverDashboardService.getApprovedData(dto);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"),responseData, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "/cancel/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody CancelApproverSearchDTO dto) {
        try {
            PaginatedDataResponse<CancelApproverDashboardDTO> responseData= approverDashboardService.getCancelApproverData(dto);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"),responseData, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
