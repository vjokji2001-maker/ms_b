package com.smartcare.hrms.master.controller.attendance.period;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.attendance.period.MonthCycleDto;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.attendance.period.MonthCycleService;
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
@RequestMapping(value = "/month/cycle")
@Log4j2
public class MonthCycleController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MonthCycleService monthCycleService;


    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody MonthCycleDto monthCycleDto) {
        try {
            monthCycleService.create(monthCycleDto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody MonthCycleDto monthCycleDto) {
        try {
            monthCycleService.update(monthCycleDto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            MonthCycleDto monthCycleDto = monthCycleService.getById(id);
            return responseGenerator.getSuccessResponse(monthCycleDto, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody MonthCycleDto monthCycleDto){
        try {
            List<MonthCycleDto> allOrByNameOrCode = monthCycleService.getByNameOrCodeOrAll(monthCycleDto);
            return responseGenerator.getSuccessResponse(allOrByNameOrCode, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        }catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        try {
            monthCycleService.delete(id);
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
