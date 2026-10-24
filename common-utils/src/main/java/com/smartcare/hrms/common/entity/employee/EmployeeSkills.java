package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "emp_skills")
@Builder
public class EmployeeSkills extends BaseEntityId {

    @Serial
    private static final long serialVersionUID = 1L;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employee employee;

    @Column(name = "skill")
    private String skill;

    @Column(name = "institute_university_name")
    private String instituteUniversityName;

    @Column(name = "rating")
    private String rating;

    @Column(name = "from_month_year")
    private LocalDate fromMonthYear;

    @Column(name = "to_month_year")
    private LocalDate toMonthYear;

    @Column(name = "document_name")
    private String documentName;

    @Transient
    private boolean remove;

}
