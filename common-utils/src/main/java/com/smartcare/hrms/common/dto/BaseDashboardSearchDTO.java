package com.smartcare.hrms.common.dto;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDashboardSearchDTO {
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
    private String jobTitle;
    private String empId;
}
