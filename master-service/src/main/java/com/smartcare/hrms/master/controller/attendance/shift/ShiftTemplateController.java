package com.smartcare.hrms.master.controller.attendance.shift;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDashboardDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.shift.ShiftTemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/attendance/shift")
@Log4j2
public class ShiftTemplateController {

    @Autowired private MessageResource messageResource;

    private @Autowired ShiftTemplateService shiftTemplateService;

    private @Autowired ResponseGenerator responseGenerator;

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody ShiftTemplateDTO shiftTemplateDTO){
        try {
            shiftTemplateService.create(shiftTemplateDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
        }catch (Exception e){
        	String cause= e.getLocalizedMessage();
        	return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("create.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody ShiftTemplateDTO shiftTemplateDTO){
        try {
            shiftTemplateService.update(shiftTemplateDTO);
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("update.api.success"),null, HttpStatus.OK);
        }catch (Exception e){
        	String cause= e.getLocalizedMessage();
        	return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody ShiftTemplateDTO shiftTemplateDTO){
        try {
            List<ShiftTemplateDashboardDTO> allOrByNameOrCode = shiftTemplateService.getAllOrByNameOrCode(shiftTemplateDTO);
            return responseGenerator.getSuccessResponse(allOrByNameOrCode,messageResource.getEnglishMessage("fetch.api.success"),HttpStatus.OK);
        }catch (Exception e){
        	String cause= e.getLocalizedMessage();
        	return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("fetch.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            ShiftTemplateDTO template = shiftTemplateService.getById(id);
            return responseGenerator.getSuccessResponse(template, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
        	String cause= e.getLocalizedMessage();
        	return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
        	shiftTemplateService.delete(id);
        	 return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("delete.api.success"),null, HttpStatus.OK);
        } catch (Exception e) {
        	String cause= e.getLocalizedMessage();
        	return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("delete.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }
    
    
}