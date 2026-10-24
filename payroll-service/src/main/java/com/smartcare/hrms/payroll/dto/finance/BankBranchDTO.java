package com.smartcare.hrms.payroll.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankBranchDTO {

    private Long id;

    private Long bankId;
    private String bankName;

    private String name;
    private String ifscCode;

    private String city;
    private String state;

    private IfscDetails ifscDetails;

    private Boolean isActive;
}
