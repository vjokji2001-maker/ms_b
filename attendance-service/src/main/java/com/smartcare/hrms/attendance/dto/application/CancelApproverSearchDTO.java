package com.smartcare.hrms.attendance.dto.application;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class CancelApproverSearchDTO {

    private String requestType;
    private String approverEmpId;
    private String name;
    private String code;
    private Long   companyId;
    private Long   branchId;
    private Long   departmentId;
    private Long   designationId;
    private Long   workLocationId;
    private Long   categoryId;
    private Long   gradeId;
    private String fromDate;
    private String toDate;
    private String status;
    private PaginationInfo paginationInfo;
}
