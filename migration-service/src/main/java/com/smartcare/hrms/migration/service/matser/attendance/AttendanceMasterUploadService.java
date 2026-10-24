package com.smartcare.hrms.migration.service.matser.attendance;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroupDtls;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayChild;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveName;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplateDtls;
import com.smartcare.hrms.common.entity.master.attendance.period.MonthCycle;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffDetail;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import com.smartcare.hrms.common.enumeration.DayOfWeek;
import com.smartcare.hrms.common.enumeration.WeekNo;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.approvalgroup.ApprovalGroupDtlsRepository;
import com.smartcare.hrms.common.repository.master.attendance.approvalgroup.ApprovalGroupRepository;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayChildRepository;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayParentRepository;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveNameRepository;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveTemplateDtlsRepository;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveTemplateRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.MonthCycleRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.repository.master.attendance.shift.ShiftTemplateRepository;
import com.smartcare.hrms.common.repository.master.attendance.weekOff.WeekOffDetailRepository;
import com.smartcare.hrms.common.repository.master.attendance.weekOff.WeekOffParentRepository;
import com.smartcare.hrms.common.repository.master.employment.GradeRepository;
import com.smartcare.hrms.migration.dto.master.attendance.*;
import com.smartcare.hrms.migration.dto.master.employment.CompanyUploadErrorDTO;
import com.smartcare.hrms.migration.dto.master.employment.CompanyUploadResultDTO;
import com.smartcare.hrms.migration.service.matser.employeement.CombinedMasterExcelHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceMasterUploadService {

    private @Autowired HolidayParentRepository holidayParentRepository;
    private @Autowired HolidayChildRepository holidayChildRepository;
    private @Autowired ShiftTemplateRepository shiftTemplateRepository;
    private @Autowired WeekOffParentRepository weekOffParentRepository;
    private @Autowired WeekOffDetailRepository weekOffDetailRepository;
    private @Autowired LeaveTemplateRepository leaveTemplateRepository;
    private @Autowired LeaveTemplateDtlsRepository leaveTemplateDtlsRepository;
    private @Autowired LeaveNameRepository leaveNameRepository;
    private @Autowired ApprovalGroupRepository approvalGroupRepository;
    private @Autowired ApprovalGroupDtlsRepository approvalGroupDtlsRepository;
    private @Autowired MonthCycleRepository monthCycleRepository;
    private @Autowired YearCycleRepository yearCycleRepository;
    private @Autowired GradeRepository gradeRepository;
    private @Autowired EmployeeRepository employeeRepository;

    // ─── Upload Entry Point ───────────────────────────────────────────────────────

    @Transactional
    public AttendanceMasterUploadResultDTO uploadFromExcel(MultipartFile file) throws IOException {
        if (!AttendanceMasterExcelHelper.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .xlsx file.");
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            return AttendanceMasterUploadResultDTO.builder()
                    .holidayResult(processHoliday(workbook))
                    .holidayDatesResult(processHolidayDates(workbook))
                    .shiftTemplateResult(processShiftTemplate(workbook))
                    .weekOffResult(processWeekOff(workbook))
                    .weekOffDetailsResult(processWeekOffDetails(workbook))
                    .leaveNameResult(processLeaveName(workbook))
                    .leaveTemplateResult(processLeaveTemplate(workbook))
                    .leaveTemplateDtlsResult(processLeaveTemplateDtls(workbook))
                    .approvalGroupResult(processApprovalGroup(workbook))
                    .approvalGroupDtlsResult(processApprovalGroupDtls(workbook))
                    .monthCycleResult(processMonthCycle(workbook))
                    .yearCycleResult(processYearCycle(workbook))
                    .build();
        }
    }

    // ─── Holiday ──────────────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processHoliday(Workbook workbook) {
        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_HOLIDAY)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<HolidayUploadDTO> list = AttendanceMasterExcelHelper.parseHoliday(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            HolidayUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName())) { errors.add(error(row, dto.getName(), dto.getCode(), "Holiday Name is required")); failure++; continue; }
            if (isBlank(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Holiday Code is required")); failure++; continue; }
            if (isBlank(dto.getYear())) { errors.add(error(row, dto.getName(), dto.getCode(), "Holiday Year is required")); failure++; continue; }
            if (holidayParentRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            Year year;
            try { year = Year.of(Integer.parseInt(dto.getYear())); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid Year. Use YYYY format")); failure++; continue; }

            holidayParentRepository.save(HolidayParent.builder()
                    .name(dto.getName()).code(dto.getCode()).year(year).build());
            success++;
        }
        return result("Holiday", list.size(), success, failure, errors);
    }

    // ─── Holiday Dates ────────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processHolidayDates(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_HOLIDAY_DATES)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<HolidayDateUploadDTO> list = AttendanceMasterExcelHelper.parseHolidayDates(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            HolidayDateUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getHolidayCode())) { errors.add(error(row, dto.getName(), dto.getHolidayCode(), "Holiday Code is required")); failure++; continue; }
            if (isBlank(dto.getName()))         { errors.add(error(row, dto.getName(), dto.getHolidayCode(), "Holiday Name is required")); failure++; continue; }
            if (isBlank(dto.getDate()))         { errors.add(error(row, dto.getName(), dto.getHolidayCode(), "Holiday Date is required")); failure++; continue; }

if(holidayChildRepository.existsByNameIgnoreCase(dto.getName())){ errors.add(error(row, dto.getName(),dto.getHolidayCode(),"Holiday Name  already exist"));failure++;continue;}

            Optional<HolidayParent> parentOpt = holidayParentRepository.findByCodeIgnoreCase(dto.getHolidayCode());
            if (parentOpt.isEmpty()) { errors.add(error(row, dto.getName(), dto.getHolidayCode(), "Holiday Code '" + dto.getHolidayCode() + "' not found")); failure++; continue; }

            LocalDate date;
            try { date = LocalDate.parse(dto.getDate()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getHolidayCode(), "Invalid Date. Use YYYY-MM-DD")); failure++; continue; }

            holidayChildRepository.save(HolidayChild.builder()
                    .holiday(parentOpt.get()).name(dto.getName()).date(date).checked(true).build());
            success++;
        }
        return result("HolidayDates", list.size(), success, failure, errors);
    }

    // ─── Shift Template ───────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processShiftTemplate(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_SHIFT_TEMPLATE)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<ShiftTemplateUploadDTO> list = AttendanceMasterExcelHelper.parseShiftTemplate(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ShiftTemplateUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))   { errors.add(error(row, dto.getName(), dto.getCode(), " Shift Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))   { errors.add(error(row, dto.getName(), dto.getCode(), "Shift Code is required")); failure++; continue; }
            if (isBlank(dto.getInTime())) { errors.add(error(row, dto.getName(), dto.getCode(), "Shift In Time is required")); failure++; continue; }
            if (isBlank(dto.getOutTime())){ errors.add(error(row, dto.getName(), dto.getCode(), "Shift Out Time is required")); failure++; continue; }
            if (shiftTemplateRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            LocalTime inTime, outTime;
            try { inTime = LocalTime.parse(dto.getInTime()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid In Time. Use HH:mm")); failure++; continue; }
            try { outTime = LocalTime.parse(dto.getOutTime()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid Out Time. Use HH:mm")); failure++; continue; }

            LocalTime totalWorkingHours  = parseTimeOptional(dto.getTotalWorkingHours());
            LocalTime firstHalfOutTime   = parseTimeOptional(dto.getFirstHalfOutTime());
            LocalTime secondHalfInTime   = parseTimeOptional(dto.getSecondHalfInTime());
            LocalTime totalHalfDayHours  = parseTimeOptional(dto.getTotalHalfDayHours());

            shiftTemplateRepository.save(ShiftTemplate.builder()
                    .name(dto.getName())
                    .code(dto.getCode())
                    .nightShift(parseBooleanOptional(dto.getNightShift()))
                    .halfDay(parseBooleanOptional(dto.getHalfDay()))
                    .hourBased(parseBooleanOptional(dto.getHourBased()))
                    .inTime(inTime)
                    .outTime(outTime)
                    .totalWorkingHours(totalWorkingHours)
                    .firstHalfOutTime(firstHalfOutTime)
                    .secondHalfInTime(secondHalfInTime)
                    .totalHalfDayHours(totalHalfDayHours)
                    .bufferTime(parseIntOptional(dto.getBufferTime()))
                    .cutOffTime(parseIntOptional(dto.getCutOffTime()))
                    .halfRate(parseDoubleOptional(dto.getHalfRate()))
                    .fullRate(parseDoubleOptional(dto.getFullRate()))
                    .build());
            success++;
        }
        return result("ShiftTemplate", list.size(), success, failure, errors);
    }

    // ─── Week Off ─────────────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processWeekOff(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_WEEK_OFF)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<WeekOffUploadDTO> list = AttendanceMasterExcelHelper.parseWeekOff(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            WeekOffUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName())) { errors.add(error(row, dto.getName(), dto.getCode(), "Week Off Name is required")); failure++; continue; }
            if (isBlank(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Week Off Code is required")); failure++; continue; }
            if (weekOffParentRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            weekOffParentRepository.save(WeekOffParent.builder()
                    .name(dto.getName()).code(dto.getCode()).build());
            success++;
        }
        return result("WeekOff", list.size(), success, failure, errors);
    }

    // ─── Week Off Details ─────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processWeekOffDetails(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_WEEK_OFF_DETAILS)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<WeekOffDetailUploadDTO> list = AttendanceMasterExcelHelper.parseWeekOffDetails(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            WeekOffDetailUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getWeekOffCode())) { errors.add(error(row, null, dto.getWeekOffCode(), "Week Off Code is required")); failure++; continue; }
            if (isBlank(dto.getDay()))         { errors.add(error(row, null, dto.getWeekOffCode(), "Day is required")); failure++; continue; }
            if (isBlank(dto.getWeekNo()))      { errors.add(error(row, null, dto.getWeekOffCode(), "Week No is required")); failure++; continue; }

            Optional<WeekOffParent> parentOpt = weekOffParentRepository.findByCodeIgnoreCase(dto.getWeekOffCode());
            if (parentOpt.isEmpty()) { errors.add(error(row, null, dto.getWeekOffCode(), "Week Off Code '" + dto.getWeekOffCode() + "' not found")); failure++; continue; }

            DayOfWeek day;
            try { day = DayOfWeek.valueOf(dto.getDay().toUpperCase()); }
            catch (Exception e) { errors.add(error(row, null, dto.getWeekOffCode(), "Invalid Day '" + dto.getDay() + "'. Valid: MONDAY/TUESDAY/WEDNESDAY/THURSDAY/FRIDAY/SATURDAY/SUNDAY")); failure++; continue; }

            WeekNo weekNo;
            try { weekNo = WeekNo.valueOf(dto.getWeekNo().toUpperCase()); }
            catch (Exception e) { errors.add(error(row, null, dto.getWeekOffCode(), "Invalid Week No '" + dto.getWeekNo() + "'. Valid: FIRST/SECOND/THIRD/FOURTH/FIFTH/ALL")); failure++; continue; }

            weekOffDetailRepository.save(WeekOffDetail.builder()
                    .weekOff(parentOpt.get()).day(day).weekNo(weekNo).build());
            success++;
        }
        return result("WeekOffDetails", list.size(), success, failure, errors);
    }

    // Leave Name

    private AttendanceMasterSheetResultDTO processLeaveName(Workbook workbook){
        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_LEAVE_NAME)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<LeaveNameUploadDTO> list= AttendanceMasterExcelHelper.parseLeaveName(workbook);
        int success=0, failure=0;
        List<AttendanceMasterUploadErrorDTO> errors= new ArrayList<>();

        for(int i=0;i<list.size();i++){
            LeaveNameUploadDTO dto=list.get(i);
            int row=i+2;
            if(isBlank(dto.getName())){errors.add(error(row, dto.getName(), dto.getCode(), "Leave Name is required"));failure++;continue;}
            if (isBlank(dto.getCode())){errors.add(error(row, dto.getName(),dto.getCode(),"Leave Code is required"));failure++;continue;}
            if(leaveNameRepository.existsByCode(dto.getCode())){errors.add(error(row, dto.getName(), dto.getCode(), "Code ' "+ dto.getCode() + "' alreday exist"));failure++;continue;}
        leaveNameRepository.save(LeaveName.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .build());
            success++;

        }
        return result("LeaveName", list.size(),success,failure,errors);
    }

    // ─── Leave Template ───────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processLeaveTemplate(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_LEAVE_TEMPLATE)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<LeaveTemplateUploadDTO> list = AttendanceMasterExcelHelper.parseLeaveTemplate(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            LeaveTemplateUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName())) { errors.add(error(row, dto.getName(), dto.getCode(), "Leave Template Name is required")); failure++; continue; }
            if (isBlank(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Leave Template  Code is required")); failure++; continue; }
            if (leaveTemplateRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            leaveTemplateRepository.save(LeaveTemplate.builder()
                    .name(dto.getName())
                    .code(dto.getCode())
                    .totalLeaveDays(parseDoubleOptional(dto.getTotalLeaveDays()))
                    .build());
            success++;
        }
        return result("LeaveTemplate", list.size(), success, failure, errors);
    }

    // ─── Leave Template Details ───────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processLeaveTemplateDtls(Workbook workbook) {
        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_LEAVE_TEMPLATE_DTLS)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<LeaveTemplateDtlsUploadDTO> list = AttendanceMasterExcelHelper.parseLeaveTemplateDtls(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            LeaveTemplateDtlsUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getLeaveTemplateCode())) { errors.add(error(row, null, dto.getLeaveTemplateCode(), "Leave Template Code is required")); failure++; continue; }
            if (isBlank(dto.getLeaveNameCode()))     { errors.add(error(row, null, dto.getLeaveTemplateCode(), "Leave Name Code is required")); failure++; continue; }

            Optional<LeaveTemplate> templateOpt = leaveTemplateRepository.findByCodeIgnoreCase(dto.getLeaveTemplateCode());
            if (templateOpt.isEmpty()) { errors.add(error(row, null, dto.getLeaveTemplateCode(), "Leave Template Code '" + dto.getLeaveTemplateCode() + "' not found")); failure++; continue; }

            Optional<LeaveName> leaveNameOpt = leaveNameRepository.findByCodeIgnoreCase(dto.getLeaveNameCode());
            if (leaveNameOpt.isEmpty()) { errors.add(error(row, null, dto.getLeaveTemplateCode(), "Leave Name Code '" + dto.getLeaveNameCode() + "' not found")); failure++; continue; }

            leaveTemplateDtlsRepository.save(LeaveTemplateDtls.builder()
                    .leave(templateOpt.get())
                    .leaveNameId(leaveNameOpt.get().getId())
                    .yearlyCount(parseDoubleOptional(dto.getYearlyCount()))
                    .quaterlyCount(parseDoubleOptional(dto.getQuarterlyCount()))
                    .monthlyCount(parseDoubleOptional(dto.getMonthlyCount()))
                    .pastDays(parseDoubleOptional(dto.getPastDays()))
                    .futureDays(parseDoubleOptional(dto.getFutureDays()))
                    .halfDayAllowed(Boolean.parseBoolean(dto.getHalfDayAllowed()))
                    .build());
            success++;
        }
        return result("LeaveTemplateDtls", list.size(), success, failure, errors);
    }

    // ─── Approval Group ───────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processApprovalGroup(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_APPROVAL_GROUP)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<ApprovalGroupUploadDTO> list = AttendanceMasterExcelHelper.parseApprovalGroup(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ApprovalGroupUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName())) { errors.add(error(row, dto.getName(), dto.getCode(), "Approval Name is required")); failure++; continue; }
            if (isBlank(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Approval Code is required")); failure++; continue; }
            if (approvalGroupRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            approvalGroupRepository.save(ApprovalGroup.builder()
                    .name(dto.getName()).code(dto.getCode()).build());
            success++;
        }
        return result("ApprovalGroup", list.size(), success, failure, errors);
    }

    // ─── Approval Group Details ───────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processApprovalGroupDtls(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_APPROVAL_GROUP_DTLS)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<ApprovalGroupDtlsUploadDTO> list = AttendanceMasterExcelHelper.parseApprovalGroupDtls(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ApprovalGroupDtlsUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getApprovalGroupCode())) { errors.add(error(row, null, dto.getApprovalGroupCode(), "Approval Group Code is required")); failure++; continue; }
            if (isBlank(dto.getEmpCode()))           { errors.add(error(row, null, dto.getApprovalGroupCode(), "Employee Code is required")); failure++; continue; }
            if (isBlank(dto.getSequence()))          { errors.add(error(row, null, dto.getApprovalGroupCode(), "Sequence is required")); failure++; continue; }

            Optional<ApprovalGroup> groupOpt = approvalGroupRepository.findByCodeIgnoreCase(dto.getApprovalGroupCode());
            if (groupOpt.isEmpty()) { errors.add(error(row, null, dto.getApprovalGroupCode(), "Approval Group Code '" + dto.getApprovalGroupCode() + "' not found")); failure++; continue; }

            Employee employee = employeeRepository.findByEmpCode(dto.getEmpCode());
            if (employee == null) { errors.add(error(row, null, dto.getApprovalGroupCode(), "Employee Code '" + dto.getEmpCode() + "' not found")); failure++; continue; }

            Integer sequence;
            try { sequence = Integer.parseInt(dto.getSequence()); }
            catch (Exception e) { errors.add(error(row, null, dto.getApprovalGroupCode(), "Invalid Sequence. Must be a number")); failure++; continue; }

            approvalGroupDtlsRepository.save(ApprovalGroupDtls.builder()
                    .approvalGroup(groupOpt.get())
                    .employee(employee)
                    .sequence(sequence)
                    .build());
            success++;
        }
        return result("ApprovalGroupDtls", list.size(), success, failure, errors);
    }

    // ─── Month Cycle ──────────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processMonthCycle(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_MONTH_CYCLE)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<MonthCycleUploadDTO> list = AttendanceMasterExcelHelper.parseMonthCycle(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            MonthCycleUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))      { errors.add(error(row, dto.getName(), dto.getCode(), "Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))      { errors.add(error(row, dto.getName(), dto.getCode(), "Code is required")); failure++; continue; }
            if (isBlank(dto.getStartDate())) { errors.add(error(row, dto.getName(), dto.getCode(), "Start Date is required")); failure++; continue; }
            if (isBlank(dto.getEndDate()))   { errors.add(error(row, dto.getName(), dto.getCode(), "End Date is required")); failure++; continue; }
            if (monthCycleRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            Integer startDate, endDate;
            try { startDate = Integer.parseInt(dto.getStartDate()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid Start Date. Must be a day number (1-31)")); failure++; continue; }
            try { endDate = Integer.parseInt(dto.getEndDate()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid End Date. Must be a day number (1-31)")); failure++; continue; }

            Grade grade = null;
            if (!isBlank(dto.getGradeCode())) {
                grade = gradeRepository.findByCode(dto.getGradeCode());
                if (grade == null) { errors.add(error(row, dto.getName(), dto.getCode(), "Grade Code '" + dto.getGradeCode() + "' not found")); failure++; continue; }
            }

            monthCycleRepository.save(MonthCycle.builder()
                    .name(dto.getName()).code(dto.getCode())
                    .startDate(startDate).endDate(endDate)
                    .grade(grade)
                    .build());
            success++;
        }
        return result("MonthCycle", list.size(), success, failure, errors);
    }

    // ─── Year Cycle ───────────────────────────────────────────────────────────────

    private AttendanceMasterSheetResultDTO processYearCycle(Workbook workbook) {

        if (!sheetExists(workbook, AttendanceMasterExcelHelper.SHEET_YEAR_CYCLE)) {
            return AttendanceMasterSheetResultDTO.builder()
                    .sheetName("Holiday")
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            AttendanceMasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<YearCycleUploadDTO> list = AttendanceMasterExcelHelper.parseYearCycle(workbook);
        int success = 0, failure = 0;
        List<AttendanceMasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            YearCycleUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))          { errors.add(error(row, dto.getName(), dto.getCode(), "Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))          { errors.add(error(row, dto.getName(), dto.getCode(), "Code is required")); failure++; continue; }
            if (isBlank(dto.getFromMonthYear())) { errors.add(error(row, dto.getName(), dto.getCode(), "From Month Year is required")); failure++; continue; }
            if (isBlank(dto.getToMonthYear()))   { errors.add(error(row, dto.getName(), dto.getCode(), "To Month Year is required")); failure++; continue; }
            if (yearCycleRepository.existsByCode(dto.getCode())) { errors.add(error(row, dto.getName(), dto.getCode(), "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            LocalDate fromMonthYear, toMonthYear;
            try { fromMonthYear = LocalDate.parse(dto.getFromMonthYear()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid From Month Year. Use YYYY-MM-DD")); failure++; continue; }
            try { toMonthYear = LocalDate.parse(dto.getToMonthYear()); }
            catch (Exception e) { errors.add(error(row, dto.getName(), dto.getCode(), "Invalid To Month Year. Use YYYY-MM-DD")); failure++; continue; }

            yearCycleRepository.save(YearCycle.builder()
                    .name(dto.getName()).code(dto.getCode())
                    .fromMonthYear(fromMonthYear).toMonthYear(toMonthYear)
                    .build());
            success++;
        }
        return result("YearCycle", list.size(), success, failure, errors);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private AttendanceMasterSheetResultDTO result(String sheetName, int total, int success, int failure, List<AttendanceMasterUploadErrorDTO> errors) {
        return AttendanceMasterSheetResultDTO.builder()
                .sheetName(sheetName).totalRows(total)
                .successCount(success).failureCount(failure)
                .errors(errors).build();
    }

    private AttendanceMasterUploadErrorDTO error(int row, String name, String code, String reason) {
        return AttendanceMasterUploadErrorDTO.builder()
                .rowNumber(row).name(name).code(code).reason(reason).build();
    }

    private LocalTime parseTimeOptional(String value) {
        if (isBlank(value)) return null;
        try { return LocalTime.parse(value); } catch (Exception e) { return null; }
    }

    private Boolean parseBooleanOptional(String value) {
        if (isBlank(value)) return false;
        return Boolean.parseBoolean(value.trim());
    }

    private Integer parseIntOptional(String value) {
        if (isBlank(value)) return null;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return null; }
    }

    private Double parseDoubleOptional(String value) {
        if (isBlank(value)) return null;
        try { return Double.parseDouble(value.trim()); } catch (Exception e) { return null; }
    }

    private boolean sheetExists(Workbook workbook, String sheetName) {
        return workbook.getSheet(sheetName) != null;
    }
}


