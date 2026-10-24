package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestDashboardDTO;
import com.smartcare.hrms.attendance.service.application.leave.LeaveODRequestService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@Log4j2
@RequestMapping("od/request")
public class LeaveODRequestController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private LeaveODRequestService leaveODRequestService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody LeaveODRequestDTO requestDTO) {
        try {
            leaveODRequestService.create(requestDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("request.created.successfully"),null, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable UUID id) {
        try {
            leaveODRequestService.delete(id);
            return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {

        try {
            LeaveODRequestDTO leaveODRequestDTO = leaveODRequestService.getById(id);
            return responseGenerator.getSuccessResponse(leaveODRequestDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader HttpHeaders headers, @RequestBody LeaveODRequestSearchDTO searchDTO) {
        try {
            PaginatedDataResponse<LeaveODRequestDashboardDTO> paginatedList = leaveODRequestService.getPaginatedODRequestList(searchDTO);
            return responseGenerator.getSuccessResponse(paginatedList, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);

        } catch (Exception e) {
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }



}
