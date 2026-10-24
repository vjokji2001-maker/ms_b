package com.smartcare.hrms.report.controller.employee;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.report.config.MessageResource;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.service.employee.EmployeeMasterReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/master")
@CrossOrigin("*")
@Log4j2
public class EmployeeMasterReportController {
    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private EmployeeMasterReportService employeeMasterReportService;

    @PostMapping("/get/report")
    public ResponseEntity<?> getReport(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto) {

        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("employee.master.data.fetch.success"), employeeMasterReportService.getEmployeeMasterReport(dto), HttpStatus.OK);

        } catch (Exception e) {

            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("employee.master.data.failed.to.fetch"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/export/excel")
    public void exportExcel(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto, HttpServletResponse response) throws Exception {

        try {

            employeeMasterReportService.exportEmployeeMasterExcel(dto, response);

        } catch (Exception e) {

            log.error("Employee Master Excel Export Failed", e);
            throw new RuntimeException("Failed to download Employee Master Report");
        }
    }

}
