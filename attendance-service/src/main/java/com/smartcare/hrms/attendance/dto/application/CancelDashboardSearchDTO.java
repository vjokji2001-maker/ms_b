package com.smartcare.hrms.attendance.dto.application;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class CancelDashboardSearchDTO {
    private String empId;
    private String fromDate;
    private String toDate;
    private String status;

    private PaginationInfo paginationInfo;

}
