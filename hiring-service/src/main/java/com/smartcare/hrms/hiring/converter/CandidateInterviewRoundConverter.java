package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.CandidateInterviewHistoryDTO;
import com.smartcare.hrms.hiring.dto.InterviewResultDTO;
import com.smartcare.hrms.hiring.dto.InterviewScheduleDTO;
import com.smartcare.hrms.hiring.model.entity.CandidateInterviewRound;
import com.smartcare.hrms.hiring.model.entity.enums.InterviewResult;
import org.springframework.stereotype.Component;

@Component
public class CandidateInterviewRoundConverter {

    public CandidateInterviewRound toEntity(
            InterviewScheduleDTO dto,
            Long candidateApplicationId,
            Integer roundNumber) {

        return CandidateInterviewRound.builder()
                .candidateApplicationId(candidateApplicationId)
                .roundNumber(roundNumber)
                .interviewDate(dto.getInterviewDate())
                .interviewTime(dto.getInterviewTime())
                .build();
    }

    public InterviewScheduleDTO toScheduleDTO(
            CandidateInterviewRound entity) {

        return InterviewScheduleDTO.builder()
                .interviewDate(entity.getInterviewDate())
                .interviewTime(entity.getInterviewTime())
                .currentRound(entity.getRoundNumber())
                .build();
    }

    public void updateResult(
            CandidateInterviewRound entity,
            InterviewResultDTO dto) {

        entity.setResult(
                InterviewResult.valueOf(dto.getResult().toUpperCase())
        );

        entity.setFeedback(dto.getFeedback());
        entity.setRating(dto.getRating());
        entity.setRemarks(dto.getRemarks());

        if (dto.getNextInterviewDate() != null) {
            entity.setInterviewDate(dto.getNextInterviewDate());
        }

        if (dto.getNextInterviewTime() != null) {
            entity.setInterviewTime(dto.getNextInterviewTime());
        }
    }
    public CandidateInterviewHistoryDTO toHistoryDTO(
            CandidateInterviewRound entity) {

        return CandidateInterviewHistoryDTO.builder()
                .candidateApplicationId(entity.getCandidateApplicationId())
                .roundNumber(entity.getRoundNumber())
                .interviewDate(entity.getInterviewDate())
                .interviewTime(entity.getInterviewTime())
                .interviewStatus(
                        entity.getInterviewStatus() != null
                                ? entity.getInterviewStatus().name()
                                : null
                )
                .result(
                        entity.getResult() != null
                                ? entity.getResult().name()
                                : null
                )
                .feedback(entity.getFeedback())
                .rating(entity.getRating())
                .remarks(entity.getRemarks())
                .build();
    }
}