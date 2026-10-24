package com.smartcare.hrms.master.controller.attendance.leave;


import com.smartcare.hrms.common.dto.ResponseDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveNameDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.master.service.attendance.leave.LeaveNameService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/attendance/leaveName")
@Log4j2
public class LeaveNameController {
	private @Autowired MessageResource messageResource;
	private @Autowired LeaveNameService leaveNameService;
	private @Autowired ResponseGenerator responseGenerator;
	
	
	@PostMapping(value="/create")
	public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody LeaveNameDTO leaveNameDTO){
		try {
			leaveNameService.create(leaveNameDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause,messageResource.getEnglishMessage("create.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update")
	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody LeaveNameDTO leaveNameDTO){
		try {
			leaveNameService.update(leaveNameDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause,messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	 @GetMapping(value = "/get/by/{id}")
	    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
	        try {
	        	LeaveNameDTO leaveNameDTO = leaveNameService.getById(id);
	            return responseGenerator.getSuccessResponse(leaveNameDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
	        } catch (Exception e) {
	        	String cause = e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers,cause,  messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
	        }
	    }
	  @PostMapping(value = "/get/all")
	    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody LeaveNameDTO leaveNameDTO){
	        try {
	            List<LeaveNameDTO> getAllOrByNameOrbyCode= leaveNameService.getAllOrByNameOrByCode(leaveNameDTO);
	             return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), getAllOrByNameOrbyCode, HttpStatus.OK);
	        }catch (Exception e){
	        	String cause = e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("fetch.api.failed"),  HttpStatus.BAD_REQUEST);
	        }
	    }
	 @DeleteMapping(value = "/delete/{id}")
		public ResponseEntity<ResponseDTO> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
			try {
				leaveNameService.delete(id);
				return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("delete.api.success"), null, HttpStatus.OK);
			}catch (DataIntegrityViolationException e) {
                return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
            }
            catch (Exception e) {
				String cause = e.getLocalizedMessage();
				return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
			}
			
		}
 
}
