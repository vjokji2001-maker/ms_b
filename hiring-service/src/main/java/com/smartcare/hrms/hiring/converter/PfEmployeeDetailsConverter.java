package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.PfEmployeeDetailsDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PfEmployeeDetailsConverter {

    public PfEmployeeDetailsDTO convertToDTO(
            String employeeCode,
            String employeeName,
            String designation,
            String uan,
            String pfNumber,
            String department,
            BigDecimal basicDA,
            BigDecimal employeePF,
            BigDecimal employerPF,
            BigDecimal eps,
            BigDecimal total,
            String status) {

        return new PfEmployeeDetailsDTO(
                employeeCode,
                employeeName,
                designation,
                uan,
                pfNumber,
                department,
                basicDA,
                employeePF,
                employerPF,
                eps,
                total,
                status
        );
    }
}