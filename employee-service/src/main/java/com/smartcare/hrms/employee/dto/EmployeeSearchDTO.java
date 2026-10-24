package com.smartcare.hrms.employee.dto;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class EmployeeSearchDTO {

    private PaginationInfo paginationInfo;
    private String name;
    private String code;
    private Long companyId;
    private Long branchId;
    private Long departmentId;
    private Long designationId;
    private Long workLocationId;
    private Long categoryId;
    private Long gradeId;
}
