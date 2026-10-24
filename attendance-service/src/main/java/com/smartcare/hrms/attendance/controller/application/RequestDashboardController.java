package com.smartcare.hrms.attendance.controller.application;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.CancelDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.CancelDashboardSearchDTO;
import com.smartcare.hrms.attendance.dto.application.RequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.RequestDashboardSearchDTO;
import com.smartcare.hrms.attendance.service.application.RequestDashboardService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/request")
public class RequestDashboardController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private RequestDashboardService requestDashboardService;


    @PostMapping(value = "/rqst/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody RequestDashboardSearchDTO requestDTO) {
        try {
            PaginatedDataResponse<RequestDashboardDTO> responseData= requestDashboardService.dashboard(requestDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"),responseData, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/cancel/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody CancelDashboardSearchDTO requestDTO) {
        try {
            PaginatedDataResponse<CancelDashboardDTO> responseData= requestDashboardService.dashboard(requestDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"),responseData, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
