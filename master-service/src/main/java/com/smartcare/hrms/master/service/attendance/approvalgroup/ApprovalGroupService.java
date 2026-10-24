package com.smartcare.hrms.master.service.attendance.approvalgroup;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.dto.master.attendance.approvalgroup.ApprovalGroupDTO;
import com.smartcare.hrms.common.dto.master.attendance.approvalgroup.ApprovalGroupDtlsDTO;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroupDtls;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.approvalgroup.ApprovalGroupRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalGroupService {

    private final ApprovalGroupRepository approvalGroupRepository;
    private final EmployeeRepository employeeRepository;
    private final MessageResource messageResource;

    @Transactional
    public void createApprovalGroup(ApprovalGroupDTO dto) {
        validateGroupSize(dto.getApprovalGroupDtlsDTOList());
        validateRequest(dto);
        validateByRepository(dto);
        ApprovalGroup group = ApprovalGroup.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .build();


        List<ApprovalGroupDtls> dtlsList = new ArrayList<>();
        Set<UUID> processedEmployeeIds = new HashSet<>();
        Set<Integer> processedSequence= new HashSet<>();

        for (ApprovalGroupDtlsDTO dtlsDTO : dto.getApprovalGroupDtlsDTOList()) {
            UUID empId = UUID.fromString(dtlsDTO.getEmpId().trim());
            Integer sequence = dtlsDTO.getSequence();
            if (processedEmployeeIds.contains(empId)) {
                throw new ValidationException(messageResource.getEnglishMessage("duplicate.employee.found"));
            }
            if(processedSequence.contains(sequence)){
                throw new ValidationException(messageResource.getEnglishMessage("duplicate.sequence.found"));
            }

            Employee emp = employeeRepository.findById(empId)
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            ApprovalGroupDtls dtls = ApprovalGroupDtls.builder()
                    .approvalGroup(group)
                    .employee(emp)
                    .sequence(dtlsDTO.getSequence())
                    .build();

            dtlsList.add(dtls);
            processedEmployeeIds.add(empId);
            processedSequence.add(sequence);
        }


        group.setApprovalGroupDtlsList(dtlsList);
        ApprovalGroup saved = approvalGroupRepository.save(group);
        mapToDTO(saved);
    }

   @Transactional
    public void updateApprovalGroup(String groupId, ApprovalGroupDTO dto) {
        validateGroupSize(dto.getApprovalGroupDtlsDTOList());
        validateRequest(dto);

        ApprovalGroup group = approvalGroupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("approval.group.not.found")));

         if(!group.getCode().equals(dto.getCode())){
             validateByRepository(dto);
         }

        group.setName(dto.getName());
        group.setCode(dto.getCode());

        // Clear old list for orphanRemoval = true to work
        group.getApprovalGroupDtlsList().clear();

        List<ApprovalGroupDtls> newDtls = new ArrayList<>();
        Set<UUID> processedEmployeeIds = new HashSet<>();
        Set<Integer> processedSequence= new HashSet<>();

        for (ApprovalGroupDtlsDTO dtlsDTO : dto.getApprovalGroupDtlsDTOList()) {
            UUID empId= UUID.fromString(dtlsDTO.getEmpId().trim());
            Integer sequence = dtlsDTO.getSequence();
            if (processedEmployeeIds.contains(empId)) {
                throw new ValidationException(messageResource.getEnglishMessage("duplicate.employee.found"));
            }
            if(processedSequence.contains(sequence)){
                throw new ValidationException(messageResource.getEnglishMessage("duplicate.sequence.found"));
            }

            processedEmployeeIds.add(empId);
            processedSequence.add(sequence);

            Employee emp = employeeRepository.findById(empId)
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

            ApprovalGroupDtls dtls = ApprovalGroupDtls.builder()
                    .approvalGroup(group)
                    .employee(emp)
                    .sequence(dtlsDTO.getSequence())
                    .build();
            dtls.setId(dtlsDTO.getId());

            newDtls.add(dtls);
        }

        group.getApprovalGroupDtlsList().addAll(newDtls);
        ApprovalGroup saved = approvalGroupRepository.save(group);
        mapToDTO(saved);
    }

    @Transactional
    public void deleteApprovalGroup(String id) {
        Optional<ApprovalGroup> approvalGroup = approvalGroupRepository.findById(UUID.fromString(id));
        if (approvalGroup.isPresent()){
            approvalGroupRepository.deleteById(UUID.fromString(id));
        }
        else {
            throw new ValidationException(messageResource.getEnglishMessage("record.not.found"));
        }
    }


    public List<ApprovalGroupDTO> getAllByNameOrCode(ApprovalGroupDTO dto){
        List<ApprovalGroupDTO> dtoList = new ArrayList<>();

        /* fetch data by name */
        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            List<ApprovalGroup> byName = approvalGroupRepository.findByNameContainingIgnoreCase(dto.getName());
            if (!CollectionUtils.isEmpty(byName)) {
                for (ApprovalGroup approvalGroup : byName) {
                    ApprovalGroupDTO approvalGroupDTO = ApprovalGroupDTO.builder()
                            .id(approvalGroup.getId().toString())
                            .name(approvalGroup.getName())
                            .code(approvalGroup.getCode())
                            .build();
                    dtoList.add(approvalGroupDTO);
                }
            }
        } /* fetch data by code */
        else if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            ApprovalGroup approvalGroup = approvalGroupRepository.findByCode(dto.getCode());
            if (approvalGroup != null) {
                ApprovalGroupDTO approvalGroupDTO = ApprovalGroupDTO.builder()
                        .id(approvalGroup.getId().toString())
                        .name(approvalGroup.getName())
                        .code(approvalGroup.getCode())
                        .build();
                dtoList.add(approvalGroupDTO);
            }
        }
        
        else {
            /* Fetch all if name and code are empty */
            List<ApprovalGroup> all = approvalGroupRepository.findAll();
            if (!CollectionUtils.isEmpty(all)) {
                for (ApprovalGroup approvalGroup : all) {
                    ApprovalGroupDTO approvalGroupDTO = ApprovalGroupDTO.builder()
                            .id(approvalGroup.getId().toString())
                            .name(approvalGroup.getName())
                            .code(approvalGroup.getCode())
                            .build();
                    dtoList.add(approvalGroupDTO);
                }
            }
        }
        dtoList.sort(Comparator.comparing(ApprovalGroupDTO::getName,String.CASE_INSENSITIVE_ORDER).reversed());
        return dtoList;
    }

    public List<ApprovalGroupDTO> getApprovalGroup(String id) {

        List<ApprovalGroupBean> rows = approvalGroupRepository.findDetailsByApprovalGroupId(id);

        if (rows.isEmpty())
        {
           throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.found"));
        }

        // group by approval group id
        Map<UUID, List<ApprovalGroupBean>> grouped = rows.stream()
                .collect(Collectors.groupingBy(ApprovalGroupBean::getId));

        return grouped.values().stream().map(group -> {
            ApprovalGroupBean first = group.get(0);

            List<ApprovalGroupDtlsDTO> details = group.stream()
                    .map(bean -> ApprovalGroupDtlsDTO.builder()
                            .id(bean.getApprovalGroupDetailId())
                            .empId(bean.getEmployeeId())
                            .sequence(bean.getApprovalSequence())
                            .employeeName(bean.getEmployeeName())
                            .employeeCode(bean.getEmployeeCode())
                            .build())
                    .toList();

            return ApprovalGroupDTO.builder()
                    .id(first.getId().toString())
                    .name(first.getApprovalName())
                    .code(first.getApprovalCode())
                    .approvalGroupDtlsDTOList(details)
                    .build();
        }).toList();
    }

    private void validateGroupSize(List<ApprovalGroupDtlsDTO> dtlsList) {
        if (dtlsList != null && dtlsList.size() > 5) {
            throw new ValidationException(messageResource.getEnglishMessage("group.dlls.max.threshold.error"));
        }
    }

    private ApprovalGroupDTO mapToDTO(ApprovalGroup group) {
        return ApprovalGroupDTO.builder()
                .id(group.getId().toString())
                .name(group.getName())
                .code(group.getCode())
                .approvalGroupDtlsDTOList(
                        group.getApprovalGroupDtlsList().stream().map(dtls ->
                                ApprovalGroupDtlsDTO.builder()
                                        .id(dtls.getId())
                                        .empId(String.valueOf(dtls.getEmployee().getId()))
                                        .sequence(dtls.getSequence())
                                        .build()
                        ).collect(Collectors.toList())
                )
                .build();
    }

    private void validateRequest(ApprovalGroupDTO approvalGroupDTO) {
    	if(ValidateUtils.isNullObject(approvalGroupDTO)) {
    		throw new ValidationException(messageResource.getEnglishMessage("approval.group.details.required"));
    	}
    	if(ValidateUtils.isStringEmpty(approvalGroupDTO.getName())) {
    		throw new ValidationException(messageResource.getEnglishMessage("approval.name.required"));
    	}
    	if(ValidateUtils.isStringEmpty(approvalGroupDTO.getCode())) {
    		throw new ValidationException(messageResource.getEnglishMessage("approval.code.required"));
    	}
        if (CollectionUtils.isEmpty(approvalGroupDTO.getApprovalGroupDtlsDTOList())) {
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.details.required"));
        }
        for (ApprovalGroupDtlsDTO dto : approvalGroupDTO.getApprovalGroupDtlsDTOList()) {

            if (ValidateUtils.isStringEmpty(dto.getEmpId())) {
                throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
            }
            if (ValidateUtils.isZero(dto.getSequence())) {
                throw new ValidationException(messageResource.getEnglishMessage("approval.sequence.required"));
            }
        }
    }

    private void validateByRepository(ApprovalGroupDTO approvalGroupDTO){
        ApprovalGroup byCode= approvalGroupRepository.findByCode(approvalGroupDTO.getCode());
        if(!ValidateUtils.isNullObject(byCode)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.code.exist"));

        }
    }
}

