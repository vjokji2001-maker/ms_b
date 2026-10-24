package com.smartcare.hrms.master.controller.employment.meta;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.employment.meta.MetaEmploymentDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.employment.meta.MetaEmploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class MetaEmploymentController {


    @Autowired
    private MessageResource messageResource;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Autowired
    private MetaEmploymentService metaEmploymentService;


    @GetMapping(value = "/employment/metadata")
    public ResponseEntity<?> getAllEmploymentMasterData(@RequestHeader HttpHeaders headers){
        try {
            MetaEmploymentDTO metaEmploymentDTO = metaEmploymentService.getAllEmploymentMasterData();
            return responseGenerator.getSuccessResponse(metaEmploymentDTO, messageResource.getEnglishMessage("fetch.api.success"), HttpStatus.OK);
        }catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
