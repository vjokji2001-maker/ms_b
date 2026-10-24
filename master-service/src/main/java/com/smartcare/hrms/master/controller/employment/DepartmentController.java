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
import com.smartcare.hrms.common.dto.master.employment.DepartmentDto;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.employment.DepartmentService;

import lombok.extern.log4j.Log4j2;

@CrossOrigin
@RestController
@RequestMapping(value = "/department")
@Log4j2
public class DepartmentController {

	
	@Autowired
	private MessageResource messageResource;
	
	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private ResponseGenerator responseGenerator;


	@PostMapping(value = "/create")
	public ResponseEntity<ResponseDTO> create(@RequestHeader HttpHeaders headers,
			@RequestBody DepartmentDto departmentDto) {

		DepartmentDto departmentDtoResponse = null;
		try {
			departmentDtoResponse = departmentService.create(departmentDto);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);

		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), departmentDtoResponse,
				HttpStatus.CREATED);
	}


	@PostMapping(value = "/get/all")
	public ResponseEntity<ResponseDTO> getAll(@RequestHeader HttpHeaders headers, @RequestBody DepartmentDto departmentDto) {

		List<DepartmentDto> departmentDtoResponse = null;
		try {
			departmentDtoResponse = departmentService.getByNameOrCodeOrAll(departmentDto);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.getSuccessResponse(departmentDtoResponse, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
	}


	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<ResponseDTO> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
		try {
			departmentService.delete(id);

		} catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
			String cause = e.getLocalizedMessage();
			e.printStackTrace();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
	}


	@PutMapping(value = "/update")
	public ResponseEntity<ResponseDTO> update(@RequestHeader HttpHeaders headers, @RequestBody DepartmentDto departmentDto) {
		DepartmentDto departmentDtoResponse = null;
		try {
			departmentDtoResponse = departmentService.update(departmentDto);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), departmentDtoResponse,
				HttpStatus.OK);
	}
	

	@GetMapping(value = "/get/by/{id}")
	public ResponseEntity<ResponseDTO> get(@RequestHeader HttpHeaders headers, @PathVariable Long id) {

		DepartmentDto departmentResponse = null;
		try {
			departmentResponse = departmentService.getById(id);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);

		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), departmentResponse, HttpStatus.OK);
	}
}
