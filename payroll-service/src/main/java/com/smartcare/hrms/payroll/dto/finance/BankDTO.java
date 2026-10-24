package com.smartcare.hrms.payroll.dto.finance;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDTO {

    private Long id;

    @DataRequired(message = "Bank name is required")
    private String name;

    private String shortName;

    @DataRequired(message = "Bank code is required")
    private String code;

    private Boolean isActive;
}