package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.service.application.leave.CancelLeaveRequestApproverService;
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
@RequestMapping("/cancel/leave/approver")
@Log4j2
public class CancelLeaveRequestApproverController {

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired CancelLeaveRequestApproverService cancelLeaveRequestApproverService;

    @PostMapping(value="/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody CancelRequestApproverSearchDTO approverSearchDTO){
        try {
            PaginatedDataResponse<CancelRequestApproverDashboardDTO> responseData= cancelLeaveRequestApproverService.getData(approverSearchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), responseData, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/single/update")
    public ResponseEntity<?> updateSingleLeaveRequest(@RequestHeader HttpHeaders headers, @RequestBody LeaveApproverDTO leaveApproverDTO){
        try{
            String clinicId= headers.getFirst("clinicid");
            cancelLeaveRequestApproverService.approveSingleCancelLeaveRequestStatus(leaveApproverDTO, clinicId);
            String status= leaveApproverDTO.getApproverStatus();
            String messageKey= null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey ="cancel.leave.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey ="cancel.leave.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/bulk/update")
    public ResponseEntity<?> updateBulkLeaveRequest(@RequestHeader HttpHeaders headers, @RequestBody List<LeaveApproverDTO> leaveApproverDTOList){
        try{
            String clinicId= headers.getFirst("clinicid");
            cancelLeaveRequestApproverService.approveBulkCancelLeaveRequestStatus(leaveApproverDTOList, clinicId);
            String status= leaveApproverDTOList.get(0).getApproverStatus();
            String messageKey= null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "cancel.leave.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "cancel.leave.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id){
        try{
            LeaveApproverDTO leaveApproverDTO= cancelLeaveRequestApproverService.getById(id);
            return responseGenerator.getSuccessResponse(leaveApproverDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
