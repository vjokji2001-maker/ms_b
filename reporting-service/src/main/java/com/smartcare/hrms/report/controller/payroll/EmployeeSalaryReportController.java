package com.smartcare.hrms.report.controller.payroll;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.report.config.MessageResource;
import com.smartcare.hrms.report.config.ReportingBaseController;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeSearchDTO;
import com.smartcare.hrms.report.service.payroll.EmployeeSalaryReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/salary")
@CrossOrigin("*")
@Log4j2
public class EmployeeSalaryReportController extends ReportingBaseController {

    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;
    private @Autowired EmployeeSalaryReportService  employeeSalaryReportService;

    @PostMapping("/get/report")
    public ResponseEntity<?> getReport(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSearchDTO dto){
        try {
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("salary.data.fetch.success"),employeeSalaryReportService.getSalaryReport(dto), HttpStatus.OK );
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("salary.data.failed.to.fetch"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/export/excel")
    public void exportExcel( @RequestHeader HttpHeaders headers,@RequestBody EmployeeSearchDTO dto, HttpServletResponse response) throws Exception {
        try {
            employeeSalaryReportService.exportSalaryExcel(dto, response);
        } catch (Exception e) {
            log.error("Excel Export Failed", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to download Salary report");
        }
    }
}
