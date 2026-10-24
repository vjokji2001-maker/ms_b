package com.smartcare.hrms.master.controller.employment;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.ResponseDTO;
import com.smartcare.hrms.common.dto.master.employment.CategoryDto;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.employment.CategoryService;
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
@RequestMapping(value = "/category")
@Log4j2
public class CategoryController {

	@Autowired
	private MessageResource messageResource;
		
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ResponseGenerator responseGenerator;


	@PostMapping(value = "/create")
	public ResponseEntity<ResponseDTO> create(@RequestHeader HttpHeaders headers,
			@RequestBody CategoryDto categoryDto) {

		CategoryDto categoryDtoResponse = null;
		try {
			categoryDtoResponse = categoryService.create(categoryDto);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), categoryDtoResponse,
				HttpStatus.CREATED);
	}


	@PostMapping(value = "/get/all")
	public ResponseEntity<ResponseDTO> getAll(@RequestHeader HttpHeaders headers, @RequestBody CategoryDto categoryDto) {

		List<CategoryDto> categoryDtoResponse = null;
		try {
			categoryDtoResponse = categoryService.getByNameOrCodeOrAll(categoryDto);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.getSuccessResponse(categoryDtoResponse, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
	}


	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<ResponseDTO> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
		try {
			categoryService.delete(id);

		} catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
	}


	@PutMapping(value = "/update")
	public ResponseEntity<ResponseDTO> update(@RequestHeader HttpHeaders headers, @RequestBody CategoryDto categoryDto) {
		CategoryDto categoryDtoResponse = null;
		try {
			categoryDtoResponse = categoryService.update(categoryDto);
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(null, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), categoryDtoResponse,
				HttpStatus.OK);
	}
	

	@GetMapping(value = "/get/by/{id}")
	public ResponseEntity<ResponseDTO> get(@RequestHeader HttpHeaders headers, @PathVariable Long id) {

		CategoryDto categoryResponse = null;
		try {
			categoryResponse = categoryService.getById(id);

		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);

		}
		return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), categoryResponse, HttpStatus.OK);
	}

}
