package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.common.bean.attendance.EmployeeShiftDetailBean;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;


@Component
public class ShiftConverter {
    private static LocalDateTime toDateTimeAdjusted(
            LocalDate date,
            LocalTime time,
            LocalTime inTime,
            LocalTime outTime
    ) {
        if (time == null) return null;

        LocalDateTime shiftStart = LocalDateTime.of(date, inTime);
        LocalDateTime shiftEnd   = LocalDateTime.of(date, outTime);

        // Check if overnight shift
        boolean isOvernight = !outTime.isAfter(inTime);

        if (isOvernight) {
            shiftEnd = shiftEnd.plusDays(1);
        }

        LocalDateTime dt = LocalDateTime.of(date, time);

        // ONLY adjust for overnight shift
        if (isOvernight && dt.isBefore(shiftStart)) {
            dt = dt.plusDays(1);
        }

        return dt;
    }

    public static void convertForCreate(EmployeeShift entity, EmployeeShiftDTO dto, EmployeeShiftDetailBean shiftDetailBean) {

        Employee emp = new Employee();
        emp.setId(UUID.fromString(dto.getEmpId()));
        entity.setEmployee(emp);

        ShiftTemplate shift = new ShiftTemplate();
        shift.setId(dto.getShiftId());
        entity.setShiftTemplate(shift);

        LocalDate date    = LocalDate.parse(dto.getDate());
        LocalTime inTime  = shiftDetailBean.getShiftInTime();
        LocalTime outTime = shiftDetailBean.getShiftOutTime();

        long cutoffHours = shiftDetailBean.getCutoffHr() != null
                ? shiftDetailBean.getCutoffHr().longValue()
                : 0L;

        entity.setDate(date);
        entity.setIsDefault(dto.getIsDefault());

        LocalDateTime shiftStart = LocalDateTime.of(date, inTime).minusHours(cutoffHours);
        entity.setShiftStartTime(shiftStart);
        entity.setShiftEndTime(shiftStart.plusHours(23).plusMinutes(59));
        entity.setEmpInTime(LocalDateTime.of(date, inTime));

        // ✅ Fixed - 4 args
        entity.setEmpOutTime(toDateTimeAdjusted(date, outTime, inTime, outTime));
        entity.setEmpFirstHalfOutTime(toDateTimeAdjusted(date, shiftDetailBean.getFirstHalfOutTime(), inTime, outTime));
        entity.setEmpSecondHalfInTime(toDateTimeAdjusted(date, shiftDetailBean.getSecondHalfInTime(), inTime, outTime));

        entity.setGraceTimeMins(shiftDetailBean.getGraceTimeMins());
        entity.setCutoffHr(shiftDetailBean.getCutoffHr());
        entity.setAutoAdjustTime(shiftDetailBean.getAutoAdjustTime());
    }

    public static void convertForUpdate(EmployeeShift entity, EmployeeShiftDTO dto, EmployeeShiftDetailBean shiftDetailBean) {

        entity.setIsDefault(dto.getIsDefault());

        if (!entity.getShiftTemplate().getId().equals(dto.getShiftId())) {
            ShiftTemplate template = new ShiftTemplate();
            template.setId(dto.getShiftId());
            entity.setShiftTemplate(template);
        }

        LocalDate date    = entity.getDate();
        LocalTime inTime  = shiftDetailBean.getShiftInTime();
        LocalTime outTime = shiftDetailBean.getShiftOutTime();

        long cutoffHours = shiftDetailBean.getCutoffHr() != null
                ? shiftDetailBean.getCutoffHr().longValue()
                : 0L;

        LocalDateTime shiftStart = LocalDateTime.of(date, inTime).minusHours(cutoffHours);
        entity.setShiftStartTime(shiftStart);
        entity.setShiftEndTime(shiftStart.plusHours(23).plusMinutes(59));
        entity.setEmpInTime(LocalDateTime.of(date, inTime));

        // ✅ Fixed - 4 args
        entity.setEmpOutTime(toDateTimeAdjusted(date, outTime, inTime, outTime));
        entity.setEmpFirstHalfOutTime(toDateTimeAdjusted(date, shiftDetailBean.getFirstHalfOutTime(), inTime, outTime));
        entity.setEmpSecondHalfInTime(toDateTimeAdjusted(date, shiftDetailBean.getSecondHalfInTime(), inTime, outTime));

        entity.setGraceTimeMins(shiftDetailBean.getGraceTimeMins());
        entity.setCutoffHr(shiftDetailBean.getCutoffHr());
        entity.setAutoAdjustTime(shiftDetailBean.getAutoAdjustTime());
    }
}