package com.smartcare.hrms.payroll.controller.statutory;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.payroll.dto.statutory.EmployeeStatutoryDTO;
import com.smartcare.hrms.payroll.service.statutory.EmployeeStatutoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/emp/statutory")
public class EmployeeStatutoryInfoController  extends PayrollBaseController {

    private @Autowired EmployeeStatutoryInfoService employeeStatutoryInfoService;

    @PostMapping(value="/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody EmployeeStatutoryDTO dto){
        try{
            employeeStatutoryInfoService.create(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.statutory.created.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.statutory.creation.failed"),HttpStatus.BAD_REQUEST );
        }
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id){
        try{
            return  responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.statutory.data.fetch.success"), employeeStatutoryInfoService.getById(id), HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.statutory.data.fetch.failed"),HttpStatus.BAD_REQUEST );
        }
    }
    @GetMapping("/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers){
        try{
            return  responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.statutory.data.fetch.success"), employeeStatutoryInfoService.getAll(), HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.statutory.data.fetch.failed"),HttpStatus.BAD_REQUEST );
        }
    }
    @PostMapping(value="/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody EmployeeStatutoryDTO dto){
        try{
            employeeStatutoryInfoService.update(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.statutory.update.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.statutory.update.failed"),HttpStatus.BAD_REQUEST );
        }
    }
    @DeleteMapping(value="/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers,@PathVariable("id") String id){
        try{
            employeeStatutoryInfoService.delete(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.statutory.delete.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.statutory.delete.failed"),HttpStatus.BAD_REQUEST );
        }
    }
}
