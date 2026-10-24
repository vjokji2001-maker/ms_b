package com.smartcare.hrms.hiring.converter;

import com.smartcare.hrms.hiring.dto.CandidateApplicationDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationListDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationResponseDTO;
import com.smartcare.hrms.hiring.model.entity.CandidateApplication;

import org.springframework.stereotype.Component;

@Component
public class CandidateApplicationConverter {

    public CandidateApplication toEntity(
            CandidateApplicationDTO dto) {

        return CandidateApplication.builder()
                .candidateName(dto.getCandidateName())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .experience(dto.getExperience())
                .currentCompany(dto.getCurrentCompany())
                .currentCtc(dto.getCurrentCtc())
                .expectedCtc(dto.getExpectedCtc())
                .noticePeriod(dto.getNoticePeriod())
                .appliedPosition(dto.getAppliedPosition())
                .hiringRequestId(dto.getHiringRequestId())
                .address(dto.getAddress())
                .remarks(dto.getRemarks())
                .build();
    }

    public CandidateApplicationResponseDTO toDTO(
            CandidateApplication entity) {

        CandidateApplicationResponseDTO dto =
                CandidateApplicationResponseDTO.builder()
                        .id(entity.getId())
                        .candidateId(entity.getCandidateId())
                        .candidateName(entity.getCandidateName())
                        .mobileNumber(entity.getMobileNumber())
                        .email(entity.getEmail())
                        .gender(entity.getGender())
                        .dateOfBirth(entity.getDateOfBirth())
                        .experience(entity.getExperience())
                        .currentCompany(entity.getCurrentCompany())
                        .currentCtc(entity.getCurrentCtc())
                        .expectedCtc(entity.getExpectedCtc())
                        .noticePeriod(entity.getNoticePeriod())
                        .appliedPosition(entity.getAppliedPosition())
                        .hiringRequestId(entity.getHiringRequestId())
                        .resumeFileName(entity.getResumeFileName())
                        .resumeFilePath(entity.getResumeFilePath())
                        .address(entity.getAddress())
                        .remarks(entity.getRemarks())
                        .appliedDate(entity.getAppliedDate())
                        .build();

        return dto;
    }

    public CandidateApplicationListDTO toListDTO(
            CandidateApplication entity) {

        return CandidateApplicationListDTO.builder()
                .id(entity.getId())
                .candidateId(entity.getCandidateId())
                .candidateName(entity.getCandidateName())
                .mobileNumber(entity.getMobileNumber())
                .email(entity.getEmail())
                .appliedPosition(entity.getAppliedPosition())
                .department(entity.getDepartment())
                .experience(entity.getExperience())
                .currentRound(entity.getCurrentRound())
                .interviewStatus(entity.getInterviewStatus())
                .finalStatus(
                        entity.getFinalStatus() != null
                                ? entity.getFinalStatus().name()
                                : null
                )
                .appliedDate(entity.getAppliedDate())
                .build();
    }
}