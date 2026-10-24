package com.smartcare.hrms.attendance.dto.application.swipe;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class SwipeRequestApproverSearchDTO {

    private PaginationInfo paginationInfo;
    private String name;
    private String code;
    private String approverEmpId;
    private Long companyId;
    private Long branchId;
    private Long departmentId;
    private Long designationId;
    private Long workLocationId;
    private Long categoryId;
    private Long gradeId;
    private String fromDate;
    private String toDate;
    private String status;
}
