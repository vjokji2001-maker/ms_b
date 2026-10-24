package com.smartcare.hrms.common.utility;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
//    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String convertToString(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return localTime.format(TIME_FORMATTER);
    }

//    public static String convertToString(LocalDateTime localDateTime) {
//        if (localDateTime == null) {
//            return null;
//        }
//        return localDateTime.format(TIME_FORMATTER);
//    }
    public static LocalTime convertToLocalTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeString, TIME_FORMATTER);
    }

    public static LocalTime convertSqlTimeToLocalTime(Object timeObj) {
        if (timeObj == null) return null;
        if (timeObj instanceof java.sql.Time) {
            return ((java.sql.Time) timeObj).toLocalTime();
        }
        return LocalTime.parse(timeObj.toString()); // fallback
    }

}
