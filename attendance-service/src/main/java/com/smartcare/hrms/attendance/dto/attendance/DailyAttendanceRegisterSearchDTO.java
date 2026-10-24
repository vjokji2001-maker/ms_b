package com.smartcare.hrms.attendance.dto.attendance;

import com.smartcare.hrms.common.pagination.PaginationInfo;
import lombok.Data;

@Data
public class DailyAttendanceRegisterSearchDTO  {

    private String fromDate;
    private String toDate;
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
    private PaginationInfo paginationInfo;
}
