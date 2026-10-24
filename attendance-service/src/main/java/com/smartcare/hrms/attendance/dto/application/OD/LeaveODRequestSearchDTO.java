package com.smartcare.hrms.attendance.dto.application.OD;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class LeaveODRequestSearchDTO {
    private PaginationInfo paginationInfo;
    private String reqEmpId;
    private String fromDate;
    private String toDate;
    private String status;


}
