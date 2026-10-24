package com.smartcare.hrms.common.entity.attendance.shift;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emp_shift")
@Data
@Builder
public class EmployeeShift extends BaseEntityId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false, unique = true)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false, unique = true)
    private ShiftTemplate shiftTemplate;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "shift_strt_time", nullable = false)
    private LocalDateTime shiftStartTime;

    @Column(name = "emp_in_time", nullable = false)
    private LocalDateTime empInTime;

    @Column(name = "emp_fh_out_time")
    private LocalDateTime empFirstHalfOutTime;

    @Column(name = "emp_sh_in_time")
    private LocalDateTime empSecondHalfInTime;

    @Column(name = "emp_out_time", nullable = false)
    private LocalDateTime empOutTime;

    @Column(name = "shift_end_time", nullable = false)
    private LocalDateTime shiftEndTime;

    @Column(name = "cutoff_hr", precision = 5, scale = 2)
    private BigDecimal cutoffHr;

    @Column(name = "grace_time_mins", nullable = false)
    private Integer graceTimeMins;

    @Column(name = "auto_adjust_time")
    private LocalTime autoAdjustTime;

}
