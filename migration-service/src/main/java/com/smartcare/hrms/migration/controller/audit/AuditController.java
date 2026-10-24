package com.smartcare.hrms.migration.controller.audit;

import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.migration.config.MessageResource;
import com.smartcare.hrms.migration.service.audit.UploadAuditService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value="/audit")
@CrossOrigin
@Log4j2
public class AuditController {

    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;
    private @Autowired UploadAuditService uploadAuditService;

    @GetMapping("/upload/latest")
    public ResponseEntity<?> getLatestUpload(@RequestHeader HttpHeaders headers) {

        try {
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("fetch.api.success"), uploadAuditService.getLatestUploadsForAllTypes(), HttpStatus.OK);
        }catch (Exception e){
            String cause=e.getLocalizedMessage();
            e.printStackTrace();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
