package com.smartcare.hrms.report.modal.dto.attendance;

import lombok.Data;

@Data
public class EmployeeSearchDTO {

    private String fromDate;
    private String toDate;

    private String name;
    private String code;

    private Integer departmentId;
    private Integer workLocationId;
    private Integer gradeId;
    private Integer companyId;
    private Integer designationId;
    private Integer categoryId;
    private Integer branchId;

    private String monthYear;
}
