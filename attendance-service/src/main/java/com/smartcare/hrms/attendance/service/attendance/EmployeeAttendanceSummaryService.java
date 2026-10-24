package com.smartcare.hrms.attendance.service.attendance;

import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceSummaryBean;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceSalaryPayloadDTO;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceSummaryDTO;
import com.smartcare.hrms.common.entity.attendance.EmployeeAttendanceSummary;
import com.smartcare.hrms.common.repository.attendance.AttendanceSalaryLinkRepository;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceSummaryRepository;
import com.smartcare.hrms.attendance.service.attendance.external.SalaryWebClientService;
import com.smartcare.hrms.common.bean.employee.EmployeeBean;
import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class EmployeeAttendanceSummaryService {

    private @Autowired EmployeeAttendanceSummaryRepository employeeAttendanceSummaryRepository;

    private @Autowired EmployeeRepository employeeRepository;

    private @Autowired AttendanceSalaryLinkRepository attendanceSalaryLinkRepository;

    private @Autowired EmployeeAttendanceRepository employeeAttendanceRepository;

    private @Autowired SalaryWebClientService salaryWebClientService;

    public List<AttendanceSummaryDTO> calculateEmployeeAttendanceSummary(String monthYear) {

        List<EmployeeAttendanceSummaryBean> existingAttendanceSummaries = employeeAttendanceSummaryRepository
                .getEmployeeAttendanceSummary(monthYear);

        Map<String, EmployeeAttendanceSummaryBean> existingSummaryMap = existingAttendanceSummaries.stream()
                .filter(bean -> bean.getStatus() == AttendanceStatusEnum.LOCKED
                        || bean.getStatus() == AttendanceStatusEnum.FROZEN)
                .collect(Collectors.toMap(
                        bean -> bean.getAttendanceSalaryLinkId().toString(),
                        Function.identity(),
                        (existing, duplicate) -> existing  // ← THIS LINE IS MISSING
                ));

        List<EmployeeBean> empDetails = employeeRepository.getEmployeeDetails();

        List<AttendanceSalaryLink> links = attendanceSalaryLinkRepository.findByMonthYear(monthYear);

        Map<String, AttendanceSalaryLink> empIdToLink = links.stream()
                .collect(Collectors.toMap(
                        AttendanceSalaryLink::getEmployeeId,
                        Function.identity()
                ));


        double totalMonthDays = YearMonth.parse(monthYear).lengthOfMonth();

        List<AttendanceSummaryDTO> result = new ArrayList<>();

        for (EmployeeBean emp : empDetails) {

            String empId   = emp.getEmpId().toString();
            String empName = emp.getFormalName();
            String empCode = emp.getEmpCode();

            AttendanceSummaryDTO dto;
            AttendanceSalaryLink link = empIdToLink.get(empId);


            if (link != null && existingSummaryMap.containsKey(link.getId().toString())) {
                // LOCKED or FROZEN — return from DB as-is
                dto = mapBeanToDto(existingSummaryMap.get(link.getId().toString()), monthYear);

            } else if (link != null) {
                // Link exists but not locked — calculate real-time
                dto = calculateAndSave(link, monthYear);
                dto.setAttendanceSalaryLinkId(link.getId().toString());

            } else {
                // No link at all — empty summary
                dto = buildEmptySummaryDTO(empId, empName, empCode, monthYear, totalMonthDays);
            }

            dto.setEmpId(empId);
            dto.setEmpName(empName);
            dto.setEmpCode(empCode);

            result.add(dto);
        }

        return result;
    }

    private AttendanceSummaryDTO calculateAndSave(AttendanceSalaryLink link, String monthYear) {

        List<EmployeeAttendanceBean> rawAttendance = employeeAttendanceRepository
                .findAttendanceByLinkId(link.getId().toString());

        AttendanceSummaryDTO dto = calculate(rawAttendance, monthYear);
        dto.setAttendanceSalaryLinkId(link.getId().toString());

        EmployeeAttendanceSummary summary = EmployeeAttendanceSummary.builder()
                .attendanceSalaryLink(link)
                .totalMonthDays(dto.getTotalMonthDays())
                .totalPresentDays(dto.getTotalPresentDays())
                .totalWeekOffDays(dto.getTotalWeekOffDays())
                .totalPaidLeaves(dto.getTotalPaidLeaves())
                .totalHolidays(dto.getTotalHolidays())
                .totalLateMarks(dto.getTotalLateMarks())
                .totalLateAbsents(dto.getTotalLateAbsents())
                .totalLateLeaves(dto.getTotalLateLeaves())
                .totalAbsents(dto.getTotalAbsents())
                .totalUnpaidLeaves(dto.getTotalUnpaidLeaves())
                .build();

//        employeeAttendanceSummaryRepository.save(summary);
        return dto;
    }

    private AttendanceSummaryDTO calculate(List<EmployeeAttendanceBean> rows, String monthYear) {

        BigDecimal totalPresentDays  = BigDecimal.ZERO;
        BigDecimal totalWeekOffDays  = BigDecimal.ZERO;
        BigDecimal totalPaidLeaves   = BigDecimal.ZERO;
        BigDecimal totalHolidays     = BigDecimal.ZERO;
        BigDecimal totalLateAbsents  = BigDecimal.ZERO;
        BigDecimal totalAbsents      = BigDecimal.ZERO;
        BigDecimal totalNightShifts  = BigDecimal.ZERO;
        BigDecimal totalUnpaidLeaves = BigDecimal.ZERO;
        BigDecimal totalLateMark   = BigDecimal.ZERO;
        Double totalNightShiftsAmt=0.0;
        Double totalNightShiftDays=0.0;

        YearMonth ym = YearMonth.parse(monthYear);
        BigDecimal totalMonthDays = BigDecimal.valueOf(ym.lengthOfMonth());

        for (EmployeeAttendanceBean row : rows) {
            String dayType  = row.getDayType1();
            String dayType1 = row.getDayType2();
            BigDecimal portion  = row.getPortion()      != null ? BigDecimal.valueOf(row.getPortion())      : BigDecimal.ZERO;
            BigDecimal lateIn   = row.getLateInMark()   != null ? BigDecimal.valueOf(row.getLateInMark())   : BigDecimal.ZERO;
            BigDecimal earlyOut = row.getEarlyOutMark() != null ? BigDecimal.valueOf(row.getEarlyOutMark()) : BigDecimal.ZERO;
            Double nightShiftAllowanceAmount=row.getNightShiftAllowanceAmount()!=null? row.getNightShiftAllowanceAmount() :0.0;
            Double nightShiftDays = row.getNightShiftDays() != null ? row.getNightShiftDays() : 0.0;  // ← ADD

            totalNightShiftsAmt += nightShiftAllowanceAmount;
            totalNightShiftDays  += nightShiftDays;
            // Present Days (DP or any P suffix = worked)
            if ("DP".equals(dayType) || (dayType != null && dayType.endsWith("P")) ||
                    "DP".equals(dayType1) || (dayType1 != null && dayType1.endsWith("P"))) {
                totalPresentDays = totalPresentDays.add(portion);
            }

            // Week Off Days
            if ("WO".equals(dayType) || "WOP".equals(dayType) ||
                    "WO".equals(dayType1) || "WOP".equals(dayType1)) {
                totalWeekOffDays = totalWeekOffDays.add(portion);
            }

            // Holidays
            if ("PH".equals(dayType) || "PHP".equals(dayType) ||
                    "PH".equals(dayType1) || "PHP".equals(dayType1)) {
                totalHolidays = totalHolidays.add(portion);
            }

            // Paid Leaves
            if (dayType != null
                    && !dayType.equals("DP")
                    && !dayType.equals("ABS")
                    && !dayType.equals("WO")
                    && !dayType.equals("WOP")
                    && !dayType.equals("PH")
                    && !dayType.equals("PHP")) {
                totalPaidLeaves = totalPaidLeaves.add(portion);
            } else if (dayType1 != null
                    && !dayType1.equals("DP")
                    && !dayType1.equals("ABS")
                    && !dayType1.equals("WO")
                    && !dayType1.equals("WOP")
                    && !dayType1.equals("PH")
                    && !dayType1.equals("PHP")) {
                totalPaidLeaves = totalPaidLeaves.add(portion);
            }

            // Absents
            if ("ABS".equals(dayType) || "ABS".equals(dayType1)) {
                totalAbsents = totalAbsents.add(portion);
            }

            // Late Marks
            totalLateMark = totalLateMark.add(lateIn).add(earlyOut);

            // Late converted ABS (had punch but marked ABS)
            if ("ABS".equals(dayType) && row.getInTime() != null) {
                totalLateAbsents = totalLateAbsents.add(portion);
            }

            // Early out  second half ABS
            if ("DP".equals(dayType) && "ABS".equals(dayType1) && row.getInTime() != null) {
                totalLateAbsents = totalLateAbsents.add(portion);
            }
        }

        return AttendanceSummaryDTO.builder()
                .monthYear(monthYear)
                .totalMonthDays(totalMonthDays)
                .totalPresentDays(totalPresentDays)
                .totalNightShifts(totalNightShifts)
                .totalWeekOffDays(totalWeekOffDays)
                .totalPaidLeaves(totalPaidLeaves)
                .totalHolidays(totalHolidays)
                .totalLateMarks(totalLateMark)
                .totalLateAbsents(totalLateAbsents)
                .totalLateLeaves(BigDecimal.ZERO)
                .totalAbsents(totalAbsents)
                .totalUnpaidLeaves(totalUnpaidLeaves)
                .totalNightShiftsAmt(totalNightShiftsAmt)
                .totalNightShiftDays(totalNightShiftDays)
                .totalPayableDays(
                        safe(totalMonthDays).subtract(safe(totalAbsents))
                )
                .status(String.valueOf(AttendanceStatusEnum.PENDING))
                .build();
    }

    private AttendanceSummaryDTO mapBeanToDto(EmployeeAttendanceSummaryBean bean, String monthYear) {
        BigDecimal monthDays = safe(bean.getTotalMonthDays());
        BigDecimal absents   = safe(bean.getTotalAbs());

        return AttendanceSummaryDTO.builder()
                .attendanceSalaryLinkId(bean.getAttendanceSalaryLinkId())
                .empName(bean.getEmpName())
                .empCode(bean.getEmpCode())
                .monthYear(monthYear)
                .totalMonthDays(monthDays)
                .totalPresentDays(safe(bean.getTotalPresentDays()))
                .totalWeekOffDays(safe(bean.getTotalWeekOfdDays()))
                .totalPaidLeaves(safe(bean.getTotalPaidLeaves()))
                .totalHolidays(safe(bean.getTotalHolidays()))
                .totalLateMarks(safe(bean.getTotalLateMark()))
                .totalLateAbsents(safe(bean.getTotalLateAbs()))
                .totalLateLeaves(safe(bean.getTotalLateLeaves()))
                .totalAbsents(absents)
                .totalUnpaidLeaves(safe(bean.getTotalUnpaidLeaves()))
                .totalNightShifts(BigDecimal.ZERO)
                .totalPayableDays(monthDays.subtract(absents))  // now safe
                .status(String.valueOf(bean.getStatus()))
                .build();
    }

    private AttendanceSummaryDTO buildEmptySummaryDTO(String empId, String empName, String empCode,
                                                      String monthYear, double totalMonthDays) {
        return AttendanceSummaryDTO.builder()
                .empId(empId)
                .empName(empName)
                .empCode(empCode)
                .monthYear(monthYear)
                .totalMonthDays(BigDecimal.valueOf(totalMonthDays))
                .totalPresentDays(BigDecimal.ZERO)
                .totalNightShifts(BigDecimal.ZERO)
                .totalWeekOffDays(BigDecimal.ZERO)
                .totalPaidLeaves(BigDecimal.ZERO)
                .totalHolidays(BigDecimal.ZERO)
                .totalLateMarks(BigDecimal.ZERO)
                .totalLateAbsents(BigDecimal.ZERO)
                .totalLateLeaves(BigDecimal.ZERO)
                .totalAbsents(BigDecimal.ZERO)
                .totalUnpaidLeaves(BigDecimal.ZERO)
                .totalPayableDays(BigDecimal.valueOf(totalMonthDays))
                .status(String.valueOf(AttendanceStatusEnum.PENDING))
                .build();
    }
    public void lockedAttendanceSummary(List<String> attendanceSalaryLinkIds, String status, HttpHeaders headers) {

        if (!"LOCKED".equalsIgnoreCase(status)) {
            return;
        }

        List<UUID> uuids = attendanceSalaryLinkIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<AttendanceSalaryLink> links = attendanceSalaryLinkRepository.findAllById(uuids);

        if (links.isEmpty()) {
            return;
        }

        List<EmployeeAttendanceSummary> employeeAttendanceSummaries = new ArrayList<>();
        List<AttendanceSalaryPayloadDTO> salaryPayloadDTOS = new ArrayList<>();
        List<String> notLockedEmployees = new ArrayList<>();

        for (AttendanceSalaryLink link : links) {

            List<EmployeeAttendanceBean> attendanceBeans = employeeAttendanceRepository
                    .findAttendanceByLinkId(link.getId().toString());

            boolean allLocked = !attendanceBeans.isEmpty() && attendanceBeans.stream()
                    .allMatch(bean -> AttendanceStatusEnum.LOCKED.equals(bean.getStatus()));

            if (!allLocked) {
                log.warn("Attendance not fully locked for link {} (employee: {}, month: {}). " +
                                "Calculating summary without locking.",
                        link.getId(), link.getEmployeeId(), link.getMonthYear());

                notLockedEmployees.add(
                        "Employee: " + link.getEmployeeId() + ", Month: " + link.getMonthYear()
                );

                AttendanceSummaryDTO dto = calculate(attendanceBeans, link.getMonthYear());
                saveSummaryOnly(dto, link, employeeAttendanceSummaries);  // save but don't lock
                continue;
            }

            AttendanceSummaryDTO dto = calculate(attendanceBeans, link.getMonthYear());

            BigDecimal payableDaysBd = sumOrZero(dto.getTotalPresentDays(),
                    dto.getTotalHolidays(),
                    dto.getTotalPaidLeaves(),
                    dto.getTotalWeekOffDays());

            BigDecimal lossOfDaysBd  = sumOrZero(dto.getTotalAbsents(),
                    dto.getTotalUnpaidLeaves(),
                    dto.getTotalLateAbsents());

            Double payableDays = payableDaysBd.doubleValue();
            Double lossOfDays  = lossOfDaysBd.doubleValue();

            EmployeeAttendanceSummary summary = employeeAttendanceSummaryRepository
                    .findByAttendanceSalaryLinkId(link.getId())
                    .orElse(EmployeeAttendanceSummary.builder()
                            .attendanceSalaryLink(link)
                            .build());

            setSummaryFields(summary, dto);
            employeeAttendanceSummaries.add(summary);

            salaryPayloadDTOS.add(AttendanceSalaryPayloadDTO.builder()
                    .attendanceSalaryLinkId(link.getId().toString())
                    .payableDays(payableDays)
                    .lossOfPayDays(lossOfDays)
                    .build());

            link.setNightShiftAllowanceAmt(
                    dto.getTotalNightShiftsAmt() != null ? dto.getTotalNightShiftsAmt() : 0.0);
            link.setNightShiftDays(
                    dto.getTotalNightShiftDays() != null ? dto.getTotalNightShiftDays().doubleValue() : 0.0);

            link.setAttendanceStatus(AttendanceStatusEnum.LOCKED);
        }

        attendanceSalaryLinkRepository.saveAll(links);
        employeeAttendanceSummaryRepository.saveAll(employeeAttendanceSummaries);

        if (!notLockedEmployees.isEmpty()) {
            log.warn("The following employees have unlocked day-wise attendance. " +
                    "Please lock attendance first before locking summary: {}", notLockedEmployees);
        }

        if (!salaryPayloadDTOS.isEmpty()) {
            log.info("Sending {} salary payloads: {}", salaryPayloadDTOS.size(), salaryPayloadDTOS);
            salaryWebClientService.createEmployeeAttendanceSalaryLink(salaryPayloadDTOS, headers);
        }
    }

    private void saveSummaryOnly(AttendanceSummaryDTO dto, AttendanceSalaryLink link,
                                 List<EmployeeAttendanceSummary> summaries) {
        EmployeeAttendanceSummary summary = employeeAttendanceSummaryRepository
                .findByAttendanceSalaryLinkId(link.getId())
                .orElse(EmployeeAttendanceSummary.builder()
                        .attendanceSalaryLink(link)
                        .build());
        setSummaryFields(summary, dto);
        summaries.add(summary);
    }

    private void setSummaryFields(EmployeeAttendanceSummary summary, AttendanceSummaryDTO dto) {
        summary.setTotalMonthDays   (dto.getTotalMonthDays()    != null ? dto.getTotalMonthDays()    : BigDecimal.ZERO);
        summary.setTotalPresentDays (dto.getTotalPresentDays()  != null ? dto.getTotalPresentDays()  : BigDecimal.ZERO);
        summary.setTotalWeekOffDays (dto.getTotalWeekOffDays()  != null ? dto.getTotalWeekOffDays()  : BigDecimal.ZERO);
        summary.setTotalPaidLeaves  (dto.getTotalPaidLeaves()   != null ? dto.getTotalPaidLeaves()   : BigDecimal.ZERO);
        summary.setTotalHolidays    (dto.getTotalHolidays()     != null ? dto.getTotalHolidays()     : BigDecimal.ZERO);
        summary.setTotalLateMarks   (dto.getTotalLateMarks()    != null ? dto.getTotalLateMarks()    : BigDecimal.ZERO);
        summary.setTotalLateAbsents (dto.getTotalLateAbsents()  != null ? dto.getTotalLateAbsents()  : BigDecimal.ZERO);
        summary.setTotalLateLeaves  (dto.getTotalLateLeaves()   != null ? dto.getTotalLateLeaves()   : BigDecimal.ZERO);
        summary.setTotalAbsents     (dto.getTotalAbsents()      != null ? dto.getTotalAbsents()      : BigDecimal.ZERO);
        summary.setTotalUnpaidLeaves(dto.getTotalUnpaidLeaves() != null ? dto.getTotalUnpaidLeaves() : BigDecimal.ZERO);
    }

    private BigDecimal sumOrZero(BigDecimal... values){
        BigDecimal total= BigDecimal.ZERO;
        for(BigDecimal v:values){
            if(v!=null) total= total.add(v);
        }
        return  total;
    }

    private BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}