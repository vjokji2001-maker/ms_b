package com.smartcare.hrms.employee.service;

import com.smartcare.hrms.common.bean.employee.EmployeeConfigBean;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveTemplateRepository;
import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.converter.EmployeeLeaveConverter;
import com.smartcare.hrms.employee.dto.EmployeeLeaveDTO;
import com.smartcare.hrms.employee.dto.EmployeeLeaveDtlsDTO;
import com.smartcare.hrms.employee.dto.EmployeeLeaveParentDTO;
import com.smartcare.hrms.employee.dto.EmployeeSettingConfigDTO;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeLeaveParent;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeLeaveParentRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeLeaveService {

    private @Autowired MessageResource messageResource;
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired YearCycleRepository yearCycleRepository;
    private @Autowired EmployeeLeaveParentRepository employeeLeaveParentRepository;
    private @Autowired EmployeeConfigRepository employeeConfigRepository;
    private @Autowired LeaveTemplateRepository leaveTemplateRepository;
    @Transactional
    public void create(EmployeeLeaveParentDTO dto) {
        ValidateRequest(dto);
        Employee employee = employeeRepository.findById(UUID.fromString(dto.getEmployeeId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));
        // Fetch Employee
        // Check if a leave record already exists for this employee
        EmployeeLeaveParent existingParent = employeeLeaveParentRepository.findByEmployee(employee);

        EmployeeLeaveParent employeeLeaveParent;

        if (existingParent != null) {
            // --- Update existing parent ---

            // Fetch related entities
            YearCycle yearCycle = yearCycleRepository.findById(dto.getYearId())
                    .orElseThrow(() -> new ValidationException(
                            messageResource.getEnglishMessage("year.cycle.not.found")));
            LeaveTemplate leaveTemplate = leaveTemplateRepository.findById(dto.getLeaveTemplateId())
                    .orElseThrow(() -> new ValidationException(
                            messageResource.getEnglishMessage("leave.template.not.found")));

            // Update parent fields
            existingParent.setYearCycle(yearCycle);
            existingParent.setLeaveTemplate(leaveTemplate);
            existingParent.setBalance(dto.getBalance());

            // Update children
            employeeLeaveParent = EmployeeLeaveConverter.toEntity(dto, existingParent);

        } else {
            // Employee leave does not exist → create new
            YearCycle yearCycle = yearCycleRepository.findById(dto.getYearId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("year.cycle.not.found")));

            LeaveTemplate leaveTemplate= leaveTemplateRepository.findById(dto.getLeaveTemplateId())
                    .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("leave.template.not.found")));

            EmployeeLeaveParent newParent = EmployeeLeaveParent.builder()
                    .employee(employee)
                    .yearCycle(yearCycle)
                    .leaveTemplate(leaveTemplate)
                    .build();

            employeeLeaveParent = EmployeeLeaveConverter.toEntity(dto, newParent);
        }

        // Save (insert or update)
        employeeLeaveParentRepository.save(employeeLeaveParent);
    }

    public void createBulkLeave(List<EmployeeLeaveParentDTO> dtoList){

        Set<UUID> employeeIds= dtoList.stream().map(dto ->UUID.fromString(dto.getEmployeeId())).collect(Collectors.toSet());

        Set<Long> yearIds= dtoList.stream().map(EmployeeLeaveParentDTO::getYearId).collect(Collectors.toSet());

        Set<Long> leaveTemplateIds= dtoList.stream().map(EmployeeLeaveParentDTO::getLeaveTemplateId).collect(Collectors.toSet());

        Map<UUID, Employee> employeeMap= employeeRepository.findAllById(employeeIds).stream().collect(Collectors.toMap(Employee::getId,x->x));

        Map<Long, YearCycle> yearMap= yearCycleRepository.findAllById(yearIds).stream().collect(Collectors.toMap(YearCycle::getId,y->y));

        Map<Long, LeaveTemplate> leaveTemplateMap= leaveTemplateRepository.findAllById(leaveTemplateIds).stream().collect(Collectors.toMap(LeaveTemplate::getId, z->z));

        validateBulkRequest(dtoList,employeeMap, yearMap);
        for(EmployeeLeaveParentDTO dto:dtoList){
            UUID empId=UUID.fromString(dto.getEmployeeId());
            Employee employee= employeeMap.get(empId);

            EmployeeLeaveParent existingParent= employeeLeaveParentRepository.findByEmployee(employee);
            EmployeeLeaveParent employeeLeaveParent;
            if (existingParent != null) {
                // --- Update existing parent ---

                // Fetch related entities
                YearCycle yearCycle = yearCycleRepository.findById(dto.getYearId())
                        .orElseThrow(() -> new ValidationException(
                                messageResource.getEnglishMessage("year.cycle.not.found")));
                LeaveTemplate leaveTemplate = leaveTemplateRepository.findById(dto.getLeaveTemplateId())
                        .orElseThrow(() -> new ValidationException(
                                messageResource.getEnglishMessage("leave.template.not.found")));

                // Update parent fields
                existingParent.setYearCycle(yearCycle);
                existingParent.setLeaveTemplate(leaveTemplate);
                existingParent.setBalance(dto.getBalance());

                // Update children
                employeeLeaveParent = EmployeeLeaveConverter.toEntity(dto, existingParent);

            }else{
                YearCycle yearCycle= yearMap.get(dto.getYearId());
                LeaveTemplate leaveTemplate= leaveTemplateMap.get(dto.getLeaveTemplateId());
                EmployeeLeaveParent newParent= EmployeeLeaveParent.builder()
                        .employee(employee)
                        .yearCycle(yearCycle)
                        .leaveTemplate(leaveTemplate)
                        .build();
                employeeLeaveParent= EmployeeLeaveConverter.toEntity(dto,newParent);
            }
            employeeLeaveParentRepository.save(employeeLeaveParent);
        }

    }


    public EmployeeSettingConfigDTO getEmployeeConfig(String id) {
        List<EmployeeConfigBean> beans = employeeLeaveParentRepository.getByEmployeeId(id);

        if (beans == null || beans.isEmpty()) {
            return null; // or throw exception
        }

        // Take first row for employee-level info
        EmployeeConfigBean first = beans.get(0);
        EmployeeSettingConfigDTO response = EmployeeSettingConfigDTO.builder()
                .employeeName(first.getEmployeeName())
                .employeeCode(first.getEmployeeCode())
                .companyName(first.getCompanyName())
                .branchName(first.getBranchName())
                .departmentName(first.getDepartmentName())
                .designationName(first.getDesignationName())
                .grade(first.getGrade())
                .category(first.getCategory())
                .location(first.getLocation())
                .leaveTemplateName(first.getLeaveTemplateName())
                .yearCycleName(first.getYearCycleName())
                .employeeLeaves(
                        beans.stream()
                                .map(b -> EmployeeLeaveDTO.builder()
                                        .employeeLeaveParentId(b.getEmployeeLeaveParentId())
                                        .futureDays(b.getFutureDays())
                                        .pastDays(b.getPastDays())
                                        .halfDayAllowed(b.getHalfDayAllowed())
                                        .monthlyCount(b.getMonthlyCount())
                                        .quaterlyCount(b.getQuaterlyCount())
                                        .yearlyCount(b.getYearlyCount())
                                        .leaveName(b.getLeaveName())
                                        .leaveNameId(b.getLeaveNameId())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();

        return response;
    }

    private void validateBulkRequest(List<EmployeeLeaveParentDTO> dtoList,Map<UUID,Employee>employeeMap, Map<Long, YearCycle>yearMap){
        if(CollectionUtils.isEmpty(dtoList)){
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.detail.not.found"));
        }
        for (EmployeeLeaveParentDTO dto: dtoList){
            if(dto==null){
                throw  new ValidationException(messageResource.getEnglishMessage("leave.request.detail.not.found"));
            }
            if(dto.getEmployeeId()==null||dto.getEmployeeId().isEmpty()){
                throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
            }
            UUID empId= UUID.fromString(dto.getEmployeeId());
            employeeRepository.findById(empId).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));
            if(dto.getYearId()==null||dto.getYearId()<=0){
                throw new ValidationException(messageResource.getEnglishMessage("year.details.required"));
            }
            yearCycleRepository.findById(dto.getYearId()).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("year.cycle.not.found")));
            if(dto.getLeaveTemplateId()==null||dto.getLeaveTemplateId()<=0){
                throw new ValidationException(messageResource.getEnglishMessage("leave.template.details.required"));
            }
            leaveTemplateRepository.findById(dto.getLeaveTemplateId()).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.template.not.found")));
            if (CollectionUtils.isEmpty(dto.getEmployeeLeaveDtlsDTO())) {
                throw new ValidationException("Leave details required");
            }


            for (EmployeeLeaveDtlsDTO employeeLeaveDtlsDTO:dto.getEmployeeLeaveDtlsDTO()){
                if(ValidateUtils.isLongZero(employeeLeaveDtlsDTO.getLeaveNameId())){
                    throw new ValidationException(messageResource.getEnglishMessage("leave.name.required"));
                }
                if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getYearlyCount())) {
                    throw new ValidationException(messageResource.getEnglishMessage("yearly.count"));
                }
                if (employeeLeaveDtlsDTO.getQuaterlyCount() > employeeLeaveDtlsDTO.getYearlyCount()) {
                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.less.than.yearly"));
                }
                if (employeeLeaveDtlsDTO.getMonthlyCount() > employeeLeaveDtlsDTO.getQuaterlyCount()) {
                    throw new ValidationException(messageResource.getEnglishMessage("monthly.count.less.than.quarterly"));
                }
                if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getQuaterlyCount())) {
                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.required"));
                }
                if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getMonthlyCount())) {
                    throw new ValidationException(messageResource.getEnglishMessage("Monthly.count.required"));
                }
                if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getPastDays())) {
                    throw new ValidationException(messageResource.getEnglishMessage("Past.days.required"));
                }
                if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getFutureDays())) {
                    throw new ValidationException(messageResource.getEnglishMessage("Future.days.required"));
                }
            }
        }
    }

    private void ValidateRequest(EmployeeLeaveParentDTO dto){
        if(ValidateUtils.isNullObject(dto)){
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.detail.not.found"));
        }
        if(dto.getEmployeeId()==null || dto.getEmployeeId().isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
        }
        UUID empId= UUID.fromString(dto.getEmployeeId());
        employeeRepository.findById(empId).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));
        if(ValidateUtils.isLongZero(dto.getYearId())){
            throw new ValidationException(messageResource.getEnglishMessage("year.details.required"));
        }
        yearCycleRepository.findById(dto.getYearId()).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("year.cycle.not.found")));

        if(ValidateUtils.isLongZero(dto.getLeaveTemplateId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.template.details.required"));
        }
        leaveTemplateRepository.findById(dto.getLeaveTemplateId()).orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.template.not.found")));

        if(CollectionUtils.isEmpty(dto.getEmployeeLeaveDtlsDTO())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.details.required"));
        }
        if(!CollectionUtils.isEmpty(dto.getEmployeeLeaveDtlsDTO())){
           for (EmployeeLeaveDtlsDTO employeeLeaveDtlsDTO:dto.getEmployeeLeaveDtlsDTO()){
            if(ValidateUtils.isLongZero(employeeLeaveDtlsDTO.getLeaveNameId())){
                throw new ValidationException(messageResource.getEnglishMessage("leave.name.required"));
            }
               if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getYearlyCount())) {
                   throw new ValidationException(messageResource.getEnglishMessage("yearly.count"));
               }
               if (employeeLeaveDtlsDTO.getQuaterlyCount() > employeeLeaveDtlsDTO.getYearlyCount()) {
                   throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.less.than.yearly"));
               }
               if (employeeLeaveDtlsDTO.getMonthlyCount() > employeeLeaveDtlsDTO.getQuaterlyCount()) {
                   throw new ValidationException(messageResource.getEnglishMessage("monthly.count.less.than.quarterly"));
               }
               if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getQuaterlyCount())) {
                   throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.required"));
               }
               if(ValidateUtils.isZero(employeeLeaveDtlsDTO.getMonthlyCount())) {
                   throw new ValidationException(messageResource.getEnglishMessage("Monthly.count.required"));
               }

           }

       }
    }

}
