package com.smartcare.hrms.master.controller.attendance.approvergroup;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.attendance.approvalgroup.ApprovalGroupDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.approvalgroup.ApprovalGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance/approval-groups")
@RequiredArgsConstructor
public class ApprovalGroupController {

    private final ApprovalGroupService approvalGroupService;
    private final MessageResource messageResource;
    private final ResponseGenerator responseGenerator;


    @PostMapping("/get/all")
    public ResponseEntity<?> getAllOrByNameOrByCode(@RequestHeader HttpHeaders headers, @RequestBody ApprovalGroupDTO requestDTO){
        try {
            List<ApprovalGroupDTO> dto=approvalGroupService.getAllByNameOrCode(requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), dto, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("fetch.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers,@RequestBody ApprovalGroupDTO dto) {
        try{
            approvalGroupService.createApprovalGroup(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
        } catch (Exception e) {
            String cause= e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers,@PathVariable String id, @RequestBody ApprovalGroupDTO dto) {
        try{
            approvalGroupService.updateApprovalGroup(id,dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause= e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers,@PathVariable String id) {
        try {
        	approvalGroupService.deleteApprovalGroup(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("delete.api.success"), null, HttpStatus.OK);
		}
        catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
        
    }
    
    @GetMapping("/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id){
    	  try {
	        	List<ApprovalGroupDTO> approvalGroupDTO=approvalGroupService.getApprovalGroup(id);
	            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), approvalGroupDTO, HttpStatus.OK);
	        } catch (Exception e) {
	        	String cause=e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"),  HttpStatus.BAD_REQUEST);
	        }

	    }
}


