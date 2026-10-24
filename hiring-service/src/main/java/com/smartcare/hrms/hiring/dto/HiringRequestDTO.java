package com.smartcare.hrms.hiring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.smartcare.hrms.hiring.model.entity.enums.Priority;
import com.smartcare.hrms.hiring.model.entity.enums.RequestStatus;
import com.smartcare.hrms.hiring.model.entity.enums.EmploymentType;


import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiringRequestDTO {

    private Long id;
    private String requestId;

    private Long departmentId;
    private Long designationId;
    private Long countryId;
    private Long stateId;
    private Long cityId;
    private Integer vacancies;
    private EmploymentType employmentType;
    private String experience;
    private LocalDate joiningDate;
    private Priority priority;
    private String reportingManager;
    private String reason;
    private String jobDescription;
    private String remarks;

    private Integer hiredCount;
    private RequestStatus status;

    private String createdDate;
    private String createdBy;

}