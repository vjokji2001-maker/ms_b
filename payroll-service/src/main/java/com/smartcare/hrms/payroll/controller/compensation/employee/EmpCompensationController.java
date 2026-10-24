package com.smartcare.hrms.payroll.controller.compensation.employee;

import com.smartcare.hrms.common.dto.BaseDashboardSearchDTO;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmployeeCompensationDashboardDTO;
import com.smartcare.hrms.payroll.service.compensation.employee.EmployeeCompensationService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/emp/compensation")
@CrossOrigin
@Log4j2
public class EmpCompensationController extends PayrollBaseController {

    private @Autowired EmployeeCompensationService employeeCompensationService;

    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {
        try {
            EmpCompensationDTO byId = employeeCompensationService.getById(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.compensation.fetch.success"), byId, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.compensation.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody BaseDashboardSearchDTO baseDashboardSearchDTO) {
        try {
            PaginatedDataResponse<EmployeeCompensationDashboardDTO> dashboardData = employeeCompensationService.getDashboardData(baseDashboardSearchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.compensation.dashboard.fetch.success"), dashboardData, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.compensation.dashboard.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody @Valid EmpCompensationDTO requestDTO) {
        try {
            employeeCompensationService.create(requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.compensation.created.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.compensation.creation.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/update/{employeeId}")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @PathVariable("employeeId") String employeeId, @RequestBody EmpCompensationDTO requestDTO) {
        try {
            employeeCompensationService.update(employeeId, requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("emp.compensation.updated.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("emp.compensation.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
