package com.smartcare.hrms.hiring.model.entity;

import lombok.Data;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.smartcare.hrms.hiring.model.entity.enums.Priority;
import com.smartcare.hrms.hiring.model.entity.enums.RequestStatus;
import com.smartcare.hrms.hiring.model.entity.enums.EmploymentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hiring_request")
public class HiringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true)
    private String requestId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "designation_id", nullable = false)
    private Long designationId;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "state_id", nullable = false)
    private Long stateId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(nullable = false)
    private Integer vacancies;

    @Column(name = "employment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Column(nullable = false)
    private String experience;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "reporting_manager", nullable = false)
    private String reportingManager;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "hired_count", nullable = false)
    @Builder.Default
    private Integer hiredCount = 0;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    private void beforeInsert() {

        if (createdDate == null) {createdDate = LocalDateTime.now();}
        if (hiredCount == null) {hiredCount = 0;}
        if (status == null) {status = RequestStatus.PENDING;}
    }
}