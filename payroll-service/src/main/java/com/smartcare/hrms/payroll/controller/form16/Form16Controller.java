package com.smartcare.hrms.payroll.controller.form16;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.payroll.dto.form16.Form16DashboardDTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DashboardRequestDTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DetailsRequestDTO;
import com.smartcare.hrms.payroll.service.form16.Form16Service;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/emp/form16")
@CrossOrigin("*")
@Log4j2
public class Form16Controller extends PayrollBaseController {

    private @Autowired Form16Service form16Service;

    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader HttpHeaders headers,
            @RequestBody Form16DashboardRequestDTO requestDTO) {

        try {

            Form16DashboardDTO response =
                    form16Service.getDashboardSummary(
                            requestDTO
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    messageResource.getEnglishMessage(
                            "form16.dashboard.fetch.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            log.error(
                    "Error fetching Form 16 dashboard: {}",
                    e.getLocalizedMessage(),
                    e
            );

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    messageResource.getEnglishMessage(
                            "form16.dashboard.fetch.failed"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping(value = "/get/all")
    public ResponseEntity<?> getAll(
            @RequestHeader HttpHeaders headers,
            @RequestBody Form16DashboardRequestDTO requestDTO) {

        try {

            List<Form16DTO> response =
                    form16Service.getForm16EmployeeData(
                            requestDTO
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    messageResource.getEnglishMessage(
                            "form16.employee.fetch.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            log.error(
                    "Error fetching Form 16 employee data: {}",
                    e.getLocalizedMessage(),
                    e
            );

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    messageResource.getEnglishMessage(
                            "form16.employee.fetch.failed"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping(value = "/get/{employeeId}")
    public ResponseEntity<?> getByEmployeeId(
            @RequestHeader HttpHeaders headers,
            @PathVariable String employeeId,
            @RequestParam String financialYear) {

        try {

            Form16DTO response =
                    form16Service.getForm16EmployeeDetails(
                            employeeId,
                            financialYear
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    messageResource.getEnglishMessage(
                            "form16.details.fetch.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            log.error(
                    "Error fetching Form 16 details: {}",
                    e.getLocalizedMessage(),
                    e
            );

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    messageResource.getEnglishMessage(
                            "form16.details.fetch.failed"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping(value = "/get/details")
    public ResponseEntity<?> getDetails(
            @RequestHeader HttpHeaders headers,
            @RequestBody Form16DetailsRequestDTO requestDTO) {

        try {

            Form16DTO response =
                    form16Service.getForm16EmployeeDetails(
                            requestDTO.getEmployeeId(),
                            requestDTO.getFinancialYear()
                    );

            return responseGenerator.postSuccessResponse(
                    headers,
                    messageResource.getEnglishMessage(
                            "form16.details.fetch.success"
                    ),
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            log.error(
                    "Error fetching Form 16 details: {}",
                    e.getLocalizedMessage(),
                    e
            );

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    messageResource.getEnglishMessage(
                            "form16.details.fetch.failed"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}