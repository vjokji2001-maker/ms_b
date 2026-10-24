package com.smartcare.hrms.attendance.controller.attendance;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceDetailsDTO;
import com.smartcare.hrms.attendance.dto.attendance.EmpAttendanceDTO;
import com.smartcare.hrms.attendance.dto.attendance.MyAttendanceDTO;
import com.smartcare.hrms.attendance.service.attendance.AttendanceCalculationService;
import com.smartcare.hrms.attendance.service.attendance.MyAttendanceService;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/my/attendance")
@Log4j2
public class MyAttendanceController {

    private @Autowired ResponseGenerator responseGenerator;

    private @Autowired MessageResource messageResource;

    private @Autowired MyAttendanceService myAttendanceService;

    private @Autowired AttendanceCalculationService attendanceCalculationService;


    @PostMapping("/calculate/{monthYear}")
    public ResponseEntity<?> calculateALlAttendance(@RequestHeader HttpHeaders headers,@PathVariable("monthYear")String monthYear, @RequestBody List<String> empIds){
        try{

            Integer month = Integer.valueOf(monthYear.split("-")[0]);
            Integer year = Integer.valueOf(monthYear.split("-")[1]);

            Map<String, List<EmpAttendanceDTO>> result = attendanceCalculationService
                    .calculateMonthlyAttendanceForAll(empIds, year, month);

            return  responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("calculate.api.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause=e.getLocalizedMessage();
            return  responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("calculate.api.failed"), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/employee/get/{employeeId}/{monthYear}")
    public ResponseEntity<?> getAttendanceByEmployeeAndMonth(@RequestHeader HttpHeaders headers, @PathVariable("employeeId") String empId,@PathVariable("monthYear")String monthYear) {
        try {

            Integer month = Integer.valueOf(monthYear.split("-")[0]);
            Integer year = Integer.valueOf(monthYear.split("-")[1]);

            List<MyAttendanceDTO> attendanceList = myAttendanceService.getAttendanceByEmployeeAndMonth(empId, year, month);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("fetch.api.success"), attendanceList, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{employeeId}/{date}")
    public ResponseEntity<?> getAttendanceDetails(@RequestHeader HttpHeaders headers, @PathVariable("employeeId") String employeeId, @PathVariable("date") String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            AttendanceDetailsDTO attendanceDetailsDTO = myAttendanceService.getAttendanceDetails(employeeId, parsedDate);
            return responseGenerator.getSuccessResponse(attendanceDetailsDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/lock/attendance/{empId}")
    public ResponseEntity<?> lockAttendance(@RequestHeader HttpHeaders headers, @PathVariable("empId") String empId, @RequestBody List<String> dates){
        try {
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("data.locked.success"), myAttendanceService.lockAttendance(empId,dates), HttpStatus.OK);
        } catch (Exception e) {
          String cause=e.getLocalizedMessage();
          return  responseGenerator.errorResponse(headers,cause, messageResource.getEnglishMessage("data.locked.failed"),HttpStatus.BAD_REQUEST );
        }
    }


}
