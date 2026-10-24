package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardSummaryDTO {

    private StaffProfileDTO profile;
    private StaffTodayAttendanceDTO todayAttendance;
    private List<StaffLeaveBalanceDTO> leaveBalances;
    private List<StaffHolidayDTO> upcomingHolidays;
    private List<String> weeklyOffDays;
    private String currentShiftName;
    private String currentShiftTimings;
}
