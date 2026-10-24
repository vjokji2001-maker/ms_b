package com.smartcare.hrms.hiring.model.entity;

import com.smartcare.hrms.hiring.model.entity.enums.CandidateApplicationStatus;
import com.smartcare.hrms.hiring.model.entity.enums.CandidateFinalStatus;
import com.smartcare.hrms.hiring.model.entity.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hiring_candidate_application")
public class CandidateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false, unique = true)
    private String candidateId;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "gender")
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "experience")
    private String experience;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "current_ctc")
    private String currentCtc;

    @Column(name = "expected_ctc")
    private String expectedCtc;

    @Column(name = "notice_period")
    private String noticePeriod;

    @Column(name = "applied_position", nullable = false)
    private String appliedPosition;

    @Column(name = "hiring_request_id", columnDefinition = "BINARY(16)")
    private Long hiringRequestId;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    @Column(name = "resume_file_path")
    private String resumeFilePath;

    @Column(name = "address")
    private String address;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "department")
    private String department;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_status", nullable = false)
    private InterviewStatus interviewStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", nullable = false)
    private CandidateFinalStatus finalStatus;

    @PrePersist
    public void prePersist() {

        if (candidateId == null || candidateId.trim().isEmpty()) {
            candidateId = "CAND-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();
        }

        if (currentRound == null) {
            currentRound = 1;
        }

        if (interviewStatus == null) {
            interviewStatus = InterviewStatus.PENDING;
        }

        if (finalStatus == null) {
            finalStatus = CandidateFinalStatus.APPLIED;
        }

        if (appliedDate == null) {
            appliedDate = LocalDate.now();
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}