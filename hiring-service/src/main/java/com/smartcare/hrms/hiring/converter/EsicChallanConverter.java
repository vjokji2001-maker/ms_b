package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.EsicChallanListDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EsicChallanConverter {

    public EsicChallanListDTO convertToDTO(
            String challanNumber,
            String month,
            Integer employees,
            BigDecimal employeeShare,
            BigDecimal employerShare,
            BigDecimal totalAmount,
            String status) {

        return new EsicChallanListDTO(
                challanNumber,
                month,
                employees,
                employeeShare,
                employerShare,
                totalAmount,
                status,
                null
        );
    }
}