package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.service.application.leave.CancelLeaveRequestService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin
@Log4j2
@RequestMapping("/cancel/leave/request")
public class CancelLeaveRequestController {

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private  @Autowired CancelLeaveRequestService  cancelLeaveRequestService;

    @PostMapping("/dashboard")
    public ResponseEntity<?> save(@RequestHeader HttpHeaders headers, @RequestBody CancelRequestSearchDTO searchDTO){
        try{
            PaginatedDataResponse<CancelRequestDashboardDTO> response = cancelLeaveRequestService.getDate(searchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"),response, HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"),HttpStatus.BAD_REQUEST );
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody CancelLeaveRequestDTO requestDTO) {
        try {
            cancelLeaveRequestService.create(requestDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("request.created.successfully"),null,HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("request.created.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getLeaveById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {
        try {
            LeaveODRequestDTO leaveRequestDTO = cancelLeaveRequestService.getById(id);
            return responseGenerator.getSuccessResponse(leaveRequestDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping (value = "/delete/by/{id}")
    public ResponseEntity<?> deleteById(@RequestHeader HttpHeaders headers, @PathVariable("id") UUID id) {
        try {
            cancelLeaveRequestService.delete(id);
            return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
