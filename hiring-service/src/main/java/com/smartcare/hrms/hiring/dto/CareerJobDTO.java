package com.smartcare.hrms.hiring.dto;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareerJobDTO {

    private Long id;
    private String requestId;

    private Long departmentId;
    private Long designationId;

    private Long countryId;
    private Long stateId;
    private Long cityId;

    private String jobDescription;
    private Integer vacancies;
    private String employmentType;
    private String experience;
    private String joiningDate;
    private String priority;
    private Integer hiredCount;
    private String status;
}