package com.smartcare.hrms.report.modal.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadcountReportDTO {

    private String departmentCode;
    private String departmentName;

    private Long currentHeadcount;
    private Long activeEmployees;
    private Long probationEmployees;
    private Long confirmedEmployees;
    private Long permanentEmployees;
    private Long contractEmployees;

    private Long maleEmployees;
    private Long femaleEmployees;

    private Long newJoiners;

    private Long employeesOnNoticePeriod;

    private Long resignedEmployees;
    private Long terminatedEmployees;
    private Long exitedEmployees;

    private String branchName;
    private String workLocation;

    private Date lastUpdatedDate;
}
