package com.smartcare.hrms.common.entity.attendance;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "emp_attendance_summary")
public class EmployeeAttendanceSummary extends BaseEntityUUID {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "attendance_salary_link_id",
            nullable = false
    )
    private AttendanceSalaryLink attendanceSalaryLink;

    @Column(name = "ttl_mnth_days", nullable = false)
    private BigDecimal totalMonthDays;

    @Column(name = "ttl_present_days", nullable = false)
    private BigDecimal totalPresentDays;

    @Column(name = "ttl_week_off_days", nullable = false)
    private BigDecimal totalWeekOffDays;

    @Column(name = "ttl_paid_lvs", nullable = false)
    private BigDecimal totalPaidLeaves;

    @Column(name = "ttl_holidays", nullable = false)
    private BigDecimal totalHolidays;

    @Column(name = "ttl_late_mark", nullable = false)
    private BigDecimal totalLateMarks;

    @Column(name = "ttl_late_abs", nullable = false)
    private BigDecimal totalLateAbsents;

    @Column(name = "ttl_late_lvs", nullable = false)
    private BigDecimal totalLateLeaves;

    @Column(name = "ttl_abs", nullable = false)
    private BigDecimal totalAbsents;

    @Column(name = "ttl_unpaid_lvs", nullable = false)
    private BigDecimal totalUnpaidLeaves;
}
