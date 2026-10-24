package com.smartcare.hrms.attendance.controller.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeShiftDetailDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestSearchDTO;
import com.smartcare.hrms.attendance.service.application.swipe.EmployeeSwipeRequestService;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.response.ResponseGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@Log4j2
@RequestMapping("/swipe/request")
public class EmployeeSwipeRequestController {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private EmployeeSwipeRequestService employeeSwipeRequestService;

    @Autowired
    private ResponseGenerator responseGenerator;


    @PostMapping(value = "/get/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSwipeRequestSearchDTO searchDTO) {
        try {
            PaginatedDataResponse<EmployeeSwipeRequestDashboardDTO> paginatedEmployeeList = employeeSwipeRequestService.getPaginatedSwipeRequestList(searchDTO);
            return responseGenerator.getSuccessResponse(paginatedEmployeeList, messageResource.getEnglishMessage("fetch.api.success") , HttpStatus.OK);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSwipeRequestDTO requestDto) {
        try {
            employeeSwipeRequestService.create(requestDto, headers.getFirst("clinicid"), headers.getFirst("empId"));
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("create.api.success"), null, HttpStatus.CREATED);
        } catch (Exception e){
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("create.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody EmployeeSwipeRequestDTO requestDto) {
        try {
            employeeSwipeRequestService.update(requestDto, headers.getFirst("clinicid"), headers.getFirst("empId"));
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("update.api.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("update.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader HttpHeaders headers, @PathVariable UUID id) {
        try {
            employeeSwipeRequestService.delete(id);
            return responseGenerator.getSuccessResponse(null, messageResource.getEnglishMessage("delete.api.success"), HttpStatus.OK);
        }catch (DataIntegrityViolationException e) {
            return responseGenerator.errorResponse(headers, messageResource.getEnglishMessage("linked.to.other.records"), messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("delete.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") String id) {
        try {
            EmployeeSwipeRequestDTO employeeSwipeRequestDTO = employeeSwipeRequestService.getById(id);
            return responseGenerator.getSuccessResponse(employeeSwipeRequestDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/shift/{date}/{empId}")
    public ResponseEntity<?> getByDate(@RequestHeader HttpHeaders headers, @PathVariable("date") String date, @PathVariable("empId") String empId) {
        try {
            EmployeeShiftDetailDTO employeeShiftDetailDTO = employeeSwipeRequestService.getEmpShiftByDate(date, empId);
            return responseGenerator.getSuccessResponse(employeeShiftDetailDTO, messageResource.getEnglishMessage("detailed.fetch.api.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
