package com.smartcare.hrms.payroll.controller.salary;

import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.common.dto.payroll.salary.BatchOperationDTO;
import com.smartcare.hrms.common.dto.payroll.salary.MonthlySalarySummaryDashboardDTO;
import com.smartcare.hrms.common.dto.payroll.salary.PayrollSummaryRequestDTO;
import com.smartcare.hrms.payroll.service.salary.PayrollSummaryService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/emp/payroll/summary")
@CrossOrigin
@Log4j2
public class PayrollSummaryController extends PayrollBaseController {

    private @Autowired PayrollSummaryService payrollSummaryService;

    @PostMapping(value = "/batch:Create")
    public ResponseEntity<?> createSummary(@RequestHeader HttpHeaders headers, @RequestBody List<BatchOperationDTO> batchOperationDTOList) {
        try {
            payrollSummaryService.create(batchOperationDTOList);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("monthly.payroll.create.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error creating payroll details : {}", e.getLocalizedMessage(), e);
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("monthly.payroll.create.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestHeader HttpHeaders headers, @RequestBody @Valid PayrollSummaryRequestDTO baseDashboardSearchDTO) {
        try {
            PaginatedDataResponse<MonthlySalarySummaryDashboardDTO> monthlyPayrollSummary = payrollSummaryService.getMonthlyPayrollSummary(baseDashboardSearchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("payroll.dashboard.fetch.success"), monthlyPayrollSummary, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching payroll dashboard data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("payroll.dashboard.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/batch:Lock")
    public ResponseEntity<?> lockSummary(@RequestHeader HttpHeaders headers, @RequestBody BatchOperationDTO batchOperationDTO) {
        try {
            payrollSummaryService.lockMonthlyPayroll(batchOperationDTO.getPayrollIds());
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("payroll.locked.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error Locking payroll summary: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("payroll.lock.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/batch:Publish")
    public ResponseEntity<?> publishSummary(@RequestHeader HttpHeaders headers, @RequestBody BatchOperationDTO batchOperationDTO) {
        try {
            payrollSummaryService.publish(batchOperationDTO.getPayrollIds());
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("payroll.publish.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching payroll dashboard data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("payroll.publish.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/batch/rollback-to-draft")
    public ResponseEntity<?> rollBackToDraft(@RequestHeader HttpHeaders headers, @RequestBody BatchOperationDTO batchOperationDTO) {
        try {
            payrollSummaryService.rollBackToDraft(batchOperationDTO.getPayrollIds());
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("payroll.roll.back.lock.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching payroll dashboard data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("payroll.roll.back.lock.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/batch/rollback-to-estimated")
    public ResponseEntity<?> rollBackToEstimated(@RequestHeader HttpHeaders headers, @RequestBody BatchOperationDTO batchOperationDTO) {
        try {
            payrollSummaryService.rollBackToEstimate(batchOperationDTO.getPayrollIds());
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("payroll.roll.back.draft.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching payroll dashboard data: {}", e.getLocalizedMessage(), e);
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("payroll.roll.back.draft.failed"), HttpStatus.BAD_REQUEST);
        }
    }

}
