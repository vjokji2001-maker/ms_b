package com.smartcare.hrms.common.entity.attendance.application.swipe;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.PunchType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emp_daily_punch_log")
@Data
@Builder
public class EmployeeDailyPunchLog extends BaseEntityId {

    @Column(name = "emp_code")
    private String empCode;

    @Column(name = "punch_date")
    private LocalDate punchDate;

    @Column(name = "punch_date_time")
    private LocalDateTime punchDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type")
    private PunchType punchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private SwipeDirection swipeDirection;
}
