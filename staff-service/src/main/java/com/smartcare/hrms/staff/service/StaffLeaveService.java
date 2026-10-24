package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequestDtls;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeLeaveParent;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeLeaveParentRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffLeaveApplyDTO;
import com.smartcare.hrms.staff.dto.StaffLeaveBalanceDTO;
import com.smartcare.hrms.staff.dto.StaffLeaveRequestItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaffLeaveService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeaveParentRepository leaveParentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private MessageResource messageResource;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Get real leave balances for employee for the current year.
     */
    public List<StaffLeaveBalanceDTO> getLeaveBalances(String empId) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        EmployeeLeaveParent leaveParent = leaveParentRepository.findByEmployee(employee);
        if (leaveParent == null || leaveParent.getYearCycle() == null) {
            return Collections.emptyList();
        }

        Long yearId = leaveParent.getYearCycle().getId();

        List<LeaveRequestBean> leaveRequests = leaveRequestRepository.getEmployeeLeaveRequestDataByEmpIdAndYearId(empId, yearId);
        List<EmployeeLeaveDataBean> leaveDataList = leaveRequestRepository.getLeaveDetailsByYearAndEmpId(empId, yearId);

        Map<Long, List<LeaveRequestBean>> requestByLeaveId = leaveRequests != null
                ? leaveRequests.stream().collect(Collectors.groupingBy(LeaveRequestBean::getLeaveNameId))
                : new HashMap<>();

        List<StaffLeaveBalanceDTO> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(leaveDataList)) {
            for (EmployeeLeaveDataBean leaveData : leaveDataList) {
                Long leaveNameId = leaveData.getLeaveNameId();
                String leaveName = leaveData.getLeaveName();
                Double yearlyCount = leaveData.getYearlyCount() != null ? leaveData.getYearlyCount().doubleValue() : 0.0;

                List<LeaveRequestBean> requests = requestByLeaveId.getOrDefault(leaveNameId, Collections.emptyList());

                double approvedCount = requests.stream()
                        .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                        .mapToDouble(r -> r.getTotalLeaveCount() != null ? r.getTotalLeaveCount() : 0.0)
                        .sum();

                double pendingCount = requests.stream()
                        .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                        .mapToDouble(r -> r.getTotalLeaveCount() != null ? r.getTotalLeaveCount() : 0.0)
                        .sum();

                double remaining = Math.max(0.0, yearlyCount - approvedCount - pendingCount);

                result.add(StaffLeaveBalanceDTO.builder()
                        .leaveNameId(leaveNameId)
                        .leaveName(leaveName)
                        .leaveCode(leaveName != null ? leaveName.replaceAll("\\s+", "").toUpperCase() : "")
                        .totalCredited(yearlyCount)
                        .totalUtilized(approvedCount)
                        .remainingBalance(remaining)
                        .build());
            }
        }
        return result;
    }

    /**
     * Staff applies for leave.
     */
    @Transactional
    public Map<String, Object> applyLeave(StaffLeaveApplyDTO applyDTO) {
        if (ValidateUtils.isNullObject(applyDTO) || ValidateUtils.isStringEmpty(applyDTO.getEmpId())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(applyDTO.getEmpId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        EmployeeLeaveParent leaveParent = leaveParentRepository.findByEmployee(employee);
        if (leaveParent == null || leaveParent.getYearCycle() == null) {
            throw new ValidationException("No active leave template or year cycle assigned to employee");
        }

        LocalDate fromDate = DateHelper.convertUserDateToLocalDate(applyDTO.getFromDate());
        LocalDate toDate = DateHelper.convertUserDateToLocalDate(applyDTO.getToDate());

        if (fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after To date");
        }

        double totalDays = applyDTO.getTotalDays() != null && applyDTO.getTotalDays() > 0
                ? applyDTO.getTotalDays()
                : (ChronoUnit.DAYS.between(fromDate, toDate) + 1.0);

        DayType fromType = parseDayType(applyDTO.getFromDateType());
        DayType toType = parseDayType(applyDTO.getToDateType());

        if (fromType != DayType.FULL_DAY && totalDays == 1.0) {
            totalDays = 0.5;
        }

        // Verify balance
        List<StaffLeaveBalanceDTO> balances = getLeaveBalances(applyDTO.getEmpId());
        StaffLeaveBalanceDTO targetBalance = balances.stream()
                .filter(b -> b.getLeaveNameId().equals(applyDTO.getLeaveNameId()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Selected leave type not assigned to employee"));

        if (targetBalance.getRemainingBalance() < totalDays) {
            throw new ValidationException(messageResource.getEnglishMessage("insufficient.leave.balance"));
        }

        // Create LeaveODRequest
        LeaveODRequest request = LeaveODRequest.builder()
                .employeeId(employee.getId().toString())
                .yearId(leaveParent.getYearCycle().getId())
                .applicationType(ApplicationType.LEAVE_RQST)
                .leaveNameId(applyDTO.getLeaveNameId())
                .applicationDate(LocalDate.now())
                .fromDate(fromDate)
                .fromDateType(fromType)
                .toDate(toDate)
                .toDateType(toType)
                .totalCount(totalDays)
                .status(ApplicationStatus.PENDING)
                .reason(applyDTO.getReason())
                .createdBy(employee.getEmpCode())
                .build();

        // Create first approval step
        LeaveODRequestDtls step1 = LeaveODRequestDtls.builder()
                .leaveODRequest(request)
                .sequence(1)
                .status(ApplicationStatus.PENDING)
                .remark("Pending with Reporting Manager")
                .build();

        request.setRequestDetails(List.of(step1));
        LeaveODRequest saved = leaveRequestRepository.save(request);

        Map<String, Object> resp = new HashMap<>();
        resp.put("requestId", saved.getId());
        resp.put("status", "SUCCESS");
        resp.put("message", messageResource.getEnglishMessage("leave.applied.success"));
        resp.put("totalDays", totalDays);
        return resp;
    }

    /**
     * Get employee's leave request history.
     */
    public List<StaffLeaveRequestItemDTO> getMyLeaveHistory(String empId) {
        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        EmployeeLeaveParent leaveParent = leaveParentRepository.findByEmployee(employee);
        if (leaveParent == null || leaveParent.getYearCycle() == null) {
            return Collections.emptyList();
        }

        Long yearId = leaveParent.getYearCycle().getId();
        List<LeaveRequestBean> requests = leaveRequestRepository.getEmployeeLeaveRequestDataByEmpIdAndYearId(empId, yearId);

        if (CollectionUtils.isEmpty(requests)) {
            return Collections.emptyList();
        }

        List<StaffLeaveRequestItemDTO> result = new ArrayList<>();
        for (LeaveRequestBean req : requests) {
            String fromDateStr = req.getFromDate() != null ? req.getFromDate().format(DATE_FORMATTER) : "";
            String toDateStr = req.getToDate() != null ? req.getToDate().format(DATE_FORMATTER) : "";
            String appDateStr = req.getApplicationDate() != null ? req.getApplicationDate().format(DATE_FORMATTER) : "";
            String fromTypeStr = req.getFromDateType() != null ? req.getFromDateType().name() : "FULL_DAY";
            String toTypeStr = req.getToDateType() != null ? req.getToDateType().name() : "FULL_DAY";
            String leaveNameStr = req.getLeaveName() != null ? req.getLeaveName() : "Leave";

            StaffLeaveRequestItemDTO item = StaffLeaveRequestItemDTO.builder()
                    .requestId(req.getLeaveRequestId())
                    .leaveNameId(req.getLeaveNameId())
                    .leaveName(leaveNameStr)
                    .fromDate(fromDateStr)
                    .fromDateType(fromTypeStr)
                    .toDate(toDateStr)
                    .toDateType(toTypeStr)
                    .totalDays(req.getTotalLeaveCount() != null ? req.getTotalLeaveCount() : 1.0)
                    .status(req.getStatus() != null ? req.getStatus() : "PENDING")
                    .reason(req.getReason())
                    .approverRemark(req.getRemark())
                    .applicationDate(appDateStr)
                    .build();

            result.add(item);
        }

        return result;
    }

    private DayType parseDayType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return DayType.FULL_DAY;
        }
        try {
            return DayType.valueOf(typeStr.toUpperCase());
        } catch (Exception e) {
            return DayType.FULL_DAY;
        }
    }
}
