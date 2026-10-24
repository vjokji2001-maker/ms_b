package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeLeaveParentDTO {
    private Long id;
    private String employeeId;
    private Long yearId;
    private String balance;
    private Long leaveTemplateId;
    private List<EmployeeLeaveDtlsDTO> employeeLeaveDtlsDTO;
}
