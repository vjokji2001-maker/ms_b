package com.smartcare.hrms.attendance.dto.application.swipe;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class EmployeeSwipeRequestSearchDTO {

    private PaginationInfo paginationInfo;
    private String reqEmpId;
    private String fromDate;
    private String toDate;
    private String status;
}
