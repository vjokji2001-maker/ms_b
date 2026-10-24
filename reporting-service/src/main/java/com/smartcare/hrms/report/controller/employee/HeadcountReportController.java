package com.smartcare.hrms.report.controller.employee;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.report.config.MessageResource;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.service.employee.HeadcountReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/headcount")
@CrossOrigin("*")
@Log4j2
public class HeadcountReportController {

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private HeadcountReportService headcountReportService;

    @PostMapping("/get/report")
    public ResponseEntity<?> getReport(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto) {

        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("headcount.report.fetch.success"), headcountReportService.getHeadcountReport(dto), HttpStatus.OK);

        } catch (Exception e) {
             e.printStackTrace();
            return responseGenerator.errorResponse(headers, e.getLocalizedMessage(), messageResource.getEnglishMessage("headcount.report.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/export/excel")
    public void exportExcel(@RequestHeader HttpHeaders headers, @RequestBody EmployeeReposrtSearchDTO dto, HttpServletResponse response) throws Exception {

        try {

            headcountReportService.exportHeadcountExcel(dto, response);

        } catch (Exception e) {

            log.error("Headcount Report Excel Export Failed", e);
            throw new RuntimeException("Failed to download Headcount Report");
        }
    }

}
