package com.smartcare.hrms.attendance.dto.application.leave;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveRequestSearchDTO {

    private PaginationInfo paginationInfo;
    private String empId;

    private String fromDate;

    private String toDate;

    private String status;
}
