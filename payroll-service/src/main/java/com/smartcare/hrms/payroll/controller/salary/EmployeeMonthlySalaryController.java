package com.smartcare.hrms.payroll.controller.salary;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryDetailsDTO;
import com.smartcare.hrms.payroll.service.salary.EmployeeMonthlySalaryService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/emp/payroll")
@CrossOrigin
@Log4j2
public class EmployeeMonthlySalaryController extends PayrollBaseController {

    private @Autowired EmployeeMonthlySalaryService monthlySalaryService;


    @GetMapping(value = "/get/monthly-salary/{payrollId}")
    public ResponseEntity<?> getMonthlySalary(@RequestHeader HttpHeaders headers, @PathVariable("payrollId") String payrollId) {
        try {
            EmployeeSalaryDetailsDTO monthSalaryDetails = monthlySalaryService.getMonthSalaryDetails(payrollId);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("salary.details.fetch.success"), monthSalaryDetails, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching employee salary data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("salary.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/monthly-salary/{payrollId}:Draft")
    public ResponseEntity<?> draft(@RequestHeader HttpHeaders headers, @PathVariable("payrollId") String payrollId,@Valid @RequestBody EmployeeSalaryDetailsDTO salaryDetailsDTO) {
        try {
            monthlySalaryService.draftMonthlySalary(salaryDetailsDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("salary.drafted.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error fetching employee salary data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("salary.draft.failed"), HttpStatus.BAD_REQUEST);
        }
    }



}
