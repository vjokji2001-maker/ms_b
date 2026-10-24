package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.PfChallanListDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PfChallanConverter {

    public PfChallanListDTO convertToDTO(
            String challanNumber,
            String month,
            String financialYear,
            Integer employees,
            BigDecimal employeePF,
            BigDecimal employerPF,
            BigDecimal eps,
            BigDecimal totalAmount,
            String status) {

        return new PfChallanListDTO(
                challanNumber,
                month,
                financialYear,
                employees,
                employeePF,
                employerPF,
                eps,
                totalAmount,
                status,
                null
        );
    }
}