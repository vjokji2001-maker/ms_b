package com.smartcare.hrms.hiring.service;

import com.smartcare.hrms.hiring.converter.CandidateInterviewRoundConverter;
import com.smartcare.hrms.hiring.dao.CandidateApplicationDAO;
import com.smartcare.hrms.hiring.dao.CandidateInterviewRoundDAO;
import com.smartcare.hrms.hiring.dto.CandidateInterviewHistoryDTO;
import com.smartcare.hrms.hiring.dto.InterviewResultDTO;
import com.smartcare.hrms.hiring.dto.InterviewScheduleDTO;
import com.smartcare.hrms.hiring.model.entity.CandidateApplication;
import com.smartcare.hrms.hiring.model.entity.CandidateInterviewRound;
import com.smartcare.hrms.hiring.model.entity.enums.CandidateFinalStatus;
import com.smartcare.hrms.hiring.model.entity.enums.InterviewResult;
import com.smartcare.hrms.hiring.model.entity.enums.InterviewStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateInterviewService {

    private final CandidateApplicationDAO candidateApplicationDAO;
    private final CandidateInterviewRoundDAO candidateInterviewRoundDAO;
    private final CandidateInterviewRoundConverter candidateInterviewRoundConverter;
    private final InterviewEmailService interviewEmailService;

    public CandidateInterviewService(
            CandidateApplicationDAO candidateApplicationDAO,
            CandidateInterviewRoundDAO candidateInterviewRoundDAO,
            CandidateInterviewRoundConverter candidateInterviewRoundConverter,
            InterviewEmailService interviewEmailService) {

        this.candidateApplicationDAO = candidateApplicationDAO;
        this.candidateInterviewRoundDAO = candidateInterviewRoundDAO;
        this.candidateInterviewRoundConverter =
                candidateInterviewRoundConverter;
        this.interviewEmailService = interviewEmailService;
    }

    @Transactional
    public InterviewScheduleDTO scheduleInterview(
            Long candidateId,
            InterviewScheduleDTO dto) {

        CandidateApplication candidate =
                candidateApplicationDAO.getById(candidateId);

        if (candidate == null) {
            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        if (candidate.getFinalStatus() == CandidateFinalStatus.SELECTED ||
                candidate.getFinalStatus() == CandidateFinalStatus.REJECTED) {

            throw new RuntimeException(
                    "Interview cannot be scheduled after candidate is selected or rejected"
            );
        }

        if (dto == null || dto.getInterviewDate() == null) {
            throw new IllegalArgumentException(
                    "Interview date is required"
            );
        }

        if (dto.getInterviewDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Interview date cannot be in the past"
            );
        }

        Integer roundNumber = candidate.getCurrentRound();

        if (roundNumber == null || roundNumber < 1) {
            roundNumber = 1;
            candidate.setCurrentRound(roundNumber);
        }

        CandidateInterviewRound existingRound =
                candidateInterviewRoundDAO.getByCandidateAndRound(
                        candidateId,
                        roundNumber
                );

        if (existingRound != null &&
                existingRound.getInterviewStatus() ==
                        InterviewStatus.SCHEDULED) {

            throw new RuntimeException(
                    "Interview is already scheduled for this round"
            );
        }

        CandidateInterviewRound interviewRound;

        if (existingRound == null) {

            interviewRound =
                    candidateInterviewRoundConverter.toEntity(
                            dto,
                            candidateId,
                            roundNumber
                    );

            interviewRound.setInterviewStatus(
                    InterviewStatus.SCHEDULED
            );

            candidateInterviewRoundDAO.save(interviewRound);

        } else {

            existingRound.setInterviewDate(
                    dto.getInterviewDate()
            );

            existingRound.setInterviewTime(
                    dto.getInterviewTime()
            );

            existingRound.setInterviewStatus(
                    InterviewStatus.SCHEDULED
            );

            interviewRound =
                    candidateInterviewRoundDAO.update(
                            existingRound
                    );
        }

        candidate.setInterviewStatus(
                InterviewStatus.SCHEDULED
        );

        candidate.setFinalStatus(
                CandidateFinalStatus.IN_PROGRESS
        );

        candidateApplicationDAO.update(candidate);

        interviewEmailService.sendInterviewScheduledEmail(
                candidate,
                interviewRound
        );

        return candidateInterviewRoundConverter.toScheduleDTO(
                interviewRound
        );
    }

    @Transactional
    public void updateInterviewResult(
            Long candidateId,
            InterviewResultDTO dto) {

        CandidateApplication candidate =
                candidateApplicationDAO.getById(candidateId);

        if (candidate == null) {
            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        if (candidate.getFinalStatus() ==
                CandidateFinalStatus.SELECTED ||
                candidate.getFinalStatus() ==
                        CandidateFinalStatus.REJECTED) {

            throw new RuntimeException(
                    "Interview result cannot be changed after candidate is selected or rejected"
            );
        }

        if (dto == null ||
                dto.getResult() == null ||
                dto.getResult().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Interview result is required"
            );
        }

        Double rating = dto.getRating();

        if (rating != null &&
                (rating < 0 ||
                        rating > 5 ||
                        rating * 2 != Math.floor(rating * 2))) {

            throw new IllegalArgumentException(
                    "Rating must be between 0 and 5 in increments of 0.5"
            );
        }

        InterviewResult result;

        try {

            result = InterviewResult.valueOf(
                    dto.getResult().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid interview result"
            );
        }

        CandidateInterviewRound currentRound =
                candidateInterviewRoundDAO.getLatestRound(
                        candidateId
                );

        if (currentRound == null) {
            throw new RuntimeException(
                    "No interview round found for candidate"
            );
        }

        if (currentRound.getInterviewStatus() !=
                InterviewStatus.SCHEDULED) {

            throw new RuntimeException(
                    "Interview result can be updated only for a scheduled interview"
            );
        }

        currentRound.setResult(result);

        currentRound.setFeedback(
                dto.getFeedback()
        );

        currentRound.setRating(
                dto.getRating()
        );

        currentRound.setRemarks(
                dto.getRemarks()
        );

        currentRound.setInterviewStatus(
                InterviewStatus.COMPLETED
        );

        candidateInterviewRoundDAO.update(
                currentRound
        );

        switch (result) {

            case NEXT_ROUND:

                scheduleNextRound(
                        candidate,
                        dto
                );

                break;

            case SELECTED:

                candidate.setInterviewStatus(
                        InterviewStatus.COMPLETED
                );

                candidate.setFinalStatus(
                        CandidateFinalStatus.SELECTED
                );

                candidateApplicationDAO.update(
                        candidate
                );

                interviewEmailService.sendSelectedEmail(
                        candidate
                );

                break;

            case REJECTED:

                candidate.setInterviewStatus(
                        InterviewStatus.COMPLETED
                );

                candidate.setFinalStatus(
                        CandidateFinalStatus.REJECTED
                );

                candidateApplicationDAO.update(
                        candidate
                );

                interviewEmailService.sendRejectedEmail(
                        candidate
                );

                break;

            case HOLD:

                candidate.setInterviewStatus(
                        InterviewStatus.PENDING
                );

                candidate.setFinalStatus(
                        CandidateFinalStatus.ON_HOLD
                );

                candidateApplicationDAO.update(
                        candidate
                );

                break;

            default:

                throw new IllegalArgumentException(
                        "Unsupported interview result"
                );
        }
    }

    private void scheduleNextRound(
            CandidateApplication candidate,
            InterviewResultDTO dto) {

        if (dto.getNextInterviewDate() == null) {

            throw new IllegalArgumentException(
                    "Next interview date is required for next round"
            );
        }

        if (dto.getNextInterviewDate()
                .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Next interview date cannot be in the past"
            );
        }

        Integer nextRoundNumber =
                candidate.getCurrentRound() + 1;

        CandidateInterviewRound existingRound =
                candidateInterviewRoundDAO.getByCandidateAndRound(
                        candidate.getId(),
                        nextRoundNumber
                );

        if (existingRound != null) {

            throw new RuntimeException(
                    "Next interview round already exists"
            );
        }

        CandidateInterviewRound nextRound =
                CandidateInterviewRound.builder()
                        .candidateApplicationId(
                                candidate.getId()
                        )
                        .roundNumber(
                                nextRoundNumber
                        )
                        .interviewDate(
                                dto.getNextInterviewDate()
                        )
                        .interviewTime(
                                dto.getNextInterviewTime()
                        )
                        .interviewStatus(
                                InterviewStatus.SCHEDULED
                        )
                        .build();

        candidateInterviewRoundDAO.save(
                nextRound
        );

        candidate.setCurrentRound(
                nextRoundNumber
        );

        candidate.setInterviewStatus(
                InterviewStatus.SCHEDULED
        );

        candidate.setFinalStatus(
                CandidateFinalStatus.IN_PROGRESS
        );

        candidateApplicationDAO.update(
                candidate
        );

        interviewEmailService.sendNextRoundEmail(
                candidate,
                nextRound
        );
    }

    public List<CandidateInterviewHistoryDTO> getInterviewHistory(
            Long candidateId) {

        CandidateApplication candidate =
                candidateApplicationDAO.getById(candidateId);

        if (candidate == null) {

            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        List<CandidateInterviewRound> rounds =
                candidateInterviewRoundDAO.getAllByCandidate(
                        candidateId
                );

        return rounds.stream()
                .map(
                        candidateInterviewRoundConverter::toHistoryDTO
                )
                .collect(Collectors.toList());
    }
}