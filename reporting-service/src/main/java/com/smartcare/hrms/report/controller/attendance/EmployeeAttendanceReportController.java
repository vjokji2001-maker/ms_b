package com.smartcare.hrms.report.controller.attendance;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.report.config.MessageResource;
import com.smartcare.hrms.report.config.ReportingBaseController;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeSearchDTO;
import com.smartcare.hrms.report.service.attendance.EmployeeAttendanceReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/attendance")
@CrossOrigin("*")
@Log4j2
public class EmployeeAttendanceReportController extends ReportingBaseController {

    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;
    private @Autowired EmployeeAttendanceReportService reportService;

    @PostMapping("/get/report")
    public ResponseEntity<?> getReport(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSearchDTO dto){
        try {
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("attendance.data.fetch.success"),reportService.getReport(dto), HttpStatus.OK );
        } catch (Exception e) {
        String cause=e.getLocalizedMessage();
        return  responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("attendance.data.failed.to.fetch"),cause, HttpStatus.BAD_REQUEST );
        }
    }

    @PostMapping("/export/excel")
    public void exportExcel(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSearchDTO dto,HttpServletResponse response) {

        try {
            reportService.exportAttendanceExcel(dto, response);
        } catch (Exception e) {
            log.error("Excel Export Failed", e);
            throw new RuntimeException("Failed to download attendance report");
        }
    }
}
