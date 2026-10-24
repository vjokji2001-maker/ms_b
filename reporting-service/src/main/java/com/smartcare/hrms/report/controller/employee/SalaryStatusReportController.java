package com.smartcare.hrms.report.controller.employee;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.report.config.MessageResource;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.service.employee.SalaryStatusReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/salary/status")
@CrossOrigin("*")
@Log4j2
public class SalaryStatusReportController {

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private SalaryStatusReportService salaryStatusReportService;

    @PostMapping("/get/report")
    public ResponseEntity<?> getReport(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto) {

        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("salary.status.report.fetch.success"), salaryStatusReportService.getSalaryStatusReport(dto), HttpStatus.OK);

        } catch (Exception e) {

            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("salary.status.report.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/export/excel")
    public void exportExcel(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto, HttpServletResponse response) throws Exception {

        try {

            salaryStatusReportService.exportSalaryStatusExcel(dto, response);

        } catch (Exception e) {

            log.error("Salary Status Report Excel Export Failed", e);
            throw new RuntimeException("Failed to download Salary Status Report");
        }
    }
}
