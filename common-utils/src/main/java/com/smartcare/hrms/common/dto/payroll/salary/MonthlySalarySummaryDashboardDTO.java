package com.smartcare.hrms.common.dto.payroll.salary;

import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySalarySummaryDashboardDTO {

    private Boolean nightShiftExist;
    private BigDecimal nsaAmount;
    private String monthPayrollId;
    private EmployeeDetails employeeDetails;
    private DaysDetails daysDetails;
    private StatutoryFlag statutoryFlags;
    private MonthlyGross monthlyGross;
    private CurrentMonthPayable currentMonthPayable;
    private ProcessStatus status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeDetails {
        private String employeeId;
        private String employeeName;
        private String employeeCode;
        private String payrollState;
        private String gender;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DaysDetails {
        private Double totalDaysInMonth;
        private Double payableDays;
        private Double lossOfPayDays;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyGross{
        private BigDecimal totalEarnings;
        private BigDecimal totalStdDeductions;
        private BigDecimal totalOtherDeductions;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentMonthPayable{
        private BigDecimal totalEarnings;
        private BigDecimal totalStdDeductions;
        private BigDecimal totalOtherDeductions;
        private BigDecimal lossOfPayAmount;
        private BigDecimal netPayable;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessStatus {
        private AttendanceStatusEnum attendanceStatus;
        private PayrollStatus payrollStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatutoryFlag {
        private Boolean pfEnabled;
        private Boolean esicEnabled;
        private Boolean ptEnabled;
        private String state;
    }
}
