package com.smartcare.hrms.common.utility;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class DateHelper {
    private static final String userViewDatePattern = "dd/MM/yyyy";
    private static final String sqlDatePattern = "yyyy-MM-dd";
    private static final String userViewDateTimePattern = "dd-MM-yyyy HH:mm";
    private static final String sqlDateTimePattern = "yyyy-MM-dd HH:mm:ss";
    private static final String inputDatePattern = "dd-MM-yyyy";
    private static final String displayViewDatePattern = "dd-MM-yyyy";
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("MM-yyyy");

    public static String formatDateStringToUserDateView(String dbDate){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(sqlDatePattern);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(userViewDatePattern);
        LocalDate date = LocalDate.parse(dbDate,inputFormatter);
        return date.format(outputFormatter);
    }

    public static String formatDateObjectToUserDateView(LocalDate dbDate){
        if(dbDate == null){
            return null;
        }
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(displayViewDatePattern);
        return dbDate.format(outputFormatter);
    }

    public static String formatDateObjectToUserViewDate(LocalDate dbDate){
        if(dbDate == null){
            return null;
        }
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(userViewDatePattern);
        return dbDate.format(outputFormatter);
    }

    public static String formatDateTimeStringToUserDateView(String dbDateTime){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(sqlDateTimePattern);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(userViewDateTimePattern);
        LocalDateTime dateTime = LocalDateTime.parse(dbDateTime, inputFormatter);
        return dateTime.format(outputFormatter);
    }

    public static String formatDateTimeObjectToUserDateView(LocalDateTime dbDateTime){
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(userViewDateTimePattern);
        return dbDateTime.format(outputFormatter);
    }

    public static LocalDate convertUserDateToLocalDate(String userViewDate) {
        if (userViewDate == null || userViewDate.isEmpty()) {
            return null;
        }
        DateTimeFormatter userFormatter = DateTimeFormatter.ofPattern(inputDatePattern);
        return LocalDate.parse(userViewDate, userFormatter);
    }


    public static LocalDate toLocalDate(String yearMonthStr) {
        if (yearMonthStr == null || yearMonthStr.isBlank()) return null;
        return YearMonth.parse(yearMonthStr, YM).atDay(1);
    }

    public static LocalDate toLocalDateFromDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return toLocalDateTime(value).toLocalDate();
    }

    public static String toYearMonthString(LocalDate date) {
        if (date == null) return null;
        return date.format(YM);
    }

    public static Long calculateTotalSpan(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }

    public static String formatTotalSpan(Integer totalMonths) {
        if (totalMonths == null || totalMonths == 0) {
            return "0 months";
        }

        int years = totalMonths / 12;
        int months = totalMonths % 12;

        if (years == 0) return months + " months";
        if (months == 0) return years + " year" + (years > 1 ? "s" : "");
        return years + " year" + (years > 1 ? "s" : "") + " " + months + " months";
    }



    public static Map<String ,LocalDate> getMonthStartEndDate(Integer monthStartDate, Integer monthEndDate, LocalDate fromDate){
        monthStartDate = getLastDateOfMonth(monthStartDate, fromDate.getMonthValue(), fromDate.getYear());
        monthEndDate  = getLastDateOfMonth(monthEndDate, fromDate.getMonthValue(), fromDate.getYear());
        LocalDate monthCycleStartDate = null;
        LocalDate monthCycleEndDate = null;
        if(monthStartDate==1){
            monthCycleStartDate=fromDate.withDayOfMonth(1);
            monthCycleEndDate = fromDate.withDayOfMonth(monthEndDate);
        }else{
            monthCycleStartDate = fromDate.withDayOfMonth(monthStartDate);
            if(monthCycleStartDate.isAfter(fromDate)){
                monthCycleStartDate = monthCycleStartDate.minusMonths(1);
                monthCycleEndDate = monthCycleStartDate.plusMonths(1).withDayOfMonth(monthEndDate);
            }else{
                monthCycleEndDate = monthCycleStartDate.plusMonths(1).withDayOfMonth(monthEndDate);
            }
        }
        Map<String,LocalDate> map = new HashMap<>();
        map.put("monthStartDate",monthCycleStartDate);
        map.put("monthEndDate",monthCycleEndDate);
        return map;
    }


    public static Map<String ,LocalDate> getQuarterStartEndDate(Integer cycleStartDate,Integer salaryEndDate,LocalDate yearMonth,LocalDate applicationFromDate){
        Map<String,LocalDate> map = new HashMap<>();
        int newCycleStartDate = getLastDateOfMonth(cycleStartDate, yearMonth.getMonthValue(), yearMonth.getYear());
        int newSalaryEndDate  = getLastDateOfMonth(salaryEndDate, yearMonth.getMonthValue(), yearMonth.getYear());
        LocalDate yearStartDate = yearMonth.withDayOfMonth(newCycleStartDate);
        int quarterEndMonth = 3;
        while (quarterEndMonth<=12){
            LocalDate quarterEndDate = yearStartDate.plusMonths(3).minusDays(1);
            newSalaryEndDate = DateHelper.getLastDateOfMonth(salaryEndDate,quarterEndDate.getMonthValue(),quarterEndDate.getYear());
            quarterEndDate=quarterEndDate.withDayOfMonth(newSalaryEndDate);
            if(!applicationFromDate.isBefore(yearStartDate) && !applicationFromDate.isAfter(quarterEndDate)){
                map.put("qStartDate",yearStartDate);
                map.put("qEndDate",quarterEndDate);
                break;
            }
            quarterEndMonth=quarterEndMonth+3;
            yearStartDate=quarterEndDate.plusDays(1);
        }
        return map;
    }
    public static Map<String ,LocalDate> getYearStartEndDate(LocalDate yearStartDate,Integer monthStartDate){
        yearStartDate = yearStartDate.withDayOfMonth(monthStartDate);
        LocalDate yearEndDate = yearStartDate.plusYears(1).minusDays(1);
        Map<String,LocalDate> map= new HashMap<>();
        map.put("yearStartDate",yearStartDate);
        map.put("yearEndDate",yearEndDate);
        return map;
    }

    public static Integer getLastDateOfMonth(int day, int month, int year){
        if(Month.of(month).equals(Month.FEBRUARY)){
            if(year%4==0){
                day= Math.min(day, 29);
            }else{
                day= Math.min(day, 28);
            }
        }else if(Month.of(month).equals(Month.APRIL)){
            day= day==31?30:day;
        }else if(Month.of(month).equals(Month.JUNE)){
            day= day==31?30:day;
        }else if(Month.of(month).equals(Month.SEPTEMBER)){
            day= day==31?30:day;
        }else if(Month.of(month).equals(Month.NOVEMBER)){
            day= day==31?30:day;
        }
        return day;
    }
    public static LocalDate toLocalDate(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }

        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }

        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value)
                    .toLocalDateTime()
                    .toLocalDate();
        }

        if (value instanceof String) {
            return LocalDate.parse((String) value);
        }

        throw new IllegalArgumentException(
                "Unsupported date type: " + value.getClass());
    }
    public static LocalDateTime toLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;

        // Try with seconds first: yyyy-MM-ddTHH:mm:ss
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            // Fall back to without seconds: yyyy-MM-ddTHH:mm
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } catch (DateTimeParseException e2) {
                // Try with space separator: yyyy-MM-dd HH:mm:ss
                try {
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }

    public static LocalTime toLocalTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        // Handle "HH:mm:ss" and "HH:mm" both
        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            try {
                return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to parse time: " + value);
            }
        }
    }

    public static String formatLocalTime(LocalTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static LocalDateTime toDateTimeOnDate(String timeStr, LocalDate date) {
        if (timeStr == null || timeStr.isBlank() || date == null) return null;
        LocalTime time = DateHelper.toLocalTime(timeStr);
        return time != null ? LocalDateTime.of(date, time) : null;
    }
}
