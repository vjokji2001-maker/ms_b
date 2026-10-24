package com.smartcare.hrms.common.entity.attendance;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "emp_atndnce")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAttendance extends BaseEntityId {

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "emp_shift_id", nullable = false)
    private Long employeeShiftId;

    @Column(name ="atndnce_date",nullable = false)
    private LocalDate attendanceDate;

    @Column(name ="in_time")
    private LocalTime inTime;

    @Column(name ="out_time")
    private LocalTime outTime;

    @Column(name ="day_type")
    private String dayType;

    @Column(name ="day_type1")
    private String dayType1;

    @Column(name ="`portion`")
    private Double portion;

    @Column(name ="late_in_mark")
    private Double lateInMark;

    @Column(name ="early_out_mark")
    private Double earlyOutMark;

    @Column(name="nsa_amt")
    private Double nightShiftAmount;

    @Column(name="nsa_days")
    private Double nightShiftDays;

    @Column(name ="status")
    @Enumerated(EnumType.STRING)
    private AttendanceStatusEnum status;

    @Column(name="leave_rqst_id")
    private String leaveRequestId;
}

