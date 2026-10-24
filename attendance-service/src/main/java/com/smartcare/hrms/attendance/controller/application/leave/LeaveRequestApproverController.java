package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestSearchDTO;
import com.smartcare.hrms.attendance.service.application.leave.LeaveRequestApproverService;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave/approver")
public class LeaveRequestApproverController {

    public @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired LeaveRequestApproverService leaveRequestApproverService;

    @PostMapping("/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody LeaveRequestApproverSearchDTO dto){
        try {
            PaginatedDataResponse<LeaveRequestApproverDashboardDTO> responseData= leaveRequestApproverService.getData(dto);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"),responseData, HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/single/update")
    public ResponseEntity<?> updateSingleLeaveRequest(@RequestHeader HttpHeaders headers, @RequestBody LeaveApproverDTO leaveApproverDTO){
        try{
                String clinicId= headers.getFirst("clinicid");
            leaveRequestApproverService.approveSingleLeaveRequestStatus(leaveApproverDTO, clinicId);
            String status= leaveApproverDTO.getApproverStatus();
            String messageKey= null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey ="leave.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey ="leave.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id){
        try{
        LeaveApproverDTO leaveApproverDTO= leaveRequestApproverService.getById(id);
        return responseGenerator.getSuccessResponse(leaveApproverDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
    } catch (Exception e) {
        String cause = e.getLocalizedMessage();
        return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
    }
    }

    @PutMapping("/bulk/update")
    public ResponseEntity<?> updateBulkLeaveRequest(@RequestHeader HttpHeaders headers, @RequestBody List<LeaveApproverDTO> leaveApproverDTOList){
        try{
            String clinicId= headers.getFirst("clinicid");
            leaveRequestApproverService.approveBulkLeaveRequestStatus(leaveApproverDTOList,clinicId);
            String status= leaveApproverDTOList.get(0).getApproverStatus();
            String messageKey= null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "leave.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "leave.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


}
