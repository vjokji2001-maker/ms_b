package com.smartcare.hrms.employee.controller;

import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.dto.EmployeeDTO;
import com.smartcare.hrms.employee.dto.EmployeeDashboardDTO;
import com.smartcare.hrms.employee.dto.EmployeeNameCodeDTO;
import com.smartcare.hrms.employee.dto.EmployeeSearchDTO;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.employee.service.EmployeeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@Log4j2
public class EmployeeController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ResponseGenerator responseGenerator;

    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSearchDTO searchDTO) {
        try {
            PaginatedDataResponse<EmployeeDashboardDTO> paginatedEmployeeList = employeeService.getPaginatedEmployeeList(searchDTO);
            return responseGenerator.getSuccessResponse(paginatedEmployeeList, messageResource.getEnglishMessage("fetch.api.success") , HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping(value = "/create")
	public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody EmployeeDTO requestDto) {
		try {
			employeeService.create(requestDto, headers.getFirst("clinicid"));
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
		} catch (Exception e){
			String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }		
	}
    
    @PutMapping(value = "/update")
	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody EmployeeDTO requestDto) {
		try {
			employeeService.update(requestDto, headers.getFirst("clinicid"));
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
		} catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}  
    
    @DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable UUID id) {
		try {
			employeeService.delete(id, headers.getFirst("clinicid"));
			return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
		}catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
			String cause = e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
		}
	}
    
    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {
        try {
           EmployeeDTO employeeDTO = employeeService.getById(id);
            return responseGenerator.getSuccessResponse(employeeDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
        	String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/get")
    public ResponseEntity<?> getEmployeeNameAndCode(@RequestHeader HttpHeaders headers, @RequestBody EmployeeNameCodeDTO employeeNameCodeDTO) {
        try {
            List<EmployeeNameCodeDTO> employeeDTO = employeeService.getEmployeeNameAndCodeList(employeeNameCodeDTO);
            return responseGenerator.getSuccessResponse(employeeDTO, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
