package com.smartcare.hrms.payroll.controller.compensation.template;

import com.smartcare.hrms.common.basecontroller.BaseController;
import com.smartcare.hrms.payroll.config.MessageResource;
import com.smartcare.hrms.common.dto.payroll.compensation.template.CompensationTemplateDTO;
import com.smartcare.hrms.payroll.service.compensation.template.CompensationTemplateService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/compensation/template")
@CrossOrigin
@Log4j2
public class CompensationTemplateController extends BaseController {
    private @Autowired MessageResource messageResource;
    private @Autowired CompensationTemplateService compensationTemplateService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @Valid @RequestBody CompensationTemplateDTO requestDTO) {
        try {
            compensationTemplateService.create(requestDTO);

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.template.created.successfully"), null, HttpStatus.OK);

        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.template.creation.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers) {
        try {
            List<CompensationTemplateDTO> compensationTemplateResponseDTOS = compensationTemplateService.getAll();
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.template.fetch.success"), compensationTemplateResponseDTOS, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.template.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        try {
            CompensationTemplateDTO compensationTemplateResponseDTO = compensationTemplateService.getById(id);
            return responseGenerator.getSuccessResponse(compensationTemplateResponseDTO, messageResource.getEnglishMessage("compensation.template.fetch.success"), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.template.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/update")
    public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @Valid @RequestBody CompensationTemplateDTO requestDTO) {
        try {
            compensationTemplateService.update(requestDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("compensation.template.updated.successfully"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("compensation.template.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
