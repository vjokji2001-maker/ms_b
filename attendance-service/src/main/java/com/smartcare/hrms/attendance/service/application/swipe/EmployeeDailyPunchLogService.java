package com.smartcare.hrms.attendance.service.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeDailyPunchLogDTO;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeDailyPunchLog;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeDailyPunchLogRepository;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeDailyPunchLogService {

    @Autowired
    private EmployeeDailyPunchLogRepository employeeDailyPunchLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private MessageResource messageResource;


    /**
     * Retrieves all punch logs for a specific employee using empId (UUID) from UI.
     * First fetches empCode from emp_dtls table, then gets punch logs.
     *
     * @param empId the employee UUID from UI (emp_dtls.id)
     * @return a list of daily punch log DTOs for the employee
     */
    public List<EmployeeDailyPunchLogDTO> getPunchLog(String empId, String date) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.employee.code"));
        }

        // 1. Get the employee to fetch empCode
        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.code.not.found")));

        LocalDate punchDate = DateHelper.convertUserDateToLocalDate(date);

        // 2. Fetch shift list for date
        List<EmployeeShift> empShift = employeeShiftRepository.findByEmployeeIdAndDate(employee.getId(), punchDate);
        if (CollectionUtils.isEmpty(empShift)) {
            throw new ValidationException(messageResource.getEnglishMessage("shift.not.assigned"));
        }
        EmployeeShift employeeShift = empShift.get(0);
        ShiftTemplate shift = employeeShift.getShiftTemplate();

        // shift timing
        LocalTime inTime = shift.getInTime();
        LocalTime outTime = shift.getOutTime();

        LocalDateTime shiftStart = LocalDateTime.of(punchDate, inTime);
        LocalDateTime shiftEnd = LocalDateTime.of(punchDate, outTime);

        // 3. Fetch logs of that day
        List<EmployeeDailyPunchLog> allLogs = employeeDailyPunchLogRepository.findByEmpCodeAndPunchDate(employee.getEmpCode(), punchDate);
        if (CollectionUtils.isEmpty(allLogs)) {
            throw new ValidationException(messageResource.getEnglishMessage("no.punch.logs.found"));
        }

        // 4. Filter by shift timing
        List<EmployeeDailyPunchLog> filteredLogs = allLogs.stream().filter(log -> !log.getPunchDateTime().isBefore(shiftStart) && !log.getPunchDateTime().isAfter(shiftEnd)).toList();
        if (filteredLogs.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("no.punch.log"));
        }

        // 5. Convert to DTO
        return filteredLogs.stream().map(this::toDTO).collect(Collectors.toList());
    }


    private EmployeeDailyPunchLogDTO toDTO(EmployeeDailyPunchLog entity) {
        if (entity == null) return null;

        return EmployeeDailyPunchLogDTO.builder()
                .id(entity.getId())
                .empCode(entity.getEmpCode())
                .punchDate(DateHelper.formatDateObjectToUserDateView(entity.getPunchDate()))
                .punchDateTime(DateHelper.formatDateTimeObjectToUserDateView(entity.getPunchDateTime()))
                .punchType(EnumConverter.toDisplayName(entity.getPunchType()))
                .punchDirection(EnumConverter.toDisplayName(entity.getSwipeDirection()))
                .build();

    }
}

