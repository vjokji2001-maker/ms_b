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
public class InterviewScheduleDTO {

    private LocalDate interviewDate;

    private LocalTime interviewTime;

    private Integer currentRound;
}