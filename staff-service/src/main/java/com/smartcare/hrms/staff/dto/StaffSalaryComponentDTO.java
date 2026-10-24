package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffSalaryComponentDTO {

    private String componentName;
    private String componentCode;
    private String componentType; // EARNING / DEDUCTION
    private Double amount;
}
