package com.smartcare.hrms.payroll.controller.compensation.component;

import com.smartcare.hrms.common.basecontroller.BaseController;
import com.smartcare.hrms.payroll.config.MessageResource;
import com.smartcare.hrms.common.dto.payroll.compensation.component.ComponentDTO;
import com.smartcare.hrms.payroll.service.compensation.component.ComponentMasterService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/components")
@CrossOrigin
@Log4j2
public class ComponentMasterController extends BaseController {

    private @Autowired MessageResource messageResource;
    private @Autowired ComponentMasterService componentMasterService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @Valid @RequestBody ComponentDTO requestDTO) {
        try {
            componentMasterService.create(requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.head.master.created.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.head.master.creation.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers) {
        try {
            List<ComponentDTO> compensationHeadMasterResponseDTOS = componentMasterService.getAll();
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.head.master.fetch.success"), compensationHeadMasterResponseDTOS, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.head.master.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @Valid @RequestBody ComponentDTO requestDTO) {
        try {
            componentMasterService.update(requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.head.master.updated.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.head.master.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> get(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            ComponentDTO compensationHeadMasterResponseDTOS = componentMasterService.getById(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.head.master.fetch.success"), compensationHeadMasterResponseDTOS, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.head.master.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

}
