package com.smartcare.hrms.attendance.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSummaryDTO {

    private String attendanceSalaryLinkId;
    private String empId;
    private String empName;
    private String empCode;
    private String monthYear;
    private BigDecimal totalMonthDays;
    private BigDecimal totalPresentDays;
    private BigDecimal totalNightShifts;
    private BigDecimal totalWeekOffDays;
    private BigDecimal totalPaidLeaves;
    private BigDecimal totalHolidays;
    private BigDecimal totalLateMarks;
    private BigDecimal totalLateAbsents;
    private BigDecimal totalLateLeaves;
    private BigDecimal totalAbsents;
    private BigDecimal totalUnpaidLeaves;
    private String status;
    private BigDecimal totalPayableDays;
    private Double totalNightShiftsAmt;
    private Double totalNightShiftDays;
}
