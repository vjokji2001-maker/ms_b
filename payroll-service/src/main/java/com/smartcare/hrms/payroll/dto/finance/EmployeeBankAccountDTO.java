package com.smartcare.hrms.payroll.dto.finance;


import com.smartcare.hrms.common.custom.annotations.DataRequired;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeBankAccountDTO {

    private String id;

    @DataRequired(message = "Employee Details is required")
    private String empId;

    @NotNull(message = "Bank ID is required")
    private Long bankId;

    @NotNull(message = "Ifsc Code is required")
     private String ifscCode;

    private Long branchId;

    @DataRequired(message = "Account Number is required",pattern = "^[0-9]{9,18}$")
    private String accountNumber;

    @DataRequired(message = "Account Holder Name is required",pattern = "^[a-zA-Z\\s]{2,150}$")
    private String accountHolderName;

    @DataRequired(message = "Account Type is required",pattern = "^(SAVINGS|CURRENT|SALARY)$")
    private String accountType;

    private Boolean isPrimary;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
