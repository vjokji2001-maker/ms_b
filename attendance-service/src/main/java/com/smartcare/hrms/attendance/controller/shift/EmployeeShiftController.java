package com.smartcare.hrms.attendance.controller.shift;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDashboardDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftSearchDTO;
import com.smartcare.hrms.attendance.service.shift.EmployeeShiftService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shift")
@Log4j2
@CrossOrigin
public class EmployeeShiftController {

    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;
    private @Autowired EmployeeShiftService employeeShiftService;

    @PostMapping("/get/dashboard")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers, @RequestBody EmployeeShiftSearchDTO dto){
        try {
            PaginatedDataResponse<EmployeeShiftDashboardDTO> responseData = employeeShiftService.getDashboardData(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), responseData, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause,  messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping ("/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody List<EmployeeShiftDTO> dtoList){
        try {
            dtoList.forEach(dto -> employeeShiftService.update(dtoList));
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
        }catch (Exception e){
            String cause= e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("update.api.failed"),  HttpStatus.BAD_REQUEST);
        }
    }
}
