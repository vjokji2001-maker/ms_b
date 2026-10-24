package com.smartcare.hrms.payroll.dto.statutory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeStatutoryDTO {
    private String id;
    private String payrollState;
    private String empName;
    private String gender;
    private String empId;
    private String uan;
    private String pfNumber;
    private LocalDate pfJoiningDate;
    private String pfZone;
    private Boolean pfVerified;
    private Boolean pfApplicable;
    private Boolean esicApplicable;
    private String esicNumber;
    private LocalDate esicJoiningDate;
    private String esicZone;
    private Boolean esicVerified;
    private Boolean npsApplicable;
    private LocalDate npsJoinDate;
    private String npsSchemeType;
    private Boolean npsVerified;
    private Boolean ptApplicable;
    private String paymentMethod;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
}
