package com.smartcare.hrms.common.entity.employee;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="emp_leave_dtls")
public class EmployeeLeaveDtls extends BaseEntityId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="enp_leave_id")
    private EmployeeLeaveParent  employeeLeave;

    @Column(name="leave_name_id")
    private Long  leaveNameId;

    @Column(name="yrly_count")
    private Double yearlyCount;

    @Column(name="qtrly_count")
    private Double quaterlyCount;

    @Column(name="mnthly_count")
    private Double  monthlyCount;

    @Column(name="pst_days")
    private Double pastDays;

    @Column(name="future_days")
    private Double futureDays;

    @Column(name="half_day_allowed")
    private boolean halfDayAllowed;

    @Transient
    private boolean remove;
}
