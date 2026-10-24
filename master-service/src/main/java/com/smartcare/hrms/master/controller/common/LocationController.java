package com.smartcare.hrms.master.controller.common;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.common.CityDTO;
import com.smartcare.hrms.common.response.ResponseGenerator;
import com.smartcare.hrms.master.service.common.LocationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/location")
@CrossOrigin
@Log4j2
public class LocationController {

    private @Autowired LocationService locationService;
    private @Autowired ResponseGenerator responseGenerator;
    private @Autowired MessageResource messageResource;

    @GetMapping(value="/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers){
        try {
            CityDTO.LocationResponseDTO locationResponseDTO= locationService.getAll();
            return responseGenerator.postSuccessResponse(headers,messageResource.getEnglishMessage("detailed.fetch.api.success"), locationResponseDTO, HttpStatus.OK);
        }catch (Exception e){
            String cause=e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("detailed.fetch.api.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
