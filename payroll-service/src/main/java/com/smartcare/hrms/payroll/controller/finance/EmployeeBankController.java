package com.smartcare.hrms.payroll.controller.finance;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.payroll.dto.finance.EmployeeBankAccountDTO;
import com.smartcare.hrms.payroll.service.finance.EmployeeBankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/emp/bank/account")
public class EmployeeBankController  extends PayrollBaseController {

    private @Autowired EmployeeBankAccountService employeeBankAccountService;

    @PostMapping(value="/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody EmployeeBankAccountDTO dto){
        try {
            employeeBankAccountService.createEmployeeBankAccount(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.bank.account.creation.success"),null, HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.bank.account.creation.failed"),HttpStatus.BAD_REQUEST );
        }
    }
    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        try {
            EmployeeBankAccountDTO result = employeeBankAccountService.getById(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.bank.account.fetch.success"), result, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.bank.account.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers) {
        try {
            List<EmployeeBankAccountDTO> result = employeeBankAccountService.getAllEmployeeBankAccount();
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.bank.account.fetch.success"), result, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.bank.account.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers,  @RequestBody EmployeeBankAccountDTO dto) {
        try {
            EmployeeBankAccountDTO result = employeeBankAccountService.update(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.bank.account.update.success"), result, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.bank.account.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        try {
            employeeBankAccountService.delete(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.bank.account.delete.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.bank.account.delete.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}

