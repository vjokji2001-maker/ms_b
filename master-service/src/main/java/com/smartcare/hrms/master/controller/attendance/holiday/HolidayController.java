package com.smartcare.hrms.master.controller.attendance.holiday;

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
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayDashboardDTO;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayParentDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.holiday.HolidayService;

import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin
@RequestMapping("/attendance/holiday")
@Log4j2
public class HolidayController {

	private @Autowired MessageResource messageResource;
	private @Autowired ResponseGenerator responseGenerator;
	private @Autowired HolidayService holidayService;
	
	@PostMapping(value = "/create")
	public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody HolidayParentDTO holidayParentDTO){
		try {
			holidayService.create(holidayParentDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
		} catch (Exception e) {
			String cause= e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	@PutMapping(value = "/update")
	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody HolidayParentDTO holidayParentDTO){
		try {
			holidayService.update(holidayParentDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
		} catch (Exception e) {
			String cause= e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(value = "/get/by/{id}")
	public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id){
		try {
			HolidayParentDTO dto = holidayService.getById(id); 
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), dto, HttpStatus.OK);
		} catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers,cause,  messageResource.getEnglishMessage("fetch.api.failed"),  HttpStatus.BAD_REQUEST);
		}
	}
	@PostMapping(value = "/get/all")
	public ResponseEntity<?> getAllOrByNameOrByCode(@RequestHeader HttpHeaders headers, @RequestBody HolidayParentDTO holidayParentDTO){
		try {
			 List<HolidayDashboardDTO> dto=holidayService.getAllOrByNameOrCode(holidayParentDTO);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), dto, HttpStatus.OK);
		} catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers,cause,  messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}
	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id){
		try {
			holidayService.delete(id);
			return responseGenerator.getSuccessResponse(null,  messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
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
