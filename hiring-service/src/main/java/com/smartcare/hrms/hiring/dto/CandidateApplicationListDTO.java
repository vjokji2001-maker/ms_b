package com.smartcare.hrms.hiring.dto;

import com.smartcare.hrms.hiring.model.entity.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationListDTO {

    private Long id;
    private String candidateId;
    private String candidateName;
    private String mobileNumber;
    private String email;
    private String appliedPosition;
    private String department;
    private String experience;
    private Integer currentRound;
    private InterviewStatus interviewStatus;
    private String finalStatus;
    private LocalDate appliedDate;
}