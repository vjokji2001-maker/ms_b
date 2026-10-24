package com.smartcare.hrms.migration.helper.attendance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class AttendanceDayDistributor {

    private AttendanceDayDistributor() {}

    /**
     * Returns all dates in the given YearMonth.
     */
    public static List<LocalDate> buildDateList(YearMonth ym) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }

    /**
     * Distributes WO / ABS / DP across the given dates.
     * Week-offs are spread evenly; then DP fills remaining slots; then ABS.
     *
     * Day type codes:
     *   WO  = Week Off
     *   DP  = Day Present
     *   ABS = Absent
     */
    public static Map<LocalDate, String> distributeDays(
            List<LocalDate> allDates, int presentDays, int absentDays, int weekOffDays) {

        Map<LocalDate, String> map = new LinkedHashMap<>();

        // Spread week-offs evenly across the month
        Set<Integer> woIndexes = new LinkedHashSet<>();
        if (weekOffDays > 0) {
            double step = (double) allDates.size() / weekOffDays;
            for (int i = 0; i < weekOffDays; i++) {
                int idx = (int) Math.round(i * step + step - 1);
                idx = Math.min(idx, allDates.size() - 1);
                // Avoid collision by moving backwards
                while (woIndexes.contains(idx) && idx > 0) idx--;
                woIndexes.add(idx);
            }
        }

        int dpLeft = presentDays;

        for (int i = 0; i < allDates.size(); i++) {
            LocalDate d = allDates.get(i);
            if (woIndexes.contains(i)) {
                map.put(d, "WO");
            } else if (dpLeft > 0) {
                map.put(d, "DP");
                dpLeft--;
            } else {
                map.put(d, "ABS");
            }
        }
        return map;
    }
}
