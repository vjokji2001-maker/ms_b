package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeDailyPunchLog;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.PunchType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeDailyPunchLogRepository;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffPunchRequestDTO;
import com.smartcare.hrms.staff.dto.StaffPunchResponseDTO;
import com.smartcare.hrms.staff.dto.StaffTodayAttendanceDTO;
import com.smartcare.hrms.staff.helper.FaceValidationHelper;
import com.smartcare.hrms.staff.helper.GeoLocationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaffAttendanceService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeDailyPunchLogRepository punchLogRepository;

    @Autowired
    private EmployeeAttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeShiftRepository shiftRepository;

    @Autowired
    private GeoLocationHelper geoLocationHelper;

    @Autowired
    private FaceValidationHelper faceValidationHelper;

    @Autowired
    private MessageResource messageResource;

    @Value("${hrms.geofence.default-radius-meters:200.0}")
    private double defaultGeofenceRadiusMeters;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Staff Check-In / Check-Out Punch with Location Geofence and Face Verification.
     */
    @Transactional
    public StaffPunchResponseDTO processPunch(StaffPunchRequestDTO request, String clinicId) {
        if (ValidateUtils.isNullObject(request) || ValidateUtils.isStringEmpty(request.getEmpId())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(request.getEmpId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        SwipeDirection direction = "OUT".equalsIgnoreCase(request.getPunchDirection()) ? SwipeDirection.OUT : SwipeDirection.IN;
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. Face Authentication / Selfie validation
        boolean faceVerified = false;
        String selfiePath = null;
        if (request.getFaceImageBase64() != null && !request.getFaceImageBase64().trim().isEmpty()) {
            if (!faceValidationHelper.isValidFaceImage(request.getFaceImageBase64())) {
                throw new ValidationException(messageResource.getEnglishMessage("face.verification.failed"));
            }
            selfiePath = faceValidationHelper.savePunchSelfie(request.getFaceImageBase64(), clinicId, employee.getEmpCode(), direction.name());
            faceVerified = (selfiePath != null);
        } else {
            // Face image is mandatory for staff mobile check-in/out
            throw new ValidationException(messageResource.getEnglishMessage("face.image.required"));
        }

        // 2. Location Geofence Validation
        boolean locationVerified = false;
        boolean withinGeofence = false;
        Double distanceFromOfficeMeters = null;

        if (Boolean.TRUE.equals(request.getIsRemotePunch())) {
            // Permitted remote / field punch
            locationVerified = true;
            withinGeofence = true;
        } else {
            if (request.getLatitude() == null || request.getLongitude() == null) {
                throw new ValidationException(messageResource.getEnglishMessage("location.coordinates.required"));
            }

            // Determine office target coordinates (from request override, or branch, or default)
            Double officeLat = request.getTargetLatitude();
            Double officeLon = request.getTargetLongitude();
            double allowedRadius = (request.getAllowedRadiusMeters() != null && request.getAllowedRadiusMeters() > 0)
                    ? request.getAllowedRadiusMeters()
                    : defaultGeofenceRadiusMeters;

            if (officeLat != null && officeLon != null) {
                distanceFromOfficeMeters = geoLocationHelper.calculateDistanceMeters(
                        request.getLatitude(), request.getLongitude(), officeLat, officeLon);
                withinGeofence = (distanceFromOfficeMeters <= allowedRadius);
                locationVerified = withinGeofence;

                if (!withinGeofence) {
                    throw new ValidationException(messageResource.getEnglishMessageWithParam(
                            "location.out.of.geofence",
                            new Object[]{Math.round(distanceFromOfficeMeters)}));
                }
            } else {
                // If specific office coordinates not configured, mark GPS location recorded
                locationVerified = true;
                withinGeofence = true;
            }
        }

        // 3. Find Today's Shift for the employee
        List<EmployeeShift> shifts = shiftRepository.findByEmployeeIdAndDate(employee.getId(), today);
        EmployeeShift empShift = !CollectionUtils.isEmpty(shifts) ? shifts.get(0) : null;
        ShiftTemplate shiftTemplate = empShift != null ? empShift.getShiftTemplate() : null;

        // 4. Save into EmployeeDailyPunchLog (Real Biometric / Mobile Punch Log)
        EmployeeDailyPunchLog punchLog = EmployeeDailyPunchLog.builder()
                .empCode(employee.getEmpCode())
                .punchDate(today)
                .punchDateTime(now)
                .punchType(PunchType.MANUAL)
                .swipeDirection(direction)
                .build();
        punchLogRepository.save(punchLog);

        // 5. Upsert EmployeeAttendance (emp_atndnce)
        Optional<EmployeeAttendance> existingAttendanceOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(
                employee.getId().toString(), today);

        EmployeeAttendance attendance;
        if (existingAttendanceOpt.isPresent()) {
            attendance = existingAttendanceOpt.get();
        } else {
            attendance = EmployeeAttendance.builder()
                    .employeeId(employee.getId().toString())
                    .employeeShiftId(empShift != null ? empShift.getId() : 0L)
                    .attendanceDate(today)
                    .dayType("PRESENT")
                    .status(AttendanceStatusEnum.PROCESED)
                    .portion(1.0)
                    .lateInMark(0.0)
                    .earlyOutMark(0.0)
                    .nightShiftAmount(0.0)
                    .nightShiftDays(0.0)
                    .build();
        }

        LocalTime punchTime = now.toLocalTime();
        if (direction == SwipeDirection.IN) {
            if (attendance.getInTime() == null) {
                attendance.setInTime(punchTime);
            }
            // Check for late mark if shift exists
            if (shiftTemplate != null && shiftTemplate.getInTime() != null) {
                if (punchTime.isAfter(shiftTemplate.getInTime().plusMinutes(15))) {
                    attendance.setLateInMark(1.0);
                }
            }
        } else {
            attendance.setOutTime(punchTime);
        }

        attendance.setDayType("PRESENT");
        attendance.setStatus(AttendanceStatusEnum.PROCESED);
        if (empShift != null) {
            attendance.setEmployeeShiftId(empShift.getId());
        }
        attendanceRepository.save(attendance);

        // Calculate today's working hours
        String totalWorkingHours = "0h 0m";
        if (attendance.getInTime() != null && attendance.getOutTime() != null) {
            Duration duration = Duration.between(attendance.getInTime(), attendance.getOutTime());
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            totalWorkingHours = hours + "h " + minutes + "m";
        }

        return StaffPunchResponseDTO.builder()
                .status("SUCCESS")
                .message(direction == SwipeDirection.IN ? messageResource.getEnglishMessage("punch.in.success") : messageResource.getEnglishMessage("punch.out.success"))
                .punchDate(today.format(DATE_FORMATTER))
                .punchTime(now.format(TIME_FORMATTER))
                .direction(direction.name())
                .locationVerified(locationVerified)
                .distanceFromOfficeMeters(distanceFromOfficeMeters != null ? Math.round(distanceFromOfficeMeters * 10.0) / 10.0 : null)
                .withinGeofence(withinGeofence)
                .faceVerified(faceVerified)
                .selfieUrl(selfiePath)
                .shiftName(shiftTemplate != null ? shiftTemplate.getName() : "General Shift")
                .totalWorkingHoursToday(totalWorkingHours)
                .build();
    }

    /**
     * Get real today's attendance status and punch times.
     */
    public StaffTodayAttendanceDTO getTodayAttendance(String empId) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        LocalDate today = LocalDate.now();
        List<EmployeeShift> shifts = shiftRepository.findByEmployeeIdAndDate(employee.getId(), today);
        EmployeeShift empShift = !CollectionUtils.isEmpty(shifts) ? shifts.get(0) : null;
        ShiftTemplate shiftTemplate = empShift != null ? empShift.getShiftTemplate() : null;

        Optional<EmployeeAttendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(
                employee.getId().toString(), today);

        String inTimeStr = "--:--";
        String outTimeStr = "--:--";
        String statusStr = "NOT_MARKED";
        boolean punchedIn = false;
        boolean punchedOut = false;
        boolean isLate = false;
        String totalWorkingHours = "0h 0m";

        if (attendanceOpt.isPresent()) {
            EmployeeAttendance att = attendanceOpt.get();
            statusStr = att.getDayType() != null ? att.getDayType() : (att.getStatus() != null ? att.getStatus().name() : "PRESENT");
            if (att.getInTime() != null) {
                inTimeStr = att.getInTime().format(TIME_FORMATTER);
                punchedIn = true;
            }
            if (att.getOutTime() != null) {
                outTimeStr = att.getOutTime().format(TIME_FORMATTER);
                punchedOut = true;
            }
            if (att.getLateInMark() != null && att.getLateInMark() > 0) {
                isLate = true;
            }
            if (att.getInTime() != null && att.getOutTime() != null) {
                Duration duration = Duration.between(att.getInTime(), att.getOutTime());
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                totalWorkingHours = hours + "h " + minutes + "m";
            }
        }

        return StaffTodayAttendanceDTO.builder()
                .date(today.format(DATE_FORMATTER))
                .status(statusStr)
                .inTime(inTimeStr)
                .outTime(outTimeStr)
                .totalWorkingHours(totalWorkingHours)
                .shiftName(shiftTemplate != null ? shiftTemplate.getName() : "General Shift")
                .shiftInTime(shiftTemplate != null && shiftTemplate.getInTime() != null ? shiftTemplate.getInTime().format(TIME_FORMATTER) : "09:30 AM")
                .shiftOutTime(shiftTemplate != null && shiftTemplate.getOutTime() != null ? shiftTemplate.getOutTime().format(TIME_FORMATTER) : "06:30 PM")
                .isPunchedIn(punchedIn)
                .isPunchedOut(punchedOut)
                .isLate(isLate)
                .build();
    }

    /**
     * Get punch logs for a given date.
     */
    public List<Map<String, Object>> getPunchHistory(String empId, String dateStr) {
        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        LocalDate date = (dateStr != null && !dateStr.trim().isEmpty())
                ? DateHelper.convertUserDateToLocalDate(dateStr)
                : LocalDate.now();

        List<EmployeeDailyPunchLog> logs = punchLogRepository.findByEmpCodeAndPunchDate(employee.getEmpCode(), date);
        return logs.stream().map(logItem -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", logItem.getId());
            map.put("punchDateTime", logItem.getPunchDateTime() != null ? logItem.getPunchDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")) : null);
            map.put("direction", logItem.getSwipeDirection() != null ? logItem.getSwipeDirection().name() : "IN");
            map.put("punchType", logItem.getPunchType() != null ? logItem.getPunchType().name() : "MANUAL");
            return map;
        }).collect(Collectors.toList());
    }
}
