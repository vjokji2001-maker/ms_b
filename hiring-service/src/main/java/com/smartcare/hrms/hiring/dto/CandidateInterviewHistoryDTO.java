package com.smartcare.hrms.hiring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateInterviewHistoryDTO {

    private Long candidateApplicationId;
    private Integer roundNumber;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String interviewStatus;
    private String result;
    private String feedback;
    private Double rating;
    private String remarks;
}