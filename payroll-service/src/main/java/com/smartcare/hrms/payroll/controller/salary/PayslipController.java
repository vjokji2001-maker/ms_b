package com.smartcare.hrms.payroll.controller.salary;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.payroll.service.salary.PayslipService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value="/payslip")
@CrossOrigin("*")
@Log4j2
public class PayslipController extends PayrollBaseController {

    private @Autowired PayslipService payslipService;

    @PostMapping("/send-payslip")
    public ResponseEntity<?> sendPayslip(@RequestHeader HttpHeaders headers,
                                         @RequestBody Map<String, String> request) {
        try {

            String htmlContent = request.get("htmlContent");
            String email = request.get("email");
            String fileName = request.get("fileName");

            // ✅ Validation (recommended)
            if (htmlContent == null || email == null || fileName == null) {
                return responseGenerator.errorResponse(
                        headers,
                        "Invalid request data",
                        "Required fields missing",
                        HttpStatus.BAD_REQUEST
                );
            }

            payslipService.sendPayslip(htmlContent, email, fileName);

            return responseGenerator.postSuccessResponse(
                    headers,
                    messageResource.getEnglishMessage("payslip.send.success"),
                    "Payslip sent successfully",
                    HttpStatus.OK
            );

        } catch (Exception e) {
            log.error("Error sending payslip: {}", e.getLocalizedMessage(), e);

            return responseGenerator.errorResponse(
                    headers,
                    e.getLocalizedMessage(),
                    messageResource.getEnglishMessage("payslip.send.failed"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
