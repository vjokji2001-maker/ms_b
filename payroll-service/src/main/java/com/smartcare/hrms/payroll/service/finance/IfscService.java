package com.smartcare.hrms.payroll.service.finance;


import com.smartcare.hrms.payroll.dto.finance.IfscDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class IfscService {

    private @Autowired WebClient webClient;

    /**
     * Fetch IFSC details (Blocking - used in current flow)
     */
    public IfscDetails fetchIfscDetails(String ifscCode) {


        try {
            String finalIfscCode = ifscCode;
            return webClient.get()
                    .uri("https://ifsc.razorpay.com/" + ifscCode)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Invalid IFSC: {}", finalIfscCode);
                                        return Mono.error(new RuntimeException("Invalid IFSC code"));
                                    })
                    )
                    .onStatus(status -> status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("IFSC API server error: {}", body);
                                        return Mono.error(new RuntimeException("IFSC API unavailable"));
                                    })
                    )
                    .bodyToMono(IfscDetails.class)
                    .block();

        } catch (Exception ex) {
            log.error("Error fetching IFSC {}: {}", ifscCode, ex.getMessage());
            throw new RuntimeException("Failed to fetch IFSC details", ex);
        }
    }



}
