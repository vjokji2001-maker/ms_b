package com.smartcare.hrms.master.controller.attendance.weekOff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffParentDto;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffTemplateDto;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.weekOff.WeekOffParentService;

import lombok.extern.log4j.Log4j2;

@CrossOrigin
@RestController
@RequestMapping(value = "/attendance/week/off")
@Log4j2
public class WeekOffController {
	
	@Autowired
	private MessageResource messageResource;
	
	@Autowired
	private WeekOffParentService weekOffParentService;
	
	@Autowired
	private ResponseGenerator responseGenerator;
	
	
	@PostMapping(value = "/create")
	public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody WeekOffParentDto weekOffParentDto) {
		try {

			weekOffParentService.create(weekOffParentDto);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
		} catch (Exception e){
			String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }		
	}
	
	
	@PutMapping(value = "/update")
	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody WeekOffParentDto weekOffParentDto) {
		try {
			weekOffParentService.update(weekOffParentDto);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            WeekOffParentDto weekOff = weekOffParentService.getById(id);
            return responseGenerator.getSuccessResponse(weekOff, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
        	String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
	
	@PostMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody WeekOffParentDto weekOffParentDto){
        try {
            List<WeekOffTemplateDto> allOrByNameOrCode = weekOffParentService.getAllOrByNameOrCode(weekOffParentDto);
            return responseGenerator.getSuccessResponse(allOrByNameOrCode, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        }catch (Exception e){
        	String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
	
	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
		try {
			weekOffParentService.delete(id);
			return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
		}
		catch (DataIntegrityViolationException e) {
			return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}
}
