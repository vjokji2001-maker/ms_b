package com.smartcare.hrms.master.controller.employment;

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
import com.smartcare.hrms.common.dto.ResponseDTO;
import com.smartcare.hrms.common.dto.master.employment.DesignationDto;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.employment.DesignationService;

import lombok.extern.log4j.Log4j2;

@CrossOrigin
@RestController
@RequestMapping(value = "/designation")
@Log4j2
public class DesignationController {
	
	
	@Autowired
	private MessageResource messageResource;
	
	
	@Autowired
	private DesignationService designationService;
	
	@Autowired
	private ResponseGenerator responseGenerator;
	

	@PostMapping(value = "/create")
	public ResponseEntity<ResponseDTO> create(@RequestHeader HttpHeaders headers, @RequestBody DesignationDto designationDto){
	
		DesignationDto designationDtoResponse = null;	
		try {
			designationDtoResponse = designationService.create(designationDto);
			
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), designationDtoResponse, HttpStatus.CREATED);
	}


	@PostMapping(value = "/get/all")
	public ResponseEntity<ResponseDTO> getAll(@RequestHeader HttpHeaders headers, @RequestBody DesignationDto designationDto){
		
		List<DesignationDto> desigantionDtoResponse = null;	
		try {
			desigantionDtoResponse = designationService.getByNameOrCodeOrAll(designationDto);
			
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.getSuccessResponse(desigantionDtoResponse, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
	}
	

	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<ResponseDTO> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id){
		
		try {
			designationService.delete(id);			
		}
		catch (DataIntegrityViolationException e) {
			return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
		
		return responseGenerator.getSuccessResponse(null,  messageResource.getEnglishMessage("delete.api.success") , HttpStatus.OK);	
	}
	

	@PutMapping(value = "/update")
	public ResponseEntity<ResponseDTO> update(@RequestHeader HttpHeaders headers, @RequestBody DesignationDto designationDto){	
		
		DesignationDto designationDtoResponse = null;	
		try {
			designationDtoResponse = designationService.update(designationDto);
			
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), designationDtoResponse, HttpStatus.OK);	
	}
	

	@GetMapping(value = "/get/by/{id}")
	public ResponseEntity<ResponseDTO> get(@RequestHeader HttpHeaders headers, @PathVariable Long id) {

		DesignationDto designationResponse = null;
		try {
			designationResponse = designationService.getById(id);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);

		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), designationResponse, HttpStatus.OK);
	}
	
}
