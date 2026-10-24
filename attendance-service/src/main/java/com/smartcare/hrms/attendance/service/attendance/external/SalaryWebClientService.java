package com.smartcare.hrms.attendance.service.attendance.external;

import com.smartcare.hrms.attendance.dto.attendance.AttendanceSalaryPayloadDTO;
import com.smartcare.hrms.common.utility.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Log4j2
public class SalaryWebClientService {

    private final WebClient webClient;


    public SalaryWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void createEmployeeAttendanceSalaryLink(List<AttendanceSalaryPayloadDTO> salaryPayloadDTOS, HttpHeaders headers){

        try{

            String response = webClient.post()
                    .uri(Constants.SALARY_ATTENDANCE_SAVE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders -> {
                        httpHeaders.addAll(headers);

                        // ✅ ensure required headers exist
                        httpHeaders.set("clinicid", headers.getFirst("clinicid"));
                        httpHeaders.set("userid", headers.getFirst("userid"));
                        httpHeaders.set("zoneid", headers.getFirst("zoneid"));
                    })
                    .bodyValue(salaryPayloadDTOS)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(body -> log.error(" Exact Error from Payroll Service: {}", body))
                                    .flatMap(body -> Mono.error(new RuntimeException(body)))
                    )
                    .bodyToMono(String.class)
                    .block();



        }catch (Exception e){
            e.printStackTrace();
            log.error("employee attendance salary link creation failed", e);
        }
    }
}
