package com.smartcare.hrms.employee.controller;

import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.dto.*;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.employee.service.EmployeeConfigService;
import com.smartcare.hrms.employee.service.EmployeeLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/emp/config")
public class EmployeeConfigController {
    private @Autowired EmployeeConfigService employeeConfigService;

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired EmployeeLeaveService employeeLeaveService;

    @PostMapping("/get/dashboard")
    public ResponseEntity<?> getAllOrByNameOrByCode(@RequestHeader HttpHeaders headers, @RequestBody EmployeeConfigSearchDTO weekOfSettingSearchDTO){
        try {
            PaginatedDataResponse<EmployeeConfigDashboardDTO> responseData = employeeConfigService.getDashboardData(weekOfSettingSearchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), responseData, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause,  messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/get/by/{employeeId}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("employeeId") String employeeId){
    	try {
            EmployeeSettingConfigDTO configDTO = employeeConfigService.getByEmployeeId(employeeId);
			return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), configDTO, HttpStatus.OK);
		} catch (Exception e) {
			String cause=e.getLocalizedMessage();
			return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
		}
    }

    @PutMapping("/singleUpdate/{configType}/{employeeId}")
    public ResponseEntity<?> singleUpdate(@RequestHeader HttpHeaders headers, @RequestBody EmployeeConfigDTO dto, @PathVariable ("configType") String configType,@PathVariable ("employeeId") String employeeId){
        try {
        	
        	dto.setEmployeeId(employeeId);
            if("WEEKOFF".equals(configType)){
                employeeConfigService.updateSingleWeekOff(dto, employeeId);
            }
            else if ("HOLIDAY".equals(configType)){
                employeeConfigService.updateSingleHoliday(dto, employeeId);
            }

            else{
                employeeConfigService.updateSingleApprovalGroup(dto,employeeId);
            }


            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), dto, HttpStatus.CREATED);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/bulkUpdate/{configType}")
    public ResponseEntity<?> bulkUpdate(@RequestHeader HttpHeaders headers, @RequestBody List<EmployeeConfigDTO> dtoList, @PathVariable ("configType") String configType) {
        try {

            if ("WEEKOFF".equals(configType)) {
                employeeConfigService.updateMultipleWeekOff(dtoList);
            } else if ("HOLIDAY".equals(configType)) {
                employeeConfigService.updateMultipleHoliday(dtoList);
            } else {
                employeeConfigService.updateMultipleApprovalGroup(dtoList);
            }


            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), dtoList, HttpStatus.CREATED);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value="/create/single/leave")
        public ResponseEntity<?> create (@RequestHeader HttpHeaders headers, @RequestBody EmployeeLeaveParentDTO dto){
            try {
                employeeLeaveService.create(dto);
                return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
            } catch (Exception e) {
                String cause=e.getLocalizedMessage();
                return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
            }
        }
    @PostMapping(value="/create/bulk/leave")
    public ResponseEntity<?> create (@RequestHeader HttpHeaders headers, @RequestBody List<EmployeeLeaveParentDTO> dtoList){
        try {
           employeeLeaveService.createBulkLeave(dtoList);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/fetch/by/{employeeId}")
    public ResponseEntity<?> getByEmployeeId(@RequestHeader HttpHeaders headers, @PathVariable("employeeId") String employeeId){
        try {
            EmployeeSettingConfigDTO configDTO = employeeLeaveService.getEmployeeConfig(employeeId);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("detailed.fetch.api.success"), configDTO, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    }



