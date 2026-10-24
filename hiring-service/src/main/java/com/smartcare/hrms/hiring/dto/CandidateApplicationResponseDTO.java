package com.smartcare.hrms.hiring.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationResponseDTO {

    private Long id;
    private String candidateId;
    private String candidateName;
    private String mobileNumber;
    private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private String experience;
    private String currentCompany;
    private String currentCtc;
    private String expectedCtc;
    private String noticePeriod;
    private String appliedPosition;
    private Long hiringRequestId;
    private String resumeFileName;
    private String resumeFilePath;
    private String address;
    private String remarks;
    private String status;
    private LocalDate appliedDate;
}