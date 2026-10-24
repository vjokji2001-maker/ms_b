package com.smartcare.hrms.attendance.dto.shift;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeShiftSearchDTO {
    private PaginationInfo paginationInfo;
    private String fromDate;
    private String toDate;
    private String name;
    private String code;
    private Long companyId;
    private Long branchId;
    private Long designationId;
    private Long departmentId;
    private String jobTitle;
    private String empId;

}
