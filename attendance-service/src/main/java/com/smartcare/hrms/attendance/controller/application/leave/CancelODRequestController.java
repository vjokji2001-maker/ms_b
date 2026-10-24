package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.service.application.leave.CancelODRequestService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@Log4j2
@RequestMapping("cancel/od/request")
public class CancelODRequestController {
    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private  @Autowired CancelODRequestService cancelODRequestService;

    @PostMapping("/dashboard")
    public ResponseEntity<?> save(@RequestHeader HttpHeaders headers, @RequestBody CancelRequestSearchDTO searchDTO){
        try{
            PaginatedDataResponse<CancelODRequestDashboardDTO> response = cancelODRequestService.getDate(searchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"),response, HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"),HttpStatus.BAD_REQUEST );
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody CancelLeaveRequestDTO requestDTO) {
        try {
            cancelODRequestService.create(requestDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("request.created.successfully"),null,HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("request.created.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getLeaveById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {
        try {
            LeaveODRequestDTO leaveRequestDTO = cancelODRequestService.getById(id);
            return responseGenerator.getSuccessResponse(leaveRequestDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping (value = "/delete/by/{id}")
    public ResponseEntity<?> deleteById(@RequestHeader HttpHeaders headers, @PathVariable("id") UUID id) {
        try {
            cancelODRequestService.delete(id);
            return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

}
