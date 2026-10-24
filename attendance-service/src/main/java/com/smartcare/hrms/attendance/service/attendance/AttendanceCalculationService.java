package com.smartcare.hrms.attendance.service.attendance;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.EmployeeAttendanceConverter;
import com.smartcare.hrms.attendance.dto.attendance.EmpAttendanceDTO;
import com.smartcare.hrms.attendance.dto.attendance.ShiftRuleDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.common.repository.attendance.AttendanceSalaryLinkRepository;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.common.bean.attendance.EmployeeDailyPunchLogBean;
import com.smartcare.hrms.common.bean.attendance.ShiftDetailsBean;
import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeDailyPunchLogRepository;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.shift.LateMarkRuleRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttendanceCalculationService {

        @Autowired
        private  EmployeeShiftRepository employeeShiftRepository;
        @Autowired
        private  EmployeeRepository employeeRepository;
        @Autowired
        private  EmployeeDailyPunchLogRepository punchLogRepository;
        @Autowired
        private  LateMarkRuleRepository lateMarkRuleRepository;
        @Autowired
        private  EmployeeAttendanceRepository attendanceRepository;
        @Autowired
        private LeaveRequestRepository leaveRequestRepository;
        @Autowired
        private MessageResource messageResource;
        @Autowired
        private  EmployeeAttendanceConverter employeeAttendanceConverter;

        @Autowired
        private AttendanceSalaryLinkRepository attendanceSalaryLinkRepository;

    @Transactional
    public Map<String, List<EmpAttendanceDTO>> calculateMonthlyAttendanceForAll(
            List<String> empIds, Integer year, Integer month) {

        Map<String, List<EmpAttendanceDTO>> result = new LinkedHashMap<>();
        String monthYear = YearMonth.of(year, month).toString();
        List<String> successEmpIds = new ArrayList<>();
        for (String empId : empIds) {
            try {
                List<EmpAttendanceDTO> attendance = calculateMonthlyAttendance(empId, year, month);
                result.put(empId, attendance);
                successEmpIds.add(empId);
            } catch (Exception e) {
                log.error("Error calculating attendance for empId: {}, error: {}", empId, e.getMessage());
                result.put(empId, Collections.emptyList());
            }
        }
        if (!successEmpIds.isEmpty()) {
            attendanceSalaryLinkRepository.markAsRecalculate(successEmpIds, monthYear);
            log.info("Marked RECALCULATE for empIds: {}", successEmpIds);
        }
        return result;
    }

        @Transactional
    public List<EmpAttendanceDTO> calculateMonthlyAttendance(String empId, Integer year, Integer month) {

        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = YearMonth.of(year, month).atEndOfMonth();

        List<ShiftDetailsBean> shiftDetails = employeeShiftRepository.getShiftDetails(empId, fromDate, toDate);

        if (shiftDetails.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime minShiftInTime = shiftDetails.stream()
                .map(s -> s.getShiftStartTime())
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime maxShiftOutTime = shiftDetails.stream()
                .map(s -> s.getShiftEndTime())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (minShiftInTime == null || maxShiftOutTime == null) {
            return Collections.emptyList();
        }

        List<EmployeeDailyPunchLogBean> punchLogData = punchLogRepository.getPunchLogData(
                empId, minShiftInTime, maxShiftOutTime.plusDays(1)
        );

        List<LocalDate> dateList = fromDate.datesUntil(toDate.plusDays(1)).toList();

        Map<String, List<ShiftDetailsBean>> shiftMap = shiftDetails.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getEmployeeId() + "_" + s.getShiftDate()
                ));

        List<EmployeeDailyPunchLogBean> allEmployeePunches = punchLogData.stream()
                .filter(p -> p.getEmpId().toString().equals(empId))
                .toList();

            Map<LocalDate, Boolean> lockedDates = attendanceRepository
                    .findLockedDatesByEmployeeAndMonth(empId, fromDate, toDate)
                    .stream()
                    .map(LocalDate::parse)
                    .collect(Collectors.toMap(date -> date, date -> Boolean.TRUE));

        List<LocalDate> holidayDates = attendanceRepository
                .getHolidayDatesForMonth(empId, fromDate, toDate)
                .stream()
                .map(LocalDate::parse)
                .toList();

        List<EmployeeWeekOffBean> weekOffDetails = employeeRepository.getEmployeeWeekDetail(empId);
        Map<LocalDate, ResolvedLeaveBean> leaveMap = buildLeaveMap(
                leaveRequestRepository.getLeaveDetailsForMonth(empId, fromDate, toDate)
        );
        List<EmpAttendanceDTO> allAttendance = new ArrayList<>();
        Set<Long> usedPunchIds = new HashSet<>();

        for (LocalDate date : dateList) {

      //  Skip locked dates
            if(lockedDates.containsKey(date)){
                continue;
            }

            String key = empId + "_" + date;
            List<ShiftDetailsBean> empShifts = shiftMap.get(key);

            if (empShifts == null || empShifts.isEmpty()) {
                continue;
            }

            ShiftDetailsBean empShift = empShifts.get(0);

            LocalDateTime shiftStart = empShift.getShiftStartTime();
            LocalDateTime shiftEnd = empShift.getShiftEndTime();

            if (shiftStart == null || shiftEnd == null) continue;

            // If shift end is before or equal to shift start  overnight shift add 1 day
            if (!shiftEnd.isAfter(shiftStart)) {
                shiftEnd = shiftEnd.plusDays(1);
            }

            final LocalDateTime finalShiftStart = shiftStart;
            final LocalDateTime finalShiftEnd = shiftEnd;

            LocalTime shiftInTime  = empShift.getShiftInTime();
            LocalTime shiftOutTime = empShift.getShiftOutTime();

            List<ShiftRuleDTO> shiftLateRules = buildLateRule(empShifts);

            // Strict shift window  no buffer beyond shift end
            List<EmployeeDailyPunchLogBean> empDailyPunches = allEmployeePunches.stream()
                    .filter(p -> {
                        LocalDateTime punchTime = p.getPunchDateTime();
                        return punchTime != null
                                && !usedPunchIds.contains(p.getId())
                                && !punchTime.isBefore(finalShiftStart)
                                && !punchTime.isAfter(finalShiftEnd);
                    })
                    .sorted(Comparator.comparing(EmployeeDailyPunchLogBean::getPunchDateTime))
                    .toList();



            // First IN punch
            LocalTime firstIn = empDailyPunches.stream()
                    .filter(p -> "IN".equals(p.getDirection()))
                    .findFirst()
                    .map(p -> p.getPunchDateTime().toLocalTime())
                    .orElse(null);

            // Last OUT punch
            LocalTime lastOut = empDailyPunches.stream()
                    .filter(p -> "OUT".equals(p.getDirection()))
                    .reduce((first, second) -> second)
                    .map(p -> p.getPunchDateTime().toLocalTime())
                    .orElse(null);

            LocalTime inTime;
            LocalTime outTime;

            if (firstIn == null && lastOut == null) {
                // No punches at all  ABS
                inTime = null;
                outTime = null;
            } else if (firstIn != null && lastOut == null) {
                // Only IN present store inTime, outTime null  will result in ABS
                inTime = firstIn;
                outTime = null;
            } else if (firstIn == null && lastOut != null) {
                // Only OUT present  ABS, don't store anything
                inTime = null;
                outTime = lastOut;
            } else {
                // Both IN and OUT present  valid attendance
                inTime = firstIn;
                outTime = lastOut;
            }


            EmployeeShiftDTO employeeShiftDTO = EmployeeShiftDTO.builder()
                    .id(empShift.getShiftId())
                    .empId(empId)
                    .shiftInTime(shiftInTime.toString())
                    .shiftOutTime(shiftOutTime.toString())
                    .graceTimeMins(empShift.getGraceTimeMin())
                    .firstHalfOutTime(empShift.getFirstHalfOutTime() != null
                            ? empShift.getFirstHalfOutTime().toString() : null)
                    .secondHalfInTime(empShift.getSecondHalfInTime() != null
                            ? empShift.getSecondHalfInTime().toString() : null)
                    .halfDayAllowed(empShift.getHalfDayAllowed())
                    .endBefore(empShift.getEndBefore())
                    .lateRulesSlab(shiftLateRules)
                    .empShiftId(empShift.getEmpShiftId())
                    .halfRate(empShift.getHalfRate())
                    .fullRate(empShift.getFullRate())
                    .nightShiftExist(empShift.getNightShiftExist())
                    .build();

            EmpAttendanceDTO empAttendanceDTO = EmpAttendanceDTO.builder()
                    .employeeId(empId)
                    .attendanceDate(date)
                    .inTime(inTime)
                    .outTime(outTime)
                    .build();

            EmpAttendanceDTO dayAttendance = calculateDayWiseAttendance(empAttendanceDTO, employeeShiftDTO);

            if (dayAttendance != null) {
                EmpAttendanceDTO result = applyOverrides(dayAttendance, holidayDates, weekOffDetails, leaveMap);
                allAttendance.add(result);
            }
        }




        for (EmpAttendanceDTO dto : allAttendance) {
            attendanceRepository.upsertAttendance(
                    dto.getEmployeeId(),
                    dto.getEmpShiftId(),
                    dto.getAttendanceDate(),
                    dto.getInTime(),
                    dto.getOutTime(),
                    dto.getDayType(),
                    dto.getDayType1(),
                    dto.getPortion(),
                    dto.getLateInMark(),
                    dto.getEarlyOutMark(),
                    dto.getNightShiftAmount(),
                    dto.getNightShiftDays(),
                    dto.getStatus().name()
            );
        }

        return allAttendance;
    }

    public EmpAttendanceDTO calculateDayWiseAttendance(
            EmpAttendanceDTO empAttendanceDTO,
            EmployeeShiftDTO employeeShiftDTO) {

        LocalDate attendanceDate= empAttendanceDTO.getAttendanceDate();

        LocalTime shiftInTime  = LocalTime.parse(employeeShiftDTO.getShiftInTime());
        LocalTime shiftOutTime = LocalTime.parse(employeeShiftDTO.getShiftOutTime());

        LocalDateTime shiftStart = LocalDateTime.of(attendanceDate, shiftInTime);
        LocalDateTime shiftEnd   = LocalDateTime.of(attendanceDate, shiftOutTime);

        if(shiftStart==null ||shiftEnd==null) return null;

        if (!shiftEnd.isAfter(shiftStart)) {
            shiftEnd = shiftEnd.plusDays(1);
        }

        LocalDate shiftStartDate = shiftStart.toLocalDate();
        LocalDate shiftEndDate = shiftEnd.toLocalDate(); // already overnight-corrected above

        LocalDateTime firstHalfOutTime = DateHelper.toDateTimeOnDate(
                employeeShiftDTO.getFirstHalfOutTime(), shiftStartDate);

        LocalDateTime secondHalfInTime = DateHelper.toDateTimeOnDate(
                employeeShiftDTO.getSecondHalfInTime(), shiftStartDate);

// If half-times fall before shiftStart they belong to next day
        if (firstHalfOutTime != null && firstHalfOutTime.isBefore(shiftStart)) {
            firstHalfOutTime = firstHalfOutTime.plusDays(1);
        }
        if (secondHalfInTime != null && secondHalfInTime.isBefore(shiftStart)) {
            secondHalfInTime = secondHalfInTime.plusDays(1);
        }

        LocalTime inTimeLocal  = empAttendanceDTO.getInTime();
        LocalTime outTimeLocal = empAttendanceDTO.getOutTime();

        LocalDateTime inTime=inTimeLocal!=null?LocalDateTime.of(attendanceDate,inTimeLocal):null;
        LocalDateTime outTime=outTimeLocal!=null?LocalDateTime.of(attendanceDate,outTimeLocal):null;


        // overnight shift  if outTime is earlier in the clock than inTime it belongs to the next calendar day
        if (inTime != null && outTime != null && outTime.isBefore(inTime)) {
            outTime = outTime.plusDays(1);
        }
        if (inTime != null && inTime.isBefore(shiftStart))
            inTime = shiftStart;

       // clamp late punch to shiftEnd (no early out mark)
        if (outTime != null && outTime.isAfter(shiftEnd))
            outTime = shiftEnd;


        boolean isHalfDayAllowed = Boolean.TRUE.equals(employeeShiftDTO.getHalfDayAllowed());
        int graceTimeMinutes     = employeeShiftDTO.getGraceTimeMins() != null
                ? employeeShiftDTO.getGraceTimeMins() : 0;


        LocalDateTime allowedInTime  = shiftStart.plusMinutes(graceTimeMinutes);
        LocalDateTime allowedOutTime = shiftEnd;

        long shiftDurationMinutes = Duration.between(shiftStart, shiftEnd).toMinutes();

        // half-shift midpoint (used when halfDay not configured)
        LocalDateTime halfShiftTime = shiftStart.plusMinutes(shiftDurationMinutes / 2);

        List<ShiftRuleDTO> slabs= employeeShiftDTO.getLateRulesSlab();

        boolean attendFirstHalf;
        boolean attendSecondHalf;

        if (isHalfDayAllowed && firstHalfOutTime != null && secondHalfInTime != null) {
            // inTime < firstHalfOutDT   strict isBefore
            attendFirstHalf  = inTime != null && inTime.isBefore(firstHalfOutTime);
            // outTime >= secondHalfInDT   isAfter OR isEqual
            attendSecondHalf = outTime != null && outTime.isAfter(secondHalfInTime);
        } else {
            // shift midpoint
            attendFirstHalf  = inTime != null && (inTime.isBefore(halfShiftTime) ||  inTime.isEqual(halfShiftTime));

            attendSecondHalf = outTime != null && (outTime.isAfter(halfShiftTime) ||  outTime.isEqual(halfShiftTime));
        }

        String dayType;
        String dayType1;
        double portion;


        if (inTime == null && outTime == null) {
            dayType  = "ABS";
            dayType1 =null;
            portion  = 1.0;

        }
        else if (inTime != null && outTime == null) {
            // Only IN punch present  store inTime but force ABS
            dayType = "ABS";
            dayType1 = null;
            portion = 1.0;

        } else if (inTime == null && outTime != null) {
            // Only OUT punch ABS
            dayType = "ABS";
            dayType1 = null;
            portion = 1.0;
        }
        else if (!isHalfDayAllowed) {
            dayType  = "DP";
            dayType1 = null;
            portion  = 1.0;

        }
        else if (attendFirstHalf && attendSecondHalf) {
            dayType  = "DP";
            dayType1 = null;
            portion  = 1.0;

        } else if (attendFirstHalf && !attendSecondHalf) {
            dayType  = "DP";
            dayType1 = "ABS";
            portion  = 0.5;

        } else if (!attendFirstHalf && attendSecondHalf) {
            dayType  = "ABS";
            dayType1 = "DP";
            portion  = 0.5;

        } else {
            dayType  = "ABS";
            dayType1 = null;
            portion  = 1.0;
        }


        double lateInMark=0.0;
        double earlyOutMark=0.0;

        if (inTime == null && outTime == null) {
            // No punch → no marks
            lateInMark   = 0.0;
            earlyOutMark = 0.0;

        }  else if (!isHalfDayAllowed) {
            //  Half day NOT allowed → calculate marks normally
            // Sentinel will handle ABS conversion if limit exceeded
            lateInMark   = calculateLateInMark(inTime, allowedInTime, slabs);
            earlyOutMark = calculateEarlyOutMark(outTime, allowedOutTime, slabs);

        } else if (attendFirstHalf && attendSecondHalf) {
            // Both halves attended
            lateInMark   = calculateLateInMark(inTime, allowedInTime, slabs);
            earlyOutMark = calculateEarlyOutMark(outTime, allowedOutTime, slabs);

        } else if (attendFirstHalf) {
            //daytype="DP" , daytype1="ABS" , Portion=0.5
            // Early-out boundary is firstHalfOutTime (or midpoint)
            LocalDateTime earlyBoundary=isHalfDayAllowed?firstHalfOutTime:halfShiftTime;

            lateInMark  = calculateLateInMark(inTime, allowedInTime, slabs);

            earlyOutMark= calculateEarlyOutMark(outTime, earlyBoundary ,slabs);

        } else if ( attendSecondHalf) {
            //daytype="ABS" , daytype1="DP" , Portion=0.5
           LocalDateTime lateBoundary= isHalfDayAllowed?secondHalfInTime:halfShiftTime;

           lateInMark= calculateLateInMark(inTime,lateBoundary,slabs);

           earlyOutMark=calculateEarlyOutMark(outTime,allowedOutTime,slabs);

        } else {

            lateInMark   =calculateLateInMark(inTime, allowedInTime, slabs);
            earlyOutMark =calculateEarlyOutMark(outTime,allowedOutTime,slabs);
        }
        if (lateInMark == -2.0) {
            lateInMark = 0.0;
            dayType    = "ABS";
            dayType1   = null;
            portion    = 1.0;
        }

        // -1.0 lateIn  exceeded slab limit
        if (lateInMark == -1.0) {
            lateInMark = 0.0;
            if (isHalfDayAllowed && firstHalfOutTime != null && secondHalfInTime != null) {
                if ("DP".equals(dayType) && dayType1 == null) {
                    // Full day present  first half ABS, second half DP
                    dayType  = "ABS";
                    dayType1 = "DP";
                    portion  = 0.5;
                } else {
                    // Already partial  full ABS
                    dayType  = "ABS";
                    dayType1 = null;
                    portion  = 1.0;
                }
            } else {
                // Half day not allowed OR not configured  Full ABS
                dayType  = "ABS";
                dayType1 = null;
                portion  = 1.0;
            }
        }

        // -1.0 earlyOut exceeded slab limit
        if (earlyOutMark == -1.0) {
            earlyOutMark = 0.0;
            if (isHalfDayAllowed && firstHalfOutTime != null && secondHalfInTime != null) {
                if ("DP".equals(dayType) && dayType1 == null) {
                    // Full day present first half DP, second half ABS
                    dayType  = "DP";
                    dayType1 = "ABS";
                    portion  = 0.5;
                } else {
                    // Already partial full ABS
                    dayType  = "ABS";
                    dayType1 = null;
                    portion  = 1.0;
                }
            } else {
                // Half day not allowed OR not configured  Full ABS
                dayType  = "ABS";
                dayType1 = null;
                portion  = 1.0;
            }
        }

        Double nightShiftAmount=0.0;
        Double nightShiftDays = 0.0;
        if(Boolean.TRUE.equals(employeeShiftDTO.getNightShiftExist())){
            Double halfRate=employeeShiftDTO.getHalfRate();
            Double fullRate=employeeShiftDTO.getFullRate();

            if ("ABS".equals(dayType) && dayType1 == null) {
                // Full day absent → no night shift amount
                nightShiftAmount = 0.0;
                nightShiftDays   = 0.0;

            } else if ("DP".equals(dayType) && dayType1 == null) {
                // Full day present → full rate
                nightShiftAmount = fullRate != null ? fullRate : 0.0;
                nightShiftDays   = 1.0;

            } else if ("DP".equals(dayType) && "ABS".equals(dayType1)) {
                // First half present, second half absent → half rate
                nightShiftAmount = halfRate != null ? halfRate : 0.0;
                nightShiftDays   = 0.5;

            } else if ("ABS".equals(dayType) && "DP".equals(dayType1)) {
                // First half absent, second half present → half rate
                nightShiftAmount = halfRate != null ? halfRate : 0.0;
                nightShiftDays   = 0.5;

            } else {
                // WOP, PHP, OD, ODP etc → full rate
                nightShiftAmount = fullRate != null ? fullRate : 0.0;
                nightShiftDays   =1.0;
            }
        }

        return EmpAttendanceDTO.builder()
                .empShiftId(employeeShiftDTO.getEmpShiftId())
                .employeeId(empAttendanceDTO.getEmployeeId())
                .attendanceDate(empAttendanceDTO.getAttendanceDate())
                .inTime(inTimeLocal)
                .outTime(outTimeLocal)
                .lateInMark(lateInMark)
                .earlyOutMark(earlyOutMark)
                .dayType(dayType)
                .dayType1(dayType1)
                .portion(portion)
                .nightShiftAmount(nightShiftAmount)
                .nightShiftDays(nightShiftDays)
                .status(AttendanceStatusEnum.PROCESED)
                .build();
    }

    private EmpAttendanceDTO applyOverrides(
            EmpAttendanceDTO dayAttendance,
            List<LocalDate> holidayDates,
            List<EmployeeWeekOffBean> weekOffDetails,
            Map<LocalDate, ResolvedLeaveBean> leaveMap) {

        LocalDate date = dayAttendance.getAttendanceDate();

        boolean isWeekOff = isWeekOff(weekOffDetails, date);
        boolean isHoliday = holidayDates.contains(date);
        ResolvedLeaveBean leave = leaveMap.get(date);


        if (isWeekOff)        return applyWeekOff(dayAttendance);
        else if (isHoliday)   return applyHoliday(dayAttendance);
        else if (leave != null) return applyLeaveOrOd(dayAttendance, leave);
        else                  return dayAttendance;
    }

    private EmpAttendanceDTO applyWeekOff(EmpAttendanceDTO dto) {
        String existingDayType  = dto.getDayType();
        String existingDayType1 = dto.getDayType1();
        double portion=dto.getPortion();
        String dayType  = null;
        String dayType1 = null;

        if ("DP".equals(existingDayType) && existingDayType1 == null) {
            dayType = "WOP"; dayType1=null; portion=1.0;
        } else if ("DP".equals(existingDayType) && "ABS".equals(existingDayType1)) {
            dayType = "WOP"; dayType1 = "WO"; portion=0.5;

        } else if ("ABS".equals(existingDayType) && "DP".equals(existingDayType1)) {
            dayType = "WO";  dayType1 = "WOP";portion=0.5;
        } else if ("ABS".equals(existingDayType) && existingDayType1 == null) {
            dayType = "WO"; dayType1=null;portion=1.0;
        }

        return EmpAttendanceDTO.builder()
                .empShiftId(dto.getEmpShiftId())
                .employeeId(dto.getEmployeeId())
                .attendanceDate(dto.getAttendanceDate())
                .inTime(dto.getInTime())
                .outTime(dto.getOutTime())
                .portion(portion)
                .dayType(dayType)
                .dayType1(dayType1)
                .lateInMark(0.0)
                .earlyOutMark(0.0)
                .status(AttendanceStatusEnum.PROCESED)
                .build();
    }



    private EmpAttendanceDTO applyHoliday(EmpAttendanceDTO dto) {
        String existingDayType  = dto.getDayType();
        String existingDayType1 = dto.getDayType1();
        double portion=dto.getPortion();
        String dayType  = null;
        String dayType1 = null;

        if ("DP".equals(existingDayType) && existingDayType1 == null) {
            dayType = "PHP"; dayType1=null;portion=1.0;
        } else if ("DP".equals(existingDayType) && "ABS".equals(existingDayType1)) {
            dayType = "PHP"; dayType1 = "PH";portion=0.5;
        } else if ("ABS".equals(existingDayType) && "DP".equals(existingDayType1)) {
            dayType = "PH";  dayType1 = "PHP";portion=0.5;
        } else if ("ABS".equals(existingDayType) && existingDayType1 == null) {
            dayType = "PH"; dayType1=null; portion=1.0;
        }

        return EmpAttendanceDTO.builder()
                .empShiftId(dto.getEmpShiftId())
                .employeeId(dto.getEmployeeId())
                .attendanceDate(dto.getAttendanceDate())
                .inTime(dto.getInTime())
                .outTime(dto.getOutTime())
                .portion(portion)
                .dayType(dayType)
                .dayType1(dayType1)
                .lateInMark(0.0)
                .earlyOutMark(0.0)
                .status(AttendanceStatusEnum.PROCESED)
                .build();
    }
    private EmpAttendanceDTO applyLeaveOrOd(EmpAttendanceDTO dto, ResolvedLeaveBean leave) {
        String appType        = leave.getApplicationType();
        String leaveCode      = leave.getLeaveCode();
        DayType effectiveType = leave.getEffectiveType();
        String existingDayType  = dto.getDayType();
        String existingDayType1 = dto.getDayType1();
        double portion        = dto.getPortion();

        String leavePrefix = appType.equalsIgnoreCase("LEAVE_RQST") ? leaveCode : "OD";

        String dayType;
        String dayType1;

        if (DayType.FIRST_HALF.equals(effectiveType)) {

            if ("DP".equals(existingDayType) && existingDayType1 == null) {
                // Employee came full day, first half on leave  leaveP / null
                dayType  = leavePrefix + "P";
                dayType1 = null;
                portion  = 1.0;

            } else if ("ABS".equals(existingDayType) && existingDayType1 == null) {
                // Employee fully absent, first half on leave leave / ABS
                dayType  = leavePrefix;
                dayType1 = "ABS";
                portion  = 0.5;

            } else if ("ABS".equals(existingDayType) && "DP".equals(existingDayType1)) {
                // Employee came only 2nd half  leave / DP
                dayType  = leavePrefix;
                dayType1 = "DP";
                portion  = 0.5;

            } else if ("DP".equals(existingDayType) && "ABS".equals(existingDayType1)) {
                // Marked present 1st half, absent 2nd half
                dayType  = leavePrefix + "P";
                dayType1 = null;
                portion  = 0.5;

            } else {
                // Fallback — no change
                dayType  = existingDayType;
                dayType1 = existingDayType1;
            }

        } else if (DayType.SECOND_HALF.equals(effectiveType)) {

            if ("DP".equals(existingDayType) && existingDayType1 == null) {
                // Employee came full day, second half on leave  DP / leaveP
                dayType  = "DP";
                dayType1 = leavePrefix + "P";
                portion  = 1.0;

            } else if ("ABS".equals(existingDayType) && existingDayType1 == null) {
                // Employee fully absent, second half on leave  ABS / leave
                dayType  = "ABS";
                dayType1 = leavePrefix;
                portion  = 0.5;

            } else if ("DP".equals(existingDayType) && "ABS".equals(existingDayType1)) {
                // Came only 1st half  DP / leaveCode
                dayType  = "DP";
                dayType1 = leavePrefix;


                portion  = 1.0;

            } else if ("ABS".equals(existingDayType) && "DP".equals(existingDayType1)) {
                // Absent 1st half, present 2nd half  ABS / leaveP
                dayType  = "ABS";
                dayType1 = leavePrefix + "P";
                portion  = 0.5;

            } else {
                // Fallback — no change
                dayType  = existingDayType;
                dayType1 = existingDayType1;
            }

        } else {
            // FULL_DAY leave
            if ("DP".equals(existingDayType) && existingDayType1 == null) {
                // Employee came full day  leaveP / null
                dayType  = leavePrefix + "P";
                dayType1 = null;
                portion  = 1.0;

            } else if ("DP".equals(existingDayType) && "ABS".equals(existingDayType1)) {
                // Employee came only 1st half  leaveCode / ABS
                dayType  = leavePrefix;
                dayType1 = "ABS";
                portion  = 0.5;

            } else if ("ABS".equals(existingDayType) && "DP".equals(existingDayType1)) {
                // Employee came only 2nd half ABS / leaveCode
                dayType  = "ABS";
                dayType1 = leavePrefix;
                portion  = 0.5;

            } else if ("ABS".equals(existingDayType) && existingDayType1 == null) {
                // Employee fully absent leaveCode / null
                dayType  = leavePrefix;
                dayType1 = null;
                portion  = 1.0;

            } else {
                // Fallback — no change
                dayType  = existingDayType;
                dayType1 = existingDayType1;
            }
        }

        return EmpAttendanceDTO.builder()
                .empShiftId(dto.getEmpShiftId())
                .employeeId(dto.getEmployeeId())
                .attendanceDate(dto.getAttendanceDate())
                .inTime(dto.getInTime())
                .outTime(dto.getOutTime())
                .dayType(dayType)
                .dayType1(dayType1)
                .portion(portion)
                .lateInMark(0.0)
                .earlyOutMark(0.0)
                .status(AttendanceStatusEnum.PROCESED)
                .build();
    }
    private double calculateLateInMark(
            LocalDateTime inTime,
            LocalDateTime allowedInTime,
            List<ShiftRuleDTO> slabs) {

        if (inTime == null || allowedInTime == null) return 0.0;
        if (!inTime.isAfter(allowedInTime))          return 0.0;

        long minutesLate = Duration.between(allowedInTime, inTime).toMinutes();
        return getLateMarkFromSlab(minutesLate, slabs, true);
    }

    private double calculateEarlyOutMark(
            LocalDateTime outTime,
            LocalDateTime allowedOutTime,
            List<ShiftRuleDTO> slabs) {

        if (outTime == null || allowedOutTime == null) return 0.0;
        if (!outTime.isBefore(allowedOutTime))         return 0.0;

        long minutesEarly = Duration.between(outTime, allowedOutTime).toMinutes();
        return getLateMarkFromSlab(minutesEarly, slabs, false);
    }

  private double getLateMarkFromSlab(Long minutes, List<ShiftRuleDTO> slabs, Boolean isLateIn){

        if(minutes<=0) return 0.0;

        if(slabs==null || slabs.isEmpty()) return 0.0;

       List<ShiftRuleDTO> relevantSlab=slabs.stream()
               .filter(slab ->isLateIn
                               ?slab.getLateInFromMin() !=null && slab.getLateInToMin() !=null
                               :slab.getEarlyOutFromMin()!=null && slab.getEarlyOutToMin() !=null)
               .sorted(Comparator.comparing(slab -> isLateIn? slab.getLateInFromMin() : slab.getEarlyOutFromMin())).toList();

      if (relevantSlab.isEmpty()) return 0.0;

            Optional<ShiftRuleDTO> match= relevantSlab.stream()
                    .filter(slab -> isLateIn
                    ? minutes>= slab.getLateInFromMin() && minutes <= slab.getLateInToMin()
                    : minutes >= slab.getEarlyOutFromMin() && minutes <= slab.getEarlyOutToMin())
                    .findFirst();

            if(match.isPresent()){
                Float mark= isLateIn ? match.get().getLateInMarkCount() :match.get().getEarlyOutMarkCount();
                return mark!=null ? mark.doubleValue():0.0;
            }

            ShiftRuleDTO lastSlab= relevantSlab.get(relevantSlab.size()-1);
      long maxSlabLimit         = isLateIn
              ? lastSlab.getLateInToMin()
              : lastSlab.getEarlyOutToMin();

      // Exceeded highest slab → sentinel -1.0  ABS
      if (minutes > maxSlabLimit) {
          return -1.0;
      }

      // Fallback safety net (gap between slabs)
      Float lastMark = isLateIn
              ? lastSlab.getLateInMarkCount()
              : lastSlab.getEarlyOutMarkCount();
      return lastMark != null ? lastMark.doubleValue() : 0.0;
  }

    private List<ShiftRuleDTO> buildLateRule(List<ShiftDetailsBean> empShifts){

        Set<String> seen = new HashSet<>();

        List<ShiftRuleDTO> shiftRules= new ArrayList<>();

        for(ShiftDetailsBean s:empShifts){
            Integer liFrom= s.getLateInFromMin();
            Integer liTo=s.getLateInToMin();
            Float liMark=s.getLateInMarkCount();

            Integer eoFrom=s.getEarlyOutFromMin();
            Integer eoTo=s.getEarlyOutToMin();
            Float eoMark=s.getEarlyOutMarkCount();

            boolean hasLateIn= liFrom !=null && liTo!=null;
            boolean hasEarlyOut= eoFrom!=null && eoTo!=null;

            if(!hasLateIn && !hasEarlyOut) continue;

            String dedupeKey = liFrom + "_" + liTo + "_" + eoFrom + "_" + eoTo;
            if (!seen.add(dedupeKey)) continue;

            shiftRules.add(ShiftRuleDTO.builder()
                    .lateInFromMin(hasLateIn     ? liFrom.longValue() : null)
                    .lateInToMin(hasLateIn       ? liTo.longValue()   : null)
                    .lateInMarkCount(hasLateIn   ? liMark             : null)
                    .earlyOutFromMin(hasEarlyOut ? eoFrom.longValue() : null)
                    .earlyOutToMin(hasEarlyOut   ? eoTo.longValue()   : null)
                    .earlyOutMarkCount(hasEarlyOut ? eoMark           : null)
                    .build());
        }

        return shiftRules;
    }
    private Map<LocalDate, ResolvedLeaveBean> buildLeaveMap(List<LeaveRequestBean> leaves) {
        Map<LocalDate, ResolvedLeaveBean> map = new LinkedHashMap<>();
        for (LeaveRequestBean leave : leaves) {
            if (leave.getFromDate() == null || leave.getToDate() == null) continue;

            List<LocalDate> dates = leave.getFromDate()
                    .datesUntil(leave.getToDate().plusDays(1))
                    .toList();

            for (LocalDate d : dates) {
                DayType effectiveType;

                if (d.equals(leave.getFromDate()) && d.equals(leave.getToDate())) {
                    effectiveType = leave.getFromDateType();
                } else if (d.equals(leave.getFromDate())) {
                    effectiveType = leave.getFromDateType();
                } else if (d.equals(leave.getToDate())) {
                    effectiveType = leave.getToDateType();
                } else {
                    effectiveType = DayType.FULL_DAY;
                }

                map.putIfAbsent(d, new ResolvedLeaveBean(leave, effectiveType));
            }
        }
        return map;
    }
    public class ResolvedLeaveBean {
        private final LeaveRequestBean original;
        private final DayType effectiveType;

        public ResolvedLeaveBean(LeaveRequestBean original, DayType effectiveType) {
            this.original      = original;
            this.effectiveType = effectiveType;
        }

        public String getApplicationType() { return original.getApplicationType(); }
        public String getLeaveCode()       { return original.getLeaveCode(); }
        public DayType getEffectiveType()  { return effectiveType; }  // ← this
        public LocalDate getFromDate()     { return original.getFromDate(); }
        public LocalDate getToDate()       { return original.getToDate(); }
    }


    private boolean isWeekOff(List<EmployeeWeekOffBean> weekDetail, LocalDate date) {
        String weekNo  = getWeekNo(date);
        String dayName = date.getDayOfWeek().name();
        return weekDetail.stream()
                .anyMatch(w -> w.getWeekNo().equalsIgnoreCase(weekNo)
                        && w.getDay().equalsIgnoreCase(dayName));
    }

    private String getWeekNo(LocalDate date){
        int week=(date.getDayOfMonth()-1)/7+1;
        return "WEEK_"+week;
    }

}