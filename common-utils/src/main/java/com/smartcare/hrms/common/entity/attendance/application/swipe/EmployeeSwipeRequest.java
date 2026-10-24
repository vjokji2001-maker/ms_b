package com.smartcare.hrms.common.entity.attendance.application.swipe;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import com.smartcare.hrms.common.enumeration.SwipeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emp_swp_rqst")
@Data
@Builder
public class EmployeeSwipeRequest extends BaseEntityUUID {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employee employee;

    @Column(name = "swp_date")
    private LocalDate swipeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private DayType dayType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private SwipeDirection swipeDirection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_shift_id")
    private EmployeeShift employeeShift;

    @Column(name = "req_in_time")
    private LocalTime reqInTime;

    @Column(name = "req_out_time")
    private LocalTime reqOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SwipeStatus swipeStatus;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_date_time", insertable = false, updatable = false)
    private LocalDateTime createdDateTime;

    @Column(name = "modified_date_time", insertable = false, updatable = false)
    private LocalDateTime modifiedDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @OneToMany(mappedBy = "employeeSwipeRequest", cascade = CascadeType.ALL)
    private List<SwipeRequestApproverDetails> approverDetailsList;
}
