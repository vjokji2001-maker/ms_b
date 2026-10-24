package com.smartcare.hrms.employee.service;

import com.smartcare.hrms.common.bean.employee.EmployeeConfigBean;
import com.smartcare.hrms.common.entity.employee.EmployeeConfig;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.approvalgroup.ApprovalGroupRepository;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayParentRepository;
import com.smartcare.hrms.common.repository.master.attendance.weekOff.WeekOffParentRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.converter.EmployeeConfigConverter;
import com.smartcare.hrms.employee.dao.EmployeeConfigDashboardDAO;
import com.smartcare.hrms.employee.dto.EmployeeConfigDTO;
import com.smartcare.hrms.employee.dto.EmployeeConfigDashboardDTO;
import com.smartcare.hrms.employee.dto.EmployeeConfigSearchDTO;
import com.smartcare.hrms.employee.dto.EmployeeSettingConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeConfigService {

    private @Autowired EmployeeConfigDashboardDAO employeeConfigDashboardDAO;
    private @Autowired EmployeeConfigRepository employeeConfigRepository;
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired HolidayParentRepository holidayParentRepository;
    private @Autowired WeekOffParentRepository weekOffParentRepository;
    private @Autowired ApprovalGroupRepository approvalGroupRepository;
    private @Autowired MessageResource messageResource;


    public PaginatedDataResponse<EmployeeConfigDashboardDTO> getDashboardData(EmployeeConfigSearchDTO dto) {
        if (ValidateUtils.isZero(dto.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return employeeConfigDashboardDAO.getData(dto);
    }


    public EmployeeSettingConfigDTO getByEmployeeId(String employeeId) {
        EmployeeConfigBean bean = employeeConfigRepository.getByEmployeeId(employeeId);
        if (!ValidateUtils.isNullObject(bean)) {
            return convertBeanToDTO(bean);
        } else {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
    }


    public void updateSingleHoliday(EmployeeConfigDTO dto, String employeeId) {


        if (employeeId != null && !employeeId.isEmpty()) {

            validateRequestForHoliday(dto);

            UUID empId = UUID.fromString(employeeId);
            /* find employee by employeeId */
            Optional<EmployeeConfig> employeeConfigOptional = employeeConfigRepository.findByEmployee_Id(empId);

            if (employeeConfigOptional.isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
            }

            EmployeeConfig employeeConfig = employeeConfigOptional.get();
            /* update single holiday */
            EmployeeConfigConverter.convertForUpdateForSingleHoliday(employeeConfig, dto);

            employeeConfigRepository.save(employeeConfig);

        } else {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

    }

    public void updateSingleWeekOff(EmployeeConfigDTO dto, String employeeId) {
        if (employeeId != null && !employeeId.isEmpty()) {
            validateRequestForWeekOff(dto);

            UUID empId = UUID.fromString(dto.getEmployeeId());
            /* find employee by employeeId */
            Optional<EmployeeConfig> employeeConfigOptional = employeeConfigRepository.findByEmployee_Id(empId);
            if (employeeConfigOptional.isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
            }
            EmployeeConfig employeeConfig = employeeConfigOptional.get();
            /* update single weekoff */
            EmployeeConfigConverter.convertForUpdateForSingleWeekOff(employeeConfig, dto);
            employeeConfigRepository.save(employeeConfig);
        } else {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
    }


    public void updateMultipleHoliday(List<EmployeeConfigDTO> dtoList) {

        if (ValidateUtils.isNullObject(dtoList) || dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.holiday.details.required"));
        }

        validateMultipleRequestForHoliday(dtoList);


        List<UUID> employeeIds = dtoList.stream()
                .map(dto -> UUID.fromString(dto.getEmployeeId()))
                .toList();

        /* Fetch all EmployeeConfig entities for these employeeIds */
        List<EmployeeConfig> employeeConfigs = employeeConfigRepository.findByEmployee_IdIn(employeeIds);

        if (employeeConfigs.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        /* Update each entity using  DTO */
        for (EmployeeConfig entity : employeeConfigs) {
            EmployeeConfigDTO dto = dtoList.stream()
                    .filter(d -> UUID.fromString(d.getEmployeeId()).equals(entity.getEmployee().getId()))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            /* Directly update HolidayParent */
            if (dto.getHolidayId() != null && dto.getHolidayId() > 0) {
                HolidayParent holidayParent = new HolidayParent();
                holidayParent.setId(dto.getHolidayId());
                entity.setHolidayParent(holidayParent);
            }

        }
        /*  Save all updated entities in bulk */
        employeeConfigRepository.saveAll(employeeConfigs);
    }

    public void updateMultipleWeekOff(List<EmployeeConfigDTO> dtoList) {

        if (ValidateUtils.isNullObject(dtoList) || dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.week.off.details.required"));
        }

        validateMultipleRequestForWeekOff(dtoList);


        List<UUID> employeeIds = dtoList.stream()
                .map(dto -> UUID.fromString(dto.getEmployeeId()))
                .toList();

        /* Fetch all EmployeeConfig entities for these employeeIds */
        List<EmployeeConfig> employeeConfigs = employeeConfigRepository.findByEmployee_IdIn(employeeIds);

        if (employeeConfigs.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        /* Update each entity using  DTO */
        for (EmployeeConfig entity : employeeConfigs) {
            EmployeeConfigDTO dto = dtoList.stream()
                    .filter(d -> UUID.fromString(d.getEmployeeId()).equals(entity.getEmployee().getId()))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            /*  Directly update WeekOffParent */
            if (dto.getWeekOffId() != null && dto.getWeekOffId() > 0) {
                WeekOffParent weekOff = new WeekOffParent();
                weekOff.setId(dto.getWeekOffId());
                entity.setWeekOffId(weekOff);
            }


        }

        /*  Save all updated entities in bulk */
        employeeConfigRepository.saveAll(employeeConfigs);
    }

    public void updateMultipleApprovalGroup(List<EmployeeConfigDTO> dtoList) {
        if (ValidateUtils.isNullObject(dtoList) || dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.approval.group.details.required"));
        }
        validateMultipleRequestForApprovalGroup(dtoList);

        List<UUID> employeeIds = dtoList.stream()
                .map(dto -> UUID.fromString(dto.getEmployeeId()))
                .toList();

        List<EmployeeConfig> employeeConfigs = employeeConfigRepository.findByEmployee_IdIn(employeeIds);
        if (employeeConfigs.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        for (EmployeeConfig entity : employeeConfigs) {
            EmployeeConfigDTO dto = dtoList.stream()
                    .filter(e -> UUID.fromString(e.getEmployeeId()).equals(entity.getEmployee().getId()))
                    .findFirst().orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            UUID approvalId = UUID.fromString(dto.getApprovalId());
            ApprovalGroup approvalGroup = new ApprovalGroup();
            approvalGroup.setId(approvalId);
            entity.setApprovalId(approvalGroup);
        }
        employeeConfigRepository.saveAll(employeeConfigs);

    }


    public void updateSingleApprovalGroup(EmployeeConfigDTO dto, String employeeId) {
        if (employeeId != null && !employeeId.isEmpty()) {
            validateSingleRequestForApprovalGroup(dto);

            UUID empId = UUID.fromString(dto.getEmployeeId());

            Optional<EmployeeConfig> empOptional = employeeConfigRepository.findByEmployee_Id(empId);
            if (empOptional.isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
            }
            EmployeeConfig employeeConfig = empOptional.get();
            EmployeeConfigConverter.convertForUpdateSingleApprovalGroup(employeeConfig, dto);
            employeeConfigRepository.save(employeeConfig);
        } else {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
    }








    private void validateRequestForHoliday(EmployeeConfigDTO dto) {
        if (ValidateUtils.isNullObject(dto)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.holiday.details.required"));
        }

        if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
        }

        UUID empId = UUID.fromString(dto.getEmployeeId());
        // find employee is present or not
        employeeRepository.findById(empId).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        if (dto.getHolidayId() == null || dto.getHolidayId() == 0) {
            throw new ValidationException(messageResource.getEnglishMessage("holiday.details.required"));
        }
        // find holiday is present or not
        holidayParentRepository.findById(dto.getHolidayId())
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("holiday.not.found")));

    }


    private void validateMultipleRequestForHoliday(List<EmployeeConfigDTO> dtoList) {

        if (ValidateUtils.isNullObject(dtoList)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.holiday.details.required"));
        }
        for (EmployeeConfigDTO dto : dtoList) {
            if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
            }
            UUID empId = UUID.fromString(dto.getEmployeeId());
            employeeRepository.findById(empId).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            if (dto.getHolidayId() == null || dto.getHolidayId() == 0) {
                throw new ValidationException(messageResource.getEnglishMessage("holiday.details.required"));
            }
            holidayParentRepository.findById(dto.getHolidayId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("holiday.not.found")));

        }
    }


    private void validateRequestForWeekOff(EmployeeConfigDTO dto) {


        if (ValidateUtils.isNullObject(dto)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.week.off.details.required"));
        }

        if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
        }

        UUID empId = UUID.fromString(dto.getEmployeeId());
        employeeRepository.findById(empId).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        if (dto.getWeekOffId() == null || dto.getWeekOffId() == 0) {
            throw new ValidationException(messageResource.getEnglishMessage("week.off.details.required"));
        }
        weekOffParentRepository.findById(dto.getWeekOffId())
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("week.off.not.found")));


    }

    private void validateMultipleRequestForWeekOff(List<EmployeeConfigDTO> dtoList) {
        if (ValidateUtils.isNullObject(dtoList)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.week.off.details.required"));
        }
        for (EmployeeConfigDTO dto : dtoList) {
            if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
            }
            UUID empId = UUID.fromString(dto.getEmployeeId());
            employeeRepository.findById(empId)
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));
            if (dto.getWeekOffId() == null || dto.getWeekOffId() == 0) {
                throw new ValidationException(messageResource.getEnglishMessage("week.off.details.required"));
            }
            weekOffParentRepository.findById(dto.getWeekOffId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("week.off.not.found")));

        }
    }


    private void validateSingleRequestForApprovalGroup(EmployeeConfigDTO dto) {
        if (ValidateUtils.isNullObject(dto)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.approval.group.details.required"));
        }
        if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
        }
        UUID empIdUuid = UUID.fromString(dto.getEmployeeId());
        employeeRepository.findById(empIdUuid)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        if (dto.getApprovalId() == null || dto.getApprovalId().isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.details.required"));
        }
        UUID approvalGroupId = UUID.fromString(dto.getApprovalId());
        approvalGroupRepository.findById(approvalGroupId)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("approval.group.not.found")));
    }

    private void validateMultipleRequestForApprovalGroup(List<EmployeeConfigDTO> dtoList) {
        if (ValidateUtils.isNullObject(dtoList)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.approval.group.details.required"));
        }
        for (EmployeeConfigDTO dto : dtoList) {
            if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
            }
            UUID empId = UUID.fromString(dto.getEmployeeId());
            employeeRepository.findById(empId)
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            if (dto.getApprovalId() == null || dto.getApprovalId().isEmpty()) {
                throw new ValidationException(messageResource.getEnglishMessage("approval.group.details.required"));
            }
            UUID approvalGroupId = UUID.fromString(dto.getApprovalId());
            approvalGroupRepository.findById(approvalGroupId)
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("approval.group.not.found")));
        }
    }

    private EmployeeSettingConfigDTO convertBeanToDTO(EmployeeConfigBean bean) {
        return EmployeeSettingConfigDTO.builder()
                .employeeName(bean.getEmployeeName())
                .employeeCode(bean.getEmployeeCode())
                .companyName(bean.getCompanyName())
                .branchName(bean.getBranchName())
                .departmentName(bean.getDepartmentName())
                .designationName(bean.getDesignationName())
                .grade(bean.getGrade())
                .category(bean.getCategory())
                .location(bean.getLocation())
                .holidayName(bean.getHolidayName())
                .weekOffName(bean.getWeekOffName())
                .approvalGroupName(bean.getApprovalGroupName())
                .build();
    }
}   




