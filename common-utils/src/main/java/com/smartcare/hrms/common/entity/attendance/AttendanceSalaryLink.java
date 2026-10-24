package com.smartcare.hrms.common.entity.attendance;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "attendance_salary_link",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_emp_month",
                        columnNames = {"employee_id", "month_year"}
                )
        }
)
@Data
@Builder
public class AttendanceSalaryLink extends BaseEntityUUID {

    @Column(name = "employee_id",nullable = false)
    private String employeeId;

    @Column(name = "month_year",nullable = false)
    private String monthYear; // YYYY-MM


    @Enumerated(EnumType.STRING)
    @Column(name = "a_status", nullable = false)
    private AttendanceStatusEnum attendanceStatus;


    @Enumerated(EnumType.STRING)
    @Column(name = "p_status", nullable = false)
    private PayrollStatus payrollStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name="night_shift_exist")
    private Boolean nightShiftExist;

    @Column(name="nsa_amt")
    private Double nightShiftAllowanceAmt;

    @Column(name="nsa_days")
    private Double nightShiftDays;

    @PrePersist
    protected void onCreate() {
        if (attendanceStatus == null) {
            attendanceStatus = AttendanceStatusEnum.PENDING;
        }
        if (payrollStatus == null) {
            payrollStatus = PayrollStatus.PENDING;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
