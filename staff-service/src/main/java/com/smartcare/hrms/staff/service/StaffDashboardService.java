package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.staff.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StaffDashboardService {

    @Autowired
    private StaffProfileService profileService;

    @Autowired
    private StaffAttendanceService attendanceService;

    @Autowired
    private StaffLeaveService leaveService;

    @Autowired
    private StaffShiftHolidayService shiftHolidayService;

    /**
     * Complete Home Screen overview API for mobile app / staff portal.
     */
    public StaffDashboardSummaryDTO getDashboardOverview(String empId) {
        StaffProfileDTO profile = profileService.getStaffProfile(empId);
        StaffTodayAttendanceDTO todayAttendance = attendanceService.getTodayAttendance(empId);
        List<StaffLeaveBalanceDTO> leaveBalances = leaveService.getLeaveBalances(empId);
        List<StaffHolidayDTO> holidays = shiftHolidayService.getAssignedHolidays(empId);
        List<String> weeklyOffDays = shiftHolidayService.getWeeklyOffDays(empId);

        // Filter upcoming holidays (first 3)
        List<StaffHolidayDTO> upcomingHolidays = holidays.stream()
                .limit(3)
                .toList();

        return StaffDashboardSummaryDTO.builder()
                .profile(profile)
                .todayAttendance(todayAttendance)
                .leaveBalances(leaveBalances)
                .upcomingHolidays(upcomingHolidays)
                .weeklyOffDays(weeklyOffDays)
                .currentShiftName(todayAttendance.getShiftName())
                .currentShiftTimings(todayAttendance.getShiftInTime() + " - " + todayAttendance.getShiftOutTime())
                .build();
    }
}
