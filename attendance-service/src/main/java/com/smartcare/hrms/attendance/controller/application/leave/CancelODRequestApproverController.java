package com.smartcare.hrms.attendance.controller.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.service.application.leave.CancelODRequestApproverService;
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
@RequestMapping("cancel/od/approver")
@Log4j2
public class CancelODRequestApproverController {
    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired CancelODRequestApproverService cancelODRequestApproverService;

    @PostMapping(value = "/dashboard")
    public ResponseEntity<?> getAllOD(@RequestHeader HttpHeaders headers, @RequestBody CancelODApproverSearchDTO approverSearchDTO) {
        try {
            PaginatedDataResponse<CancelODApproverDashboardDTO> responseData = cancelODRequestApproverService.getODData(approverSearchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), responseData, HttpStatus.OK);

        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/single/update")
    public ResponseEntity<?> updateSingleODRequest(@RequestHeader HttpHeaders headers, @RequestBody LeaveApproverDTO leaveApproverDTO) {
        try {
            String clinicId = headers.getFirst("clinicid");
            cancelODRequestApproverService.approveSingleCancelODRequestStatus(leaveApproverDTO, clinicId);
            String status = leaveApproverDTO.getApproverStatus();
            String messageKey = null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "cancel.OD.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "cancel.OD.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/bulk/update")
    public ResponseEntity<?> updateBulkODRequest(@RequestHeader HttpHeaders headers, @RequestBody List<LeaveApproverDTO> leaveApproverDTOList) {
        try {
            String clinicId = headers.getFirst("clinicid");
            cancelODRequestApproverService.approveBulkCancelODRequestStatus(leaveApproverDTOList, clinicId);
            String status = leaveApproverDTOList.get(0).getApproverStatus();
            String messageKey = null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "cancel.OD.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "cancel.OD.request.rejected.success";
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getODById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            LeaveApproverDTO leaveApproverDTO = cancelODRequestApproverService.getODById(id);

            return responseGenerator.getSuccessResponse(leaveApproverDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
