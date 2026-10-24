package com.smartcare.hrms.hiring.model.entity;

import com.smartcare.hrms.hiring.model.entity.enums.InterviewResult;
import com.smartcare.hrms.hiring.model.entity.enums.InterviewStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "hiring_candidate_interview_round",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_round",
                        columnNames = {
                                "candidate_application_id",
                                "round_number"
                        }
                )
        }
)
public class CandidateInterviewRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "candidate_application_id",
            nullable = false
    )
    private Long candidateApplicationId;

    @Column(
            name = "round_number",
            nullable = false
    )
    private Integer roundNumber;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_time")
    private LocalTime interviewTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "interview_status",
            nullable = false
    )
    private InterviewStatus interviewStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private InterviewResult result;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        if (interviewStatus == null) {
            interviewStatus = InterviewStatus.PENDING;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}