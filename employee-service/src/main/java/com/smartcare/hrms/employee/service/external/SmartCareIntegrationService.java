package com.smartcare.hrms.employee.service.external;

import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.utility.Constants;
import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.dto.external.UserMDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class SmartCareIntegrationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MessageResource messageResource;

    public void createUserInSmartcare(UserMDTO userDto, Map<String, String> dynamicHeaders) {

        String url = Constants.SMARTCARE_SAVE_USER_ENDPOINT;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (dynamicHeaders != null) dynamicHeaders.forEach(headers::set);
            HttpEntity<UserMDTO> requestEntity = new HttpEntity<>(userDto, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ValidationException(messageResource.getEnglishMessage("unable.to.create"));
            }
        } catch (Exception e) {
            throw new ValidationException(messageResource.getEnglishMessage("smart.care.integration.failed"));
        }
    }
}
