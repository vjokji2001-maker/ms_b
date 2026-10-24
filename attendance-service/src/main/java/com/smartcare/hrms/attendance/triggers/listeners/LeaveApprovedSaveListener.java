package com.smartcare.hrms.attendance.triggers.listeners;

import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.attendance.triggers.event.LeaveApprovedTrigger;
import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class LeaveApprovedSaveListener {

    private @Autowired EmployeeAttendanceRepository employeeAttendanceRepository;

    private @Autowired EmployeeRepository employeeRepository;

    @EventListener
    public void triggerSaveLeaveApproved(LeaveApprovedTrigger event) {

        LocalDate date = event.getFromDate();

        while (!date.isAfter(event.getToDate())) {

            if (isWeekOffDate(date, event.getEmployeeId())) {
                date = date.plusDays(1);
                continue;
            }

            EmployeeAttendance attendance = employeeAttendanceRepository.findByEmployeeIdAndAttendanceDate(event.getEmployeeId(), date)
                    .orElseGet(EmployeeAttendance::new);



            attendance.setEmployeeId(event.getEmployeeId());
            attendance.setEmployeeShiftId(event.getShiftId());
            attendance.setAttendanceDate(date);
            attendance.setStatus(AttendanceStatusEnum.PENDING);
            attendance.setLeaveRequestId(String.valueOf(event.getLeaveId()));

            // FIRST HALF
            if (date.equals(event.getFromDate())
                    && DayType.FIRST_HALF.equals(event.getFromDateType())) {

                attendance.setDayType(event.getLeaveCode());
                attendance.setDayType1("DP");
                attendance.setPortion(0.5);

            }
            // SECOND HALF
            else if (date.equals(event.getToDate())
                    && DayType.SECOND_HALF.equals(event.getToDateType())) {

                attendance.setDayType("DP");
                attendance.setDayType1(event.getLeaveCode());
                attendance.setPortion(0.5);

            }
            // FULL DAY
            else {
                attendance.setDayType(event.getLeaveCode());
                attendance.setDayType1(null);
                attendance.setPortion(1.0);
            }

            employeeAttendanceRepository.save(attendance);
            date = date.plusDays(1);
        }
    }

    private boolean isWeekOffDate(LocalDate date, String employeeId) {

        List<EmployeeWeekOffBean> weekOffList =
                employeeRepository.getEmployeeWeekDetail(employeeId);

        for (EmployeeWeekOffBean weekOff : weekOffList) {

            DayOfWeek weekDay = DayOfWeek.valueOf(weekOff.getDay());
            int weekIndex = Integer.parseInt(weekOff.getWeekNo().replace("WEEK_", ""));

            if (getNthWeekdayOfMonth(date.withDayOfMonth(1), weekDay, weekIndex)
                    .map(d -> d.isEqual(date))
                    .orElse(false)) {
                return true;
            }
        }
        return false;
    }


    private Optional<LocalDate> getNthWeekdayOfMonth(LocalDate month, DayOfWeek dayOfWeek, int nth) {
        LocalDate firstDayOfMonth = month.withDayOfMonth(1);
        int count = 0;

        for (int i = 0; i < month.lengthOfMonth(); i++) {
            LocalDate current = firstDayOfMonth.plusDays(i);
            if (current.getDayOfWeek() == dayOfWeek) {
                count++;
                if (count == nth) return Optional.of(current);
            }
        }
        return Optional.empty();
    }
}