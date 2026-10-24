package com.smartcare.hrms.master.controller.attendance.leave;


import com.smartcare.hrms.common.dto.ResponseDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDashboardDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.master.service.attendance.leave.LeaveTemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
 
@CrossOrigin
@RestController
@RequestMapping("/attendance/leaveTemplate")
@Log4j2
public class LeaveTemplateController {
	private @Autowired MessageResource messageResource;
	private @Autowired LeaveTemplateService leaveTemplateService;
	private @Autowired ResponseGenerator responseGenerator;
	
	
	@PostMapping(value="/create")
	public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody LeaveTemplateDTO leaveTemplateDTO){
		try {
			leaveTemplateService.create(leaveTemplateDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
		} catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update")
	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody LeaveTemplateDTO leaveTemplateDTO){
		try {
			leaveTemplateService.update(leaveTemplateDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
		} catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	 @GetMapping(value = "/get/by/{id}")
	    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
	        try {
	        	LeaveTemplateDTO template=leaveTemplateService.getById(id);
	            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), template, HttpStatus.OK);
	        } catch (Exception e) {
	        	String cause=e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"),  HttpStatus.BAD_REQUEST);
	        }
	    }
	 @PostMapping(value = "/get/all")
	    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @RequestBody LeaveTemplateDTO leaveTemplateDTO ) {
	        try {
	          List<LeaveTemplateDashboardDTO> getAllOrByNameOrByCode=leaveTemplateService.getAllOrByNameOrCode(leaveTemplateDTO);
	          return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), getAllOrByNameOrByCode, HttpStatus.OK);
	        } catch (Exception e) {
	        	String cause=e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("fetch.api.failed"),  HttpStatus.BAD_REQUEST);
	        }
	    }
 
	 @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<ResponseDTO> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
			try {
				 leaveTemplateService.delete(id);
				 return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("delete.api.success"), null, HttpStatus.OK);
			}catch (DataIntegrityViolationException e) {
				return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
			}
			catch (Exception e) {
				String cause=e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
			}
			
		}
}
