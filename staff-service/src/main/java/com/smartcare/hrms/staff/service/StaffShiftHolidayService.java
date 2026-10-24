package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeConfig;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayChild;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffHolidayDTO;
import com.smartcare.hrms.staff.dto.StaffShiftRosterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaffShiftHolidayService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeConfigRepository employeeConfigRepository;

    @Autowired
    private EmployeeShiftRepository shiftRepository;

    @Autowired
    private MessageResource messageResource;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Get all assigned holidays for this employee.
     */
    public List<StaffHolidayDTO> getAssignedHolidays(String empId) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        Optional<EmployeeConfig> configOpt = employeeConfigRepository.findByEmployee_Id(employee.getId());
        if (configOpt.isEmpty() || configOpt.get().getHolidayParent() == null) {
            return Collections.emptyList();
        }

        HolidayParent hp = configOpt.get().getHolidayParent();
        List<HolidayChild> children = hp.getHolidayChild();
        if (CollectionUtils.isEmpty(children)) {
            return Collections.emptyList();
        }

        return children.stream()
                .filter(hc -> hc.getDate() != null)
                .sorted(Comparator.comparing(HolidayChild::getDate))
                .map(hc -> StaffHolidayDTO.builder()
                        .id(hc.getId())
                        .name(hc.getName())
                        .date(hc.getDate().format(DATE_FORMATTER))
                        .dayOfWeek(hc.getDate().getDayOfWeek().name())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get weekly off days (e.g. ["SATURDAY", "SUNDAY"]).
     */
    public List<String> getWeeklyOffDays(String empId) {
        List<EmployeeWeekOffBean> weekDetails = employeeRepository.getEmployeeWeekDetail(empId);
        if (CollectionUtils.isEmpty(weekDetails)) {
            return List.of("SUNDAY");
        }
        return weekDetails.stream()
                .map(EmployeeWeekOffBean::getDay)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get monthly shift roster with holiday and weekoff markers for calendar.
     */
    public List<StaffShiftRosterDTO> getMonthlyRoster(String empId, Integer year, Integer month) {
        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        LocalDate now = LocalDate.now();
        int targetYear = (year != null && year > 0) ? year : now.getYear();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 1. Fetch employee shifts for this month
        List<EmployeeShift> shifts = shiftRepository.findShiftsByEmpIdsAndDateRange(
                List.of(employee.getId()), startDate, endDate);
        Map<LocalDate, EmployeeShift> shiftByDate = shifts.stream()
                .collect(Collectors.toMap(EmployeeShift::getDate, s -> s, (s1, s2) -> s1));

        // 2. Fetch holidays
        List<StaffHolidayDTO> holidays = getAssignedHolidays(empId);
        Map<String, String> holidayMap = holidays.stream()
                .collect(Collectors.toMap(StaffHolidayDTO::getDate, StaffHolidayDTO::getName, (h1, h2) -> h1));

        // 3. Fetch week-offs
        Set<String> weekOffDays = new HashSet<>(getWeeklyOffDays(empId));

        List<StaffShiftRosterDTO> roster = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            String dateStr = cur.format(DATE_FORMATTER);
            String dayName = cur.getDayOfWeek().name();
            boolean isWeekOff = weekOffDays.contains(dayName) || weekOffDays.contains(dayName.substring(0, 3));
            String holidayName = holidayMap.get(dateStr);
            boolean isHoliday = (holidayName != null);

            EmployeeShift es = shiftByDate.get(cur);
            ShiftTemplate st = (es != null) ? es.getShiftTemplate() : null;

            String shiftName = (st != null) ? st.getName() : "General Shift";
            String shiftCode = (st != null) ? st.getCode() : "GEN";
            String inTime = (st != null && st.getInTime() != null) ? st.getInTime().format(TIME_FORMATTER) : "09:30 AM";
            String outTime = (st != null && st.getOutTime() != null) ? st.getOutTime().format(TIME_FORMATTER) : "06:30 PM";

            roster.add(StaffShiftRosterDTO.builder()
                    .date(dateStr)
                    .dayOfWeek(dayName)
                    .shiftName(shiftName)
                    .shiftCode(shiftCode)
                    .inTime(inTime)
                    .outTime(outTime)
                    .isWeekOff(isWeekOff)
                    .isHoliday(isHoliday)
                    .holidayName(holidayName)
                    .isDefault(es != null ? es.getIsDefault() : true)
                    .build());

            cur = cur.plusDays(1);
        }

        return roster;
    }
}
