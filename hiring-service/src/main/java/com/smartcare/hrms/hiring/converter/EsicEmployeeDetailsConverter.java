package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.EsicEmployeeDetailsDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EsicEmployeeDetailsConverter {

    public EsicEmployeeDetailsDTO convertToDTO(
            String employeeCode,
            String employeeName,
            String esicNumber,
            String department,
            BigDecimal wages,
            BigDecimal employeeShare,
            BigDecimal employerShare,
            BigDecimal total,
            String status) {

        return new EsicEmployeeDetailsDTO(
                employeeCode,
                employeeName,
                esicNumber,
                department,
                wages,
                employeeShare,
                employerShare,
                total,
                status
        );
    }
}