package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestApproverDetailsDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.service.application.leave.LeaveODRequestApproverDetailsService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
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
@RequestMapping("/od/approver")
public class LeaveODRequestApproverDetailsController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private LeaveODRequestApproverDetailsService leaveODRequestApproverDetailsService;

    @PostMapping("/get/dashboard")
    public ResponseEntity<?> getODDashboard(@RequestHeader HttpHeaders headers, @RequestBody LeaveODApproverSearchDTO searchDTO) {
        try {
            PaginatedDataResponse<LeaveODApproverDashboardDTO> paginatedList = leaveODRequestApproverDetailsService.getPaginatedODRequestList(searchDTO);
            return responseGenerator.getSuccessResponse(paginatedList, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updateSingleODRequest(@RequestHeader HttpHeaders headers, @RequestBody LeaveODRequestApproverDetailsDTO requestDto) {
        try {
            String clinicId = headers.getFirst("clinicid");
            leaveODRequestApproverDetailsService.approveSingleODRequestStatus(requestDto, clinicId);
            String status = requestDto.getOdStatus();
            String messageKey = null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "OD.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "OD.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            LeaveApproverDTO leaveApproverDTO = leaveODRequestApproverDetailsService.getById(id);
            return responseGenerator.getSuccessResponse(leaveApproverDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update/bulk")
    public ResponseEntity<?> updateBulkRequest(@RequestHeader HttpHeaders headers, @RequestBody List<LeaveApproverDTO> listRequestDto) {
        try {
            String clinicId = headers.getFirst("clinicId");
            leaveODRequestApproverDetailsService.approveBulkODRequestStatus(listRequestDto, clinicId);
            String messageKey = null;
            if (!listRequestDto.isEmpty()) {
                String firstStatus = listRequestDto.get(0).getApproverStatus();
                if ("APPROVED".equalsIgnoreCase(firstStatus)) {
                    messageKey = "OD.request.bulk.approved";
                } else if ("REJECTED".equalsIgnoreCase(firstStatus)) {
                    messageKey = "OD.request.bulk.rejected";
                }
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}



