package com.smartcare.hrms.attendance.controller.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.swipe.*;
import com.smartcare.hrms.attendance.service.application.swipe.SwipeRequestApproverDetailsService;
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
@RequestMapping("/swipe/approver")
public class SwipeRequestApproverDetailsController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private SwipeRequestApproverDetailsService swipeRequestApproverDetailsService;


    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader HttpHeaders headers, @RequestBody SwipeRequestApproverSearchDTO searchDTO) {
        try {
            PaginatedDataResponse<SwipeRequestApproverDetailsDashboardDTO> paginatedEmployeeList = swipeRequestApproverDetailsService.getPaginatedSwipeRequestList(searchDTO);
            return responseGenerator.getSuccessResponse(paginatedEmployeeList, messageResource.getEnglishMessage("fetch.api.success") , HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updateSingleSwipeRequest(@RequestHeader HttpHeaders headers, @RequestBody SwipeRequestApproverDetailsDTO requestDto) {
        try {
            String clinicId = headers.getFirst("clinicid");
            swipeRequestApproverDetailsService.updateSingleSwipeRequestStatus(requestDto, clinicId);
            String status = requestDto.getSwipeStatus();
            String messageKey = null;
            if ("APPROVED".equalsIgnoreCase(status)) {
                messageKey = "swipe.request.approved.success";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                messageKey = "swipe.request.rejected.success";
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
            SwipeRequestApproverDetailsDTO employeeSwipeRequestDTO = swipeRequestApproverDetailsService.getById(id);
            return responseGenerator.getSuccessResponse(employeeSwipeRequestDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping(value = "/update/bulk")
    public ResponseEntity<?> updateBulkSwipeRequest(@RequestHeader HttpHeaders headers, @RequestBody List<SwipeRequestApproverDetailsDTO> listRequestDto) {
        try {
            String clinicId = headers.getFirst("clinicid");
            swipeRequestApproverDetailsService.updateBulkSwipeRequestStatus(listRequestDto, clinicId);
            String messageKey = null;
            if (!listRequestDto.isEmpty()) {
                String firstStatus = listRequestDto.get(0).getSwipeStatus();
                if ("APPROVED".equalsIgnoreCase(firstStatus)) {
                    messageKey = "swipe.request.bulk.approved";
                } else if ("REJECTED".equalsIgnoreCase(firstStatus)) {
                    messageKey = "swipe.request.bulk.rejected";
                }
            }
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage(messageKey), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }




}
