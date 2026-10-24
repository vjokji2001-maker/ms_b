package com.smartcare.hrms.migration.service.attendance;

import com.alibaba.excel.EasyExcel;
import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.shift.ShiftTemplateRepository;
import com.smartcare.hrms.migration.dao.attendance.AttendanceShiftDaoImpl;
import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadDTO;
import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadRequestDTO;
import com.smartcare.hrms.migration.dto.attendance.EmployeeShiftDTO;
import com.smartcare.hrms.migration.helper.attendance.AttendanceBatchWriter;
import com.smartcare.hrms.migration.helper.attendance.AttendanceDayDistributor;
import com.smartcare.hrms.migration.helper.attendance.AttendanceExcelParser;
import com.smartcare.hrms.migration.validator.AttendanceRowValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AttendanceUploadService {
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired EmployeeAttendanceRepository employeeAttendanceRepository;
    private @Autowired EmployeeShiftRepository employeeShiftRepository;
    private @Autowired ShiftTemplateRepository shiftTemplateRepository;
    private  @Autowired JdbcTemplate jdbcTemplate;
    private @Autowired AttendanceShiftDaoImpl attendanceShiftDaoImpl;
    private @Autowired AttendanceBatchWriter batchWriter;
    private @Autowired EmployeeShiftUpdateService employeeShiftUpdateService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int BATCH_SIZE = 100;
    private static final String GENERAL_SHIFT_NAME = "General Shift";
    private static final String GENERAL_SHIFT_CODE = "GEN";
    private static final String GENERAL_SHIFT_IN   = "10:00:00";
    private static final String GENERAL_SHIFT_OUT  = "17:00:00";


    public void uploadAttendance(MultipartFile file) {

        // ── 1. Read all rows ──────────────────────────────────────────────────
        List<AttendanceUploadDTO> allRows = readAllRows(file);
        if (allRows.isEmpty()) {
            log.warn("Excel file has no data rows.");
            return;
        }

        // ── 2. Validate attendance date formats before anything else ──────────
        validateAttendanceDates(allRows);

        // ── 3. Derive date range from Excel ───────────────────────────────────
        List<LocalDate> parsedDates = allRows.stream()
                .map(dto -> parseDate(dto.getAttendanceDate()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        LocalDate fromDate = Collections.min(parsedDates);
        LocalDate toDate   = Collections.max(parsedDates);
        log.info("Date range derived from Excel: {} → {}", fromDate, toDate);

        // ── 4. Collect distinct emp codes ─────────────────────────────────────
        List<String> empCodes = allRows.stream()
                .map(AttendanceUploadDTO::getEmpCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        // ── 5. Resolve empCode → UUID  (JPQL, single DB call) ─────────────────
        Map<String, UUID> empCodeToEmpId = resolveEmpIds(empCodes);

        List<String> unresolvedCodes = empCodes.stream()
                .filter(c -> !empCodeToEmpId.containsKey(c))
                .collect(Collectors.toList());
        if (!unresolvedCodes.isEmpty()) {
            log.warn("Emp codes not found in emp_dtls (rows will be skipped): {}", unresolvedCodes);
        }

        List<UUID> resolvedEmpIds = new ArrayList<>(empCodeToEmpId.values());
        if (resolvedEmpIds.isEmpty()) {
            throw new RuntimeException("None of the emp codes could be resolved to employee IDs.");
        }

        // ── 6. Fetch shifts for all resolved emp IDs + date range (1 DB call) ─
        List<EmployeeShift> shifts = employeeShiftRepository
                .findShiftsByEmpIdsAndDateRange(resolvedEmpIds, fromDate, toDate);

        Map<UUID, Map<LocalDate, EmployeeShift>> shiftLookup = shifts.stream()
                .collect(Collectors.groupingBy(
                        es -> es.getEmployee().getId(),
                        Collectors.toMap(EmployeeShift::getDate, s -> s, (a, b) -> a)
                ));

        log.info("Resolved {} emp IDs, {} shift records for range {} → {}",
                resolvedEmpIds.size(), shifts.size(), fromDate, toDate);

        // ── 7. Validate all shifts exist before persisting ────────────────────
        validateShiftsExist(allRows, empCodeToEmpId, shiftLookup);

        // ── 8. Map + persist in batches ───────────────────────────────────────
        processBatches(allRows, empCodeToEmpId, shiftLookup);
    }



    private List<AttendanceUploadDTO> readAllRows(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(AttendanceUploadDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read attendance file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates attendance date format for ALL rows upfront.
     * Collects ALL invalid dates first, then throws one combined error
     * so the user can fix everything at once.
     * Expected format: yyyy-MM-dd (e.g. 2026-04-01)
     */
    private void validateAttendanceDates(List<AttendanceUploadDTO> allRows) {
        List<String> errors = new ArrayList<>();
        int rowNum = 2; // row 1 = header in Excel

        for (AttendanceUploadDTO dto : allRows) {
            String empCode = dto.getEmpCode() != null ? dto.getEmpCode().trim() : "UNKNOWN";
            String dateStr = dto.getAttendanceDate();

            if (dateStr == null || dateStr.isBlank()) {
                errors.add(String.format("  Row %d → empCode=%s : Attendance Date is empty.", rowNum, empCode));
            } else {
                try {
                    LocalDate.parse(dateStr.trim(), DATE_FMT);
                } catch (Exception e) {
                    errors.add(String.format(
                            "  Row %d → empCode=%s : Invalid Attendance Date '%s'. Required format is yyyy-MM-dd (e.g. 2026-04-01).",
                            rowNum, empCode, dateStr.trim()));
                }
            }
            rowNum++;
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Attendance upload failed. Attendance Date format is incorrect for the following rows. " +
                    "Please use the format yyyy-MM-dd (e.g. 2026-04-01) and re-upload:\n" +
                    String.join("\n", errors);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        log.info("All {} rows passed attendance date format validation.", allRows.size());
    }


    private void validateShiftsExist(List<AttendanceUploadDTO> allRows,
                                     Map<String, UUID> empCodeToEmpId,
                                     Map<UUID, Map<LocalDate, EmployeeShift>> shiftLookup) {
        List<String> missingShiftErrors = new ArrayList<>();

        for (AttendanceUploadDTO dto : allRows) {
            String empCode = dto.getEmpCode();
            if (empCode == null || empCode.isBlank()) continue;

            UUID empId = empCodeToEmpId.get(empCode.trim());
            if (empId == null) continue;

            LocalDate attendanceDate = parseDate(dto.getAttendanceDate());
            if (attendanceDate == null) continue;

            EmployeeShift shift = Optional.ofNullable(shiftLookup.get(empId))
                    .map(dateMap -> dateMap.get(attendanceDate))
                    .orElse(null);

            if (shift == null) {
                missingShiftErrors.add(String.format(
                        "  - empCode=%s on date=%s", empCode.trim(), attendanceDate));
                log.warn("No shift assigned for empCode={} on date={}", empCode.trim(), attendanceDate);
            }
        }

        if (!missingShiftErrors.isEmpty()) {
            String errorMessage = "Attendance upload failed. No shift assigned for the following employees. " +
                    "Please assign shifts before uploading attendance:\n" +
                    String.join("\n", missingShiftErrors);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private void processBatches(List<AttendanceUploadDTO> allRows,
                                Map<String, UUID> empCodeToEmpId,
                                Map<UUID, Map<LocalDate, EmployeeShift>> shiftLookup) {
        List<EmployeeAttendance> batch = new ArrayList<>(BATCH_SIZE);

        for (AttendanceUploadDTO dto : allRows) {
            EmployeeAttendance entity = mapToEntity(dto, empCodeToEmpId, shiftLookup);
            if (entity != null) {
                batch.add(entity);
            }
            if (batch.size() >= BATCH_SIZE) {
                employeeAttendanceRepository.saveAll(batch);
                log.info("Saved batch of {} attendance records.", batch.size());
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            employeeAttendanceRepository.saveAll(batch);
            log.info("Saved final batch of {} attendance records.", batch.size());
        }
    }

    private EmployeeAttendance mapToEntity(AttendanceUploadDTO dto,
                                           Map<String, UUID> empCodeToEmpId,
                                           Map<UUID, Map<LocalDate, EmployeeShift>> shiftLookup) {

        String empCode = dto.getEmpCode();
        UUID empId = empCodeToEmpId.get(empCode);

        if (empId == null) {
            log.warn("Skipping row — emp code not resolved: {}", empCode);
            return null;
        }

        LocalDate attendanceDate = parseDate(dto.getAttendanceDate());
        if (attendanceDate == null) {
            log.warn("Skipping row — invalid date for empCode={}: '{}'", empCode, dto.getAttendanceDate());
            return null;
        }

        EmployeeShift shift = shiftLookup.get(empId).get(attendanceDate);

        Optional<EmployeeAttendance> existingOpt =
                employeeAttendanceRepository.findByEmployeeIdAndAttendanceDate(
                        empId.toString(), attendanceDate);

        EmployeeAttendance entity;

        if (existingOpt.isPresent()) {
            entity = existingOpt.get();

            log.info("Updating attendance for empId={} date={}", empId, attendanceDate);

        } else {
            entity = new EmployeeAttendance();
            entity.setEmployeeId(empId.toString());
            entity.setAttendanceDate(attendanceDate);

            log.info("Creating attendance for empId={} date={}", empId, attendanceDate);
        }

        entity.setEmployeeShiftId(shift.getId());
        entity.setInTime(parseTime(dto.getInTime()));
        entity.setOutTime(parseTime(dto.getOutTime()));
        entity.setStatus(AttendanceStatusEnum.PENDING);

        return entity;
    }


    private Map<String, UUID> resolveEmpIds(List<String> empCodes) {
        List<Object[]> rows = employeeRepository.findEmpCodeAndIdByEmpCodes(empCodes);
        Map<String, UUID> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (UUID) row[1]);
        }
        return result;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date.trim(), DATE_FMT);
        } catch (Exception e) {
            log.warn("Cannot parse date '{}': {}", date, e.getMessage());
            return null;
        }
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) return null;
        try {
            return LocalTime.parse(time.trim(), TIME_FMT);
        } catch (Exception e) {
            log.warn("Cannot parse time '{}': {}", time, e.getMessage());
            return null;
        }
    }

    @Transactional
    public Map<String, Object> processExcel(MultipartFile file) throws  Exception{

        List<AttendanceUploadRequestDTO> rows= AttendanceExcelParser.parse(file.getInputStream());
        if(rows.isEmpty()){
            throw new IllegalArgumentException("Excel file has no data rows");
        }
        List<String> errorRows= AttendanceRowValidator.validate(rows);
        List<AttendanceUploadRequestDTO> validaRows=AttendanceRowValidator.filterValid(rows,errorRows);

        if(validaRows.isEmpty()){
            return  Map.of("status","Failed","errors", errorRows);
        }

        Long generalShiftId= attendanceShiftDaoImpl.resolveGeneralShift();

        List<String> allCodes=validaRows.stream()
                .map(AttendanceUploadRequestDTO::getEmployeeCode)
                .distinct()
                .toList();

        Map<String, String> codeToEmpIds= attendanceShiftDaoImpl.fetchEmployeeIdsByCodes(allCodes);

        List<String> unresolvedCodes=allCodes.stream()
                .filter(c ->!codeToEmpIds.containsKey(c))
                .toList();

        if(!unresolvedCodes.isEmpty()){
            errorRows.add("Employee not found for codes : "+unresolvedCodes);
        }

        List<AttendanceUploadRequestDTO> processableRows= validaRows.stream()
                .filter(r -> codeToEmpIds.containsKey(r.getEmployeeCode()))
                .toList();

        if(processableRows.isEmpty()){
            return  Map.of("status", "Failed", "errors", errorRows);
        }

        List<EmployeeShiftDTO> allShiftDtos=buildShiftDto(processableRows, codeToEmpIds, generalShiftId);
        employeeShiftUpdateService.update(allShiftDtos);

        Map<String, String> empDateToShiftId= attendanceShiftDaoImpl.fetchAllEmpShiftIds(processableRows,codeToEmpIds);

        int attendanceCount= batchWriter.batchUpsertAttendance(processableRows,codeToEmpIds, empDateToShiftId);

        int salaryLinkCount= batchWriter.batchUpsertSalaryLinks(processableRows, codeToEmpIds);

        Map<String, Object> response= new LinkedHashMap<>();
        response.put("status","Success");
        response.put("totalRowsInFile", rows.size());
        response.put("processEmployee",processableRows.size());
        response.put("attendanceRecords",attendanceCount);
        response.put("salaryLinksUpserted",salaryLinkCount);
        if(!errorRows.isEmpty()) response.put("warnings", errorRows);

        return response;
    }

    private List<EmployeeShiftDTO> buildShiftDto(List<AttendanceUploadRequestDTO> rows,Map<String, String> codeToEmpIds, Long generalShiftId){
        List<EmployeeShiftDTO> dtos= new ArrayList<>();
        for(AttendanceUploadRequestDTO row:rows){
            String empId= codeToEmpIds.get(row.getEmployeeCode());
            YearMonth ym = YearMonth.of(row.getYear(), row.getMonth());

            for(LocalDate date: AttendanceDayDistributor.buildDateList(ym)){
                dtos.add(EmployeeShiftDTO.builder()
                                .empId(empId)
                                .date(date.toString())
                                .shiftId(generalShiftId)
                                .build());
            }
        }
        return  dtos;
    }

}