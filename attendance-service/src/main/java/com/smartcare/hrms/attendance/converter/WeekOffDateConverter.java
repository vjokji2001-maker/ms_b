package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WeekOffDateConverter{

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<String> convert(
            List<EmployeeWeekOffBean> weekOffRules,
            LocalDate yearStartDate,
            LocalDate yearEndDate) {

        if (weekOffRules == null || weekOffRules.isEmpty()) {
            return Collections.emptyList();
        }

        Set<LocalDate> weekOffDates = new HashSet<>();

        LocalDate currentMonth = yearStartDate.withDayOfMonth(1);

        while (!currentMonth.isAfter(yearEndDate)) {

            for (EmployeeWeekOffBean rule : weekOffRules) {

                int weekIndex = getWeekIndex(rule.getWeekNo());

                java.time.DayOfWeek javaDay =
                        java.time.DayOfWeek.valueOf(rule.getDay());

                Optional<LocalDate> weekOffDate =
                        getNthWeekdayOfMonth(currentMonth, javaDay, weekIndex);

                weekOffDate.ifPresent(date -> {
                    if (!date.isBefore(yearStartDate)
                            && !date.isAfter(yearEndDate)) {
                        weekOffDates.add(date);
                    }
                });
            }

            currentMonth = currentMonth.plusMonths(1);
        }

        return weekOffDates.stream()
                .sorted()
                .map(date -> date.format(FORMATTER))
                .collect(Collectors.toList());
    }

    /* ------------------ Helper Methods ------------------ */

    private int getWeekIndex(String weekNo) {
        return Integer.parseInt(weekNo.replace("WEEK_", ""));
    }

    private Optional<LocalDate> getNthWeekdayOfMonth(
            LocalDate month,
            java.time.DayOfWeek dayOfWeek,
            int nth) {

        LocalDate firstDay = month.withDayOfMonth(1);
        int count = 0;

        for (int i = 0; i < month.lengthOfMonth(); i++) {
            LocalDate date = firstDay.plusDays(i);

            if (date.getDayOfWeek() == dayOfWeek) {
                count++;
                if (count == nth) {
                    return Optional.of(date);
                }
            }
        }
        return Optional.empty();
    }
}
