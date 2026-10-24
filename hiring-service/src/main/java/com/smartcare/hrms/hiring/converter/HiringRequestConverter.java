package com.smartcare.hrms.hiring.converter;


import org.springframework.stereotype.Component;
import com.smartcare.hrms.hiring.dto.CareerJobDTO;
import com.smartcare.hrms.hiring.dto.HiringRequestDTO;
import com.smartcare.hrms.hiring.model.entity.HiringRequest;

@Component
public class HiringRequestConverter {

    public HiringRequest toEntity(HiringRequestDTO dto) {
        return HiringRequest.builder()
                .departmentId(dto.getDepartmentId())
                .designationId(dto.getDesignationId())
                .countryId(dto.getCountryId())
                .stateId(dto.getStateId())
                .cityId(dto.getCityId())
                .vacancies(dto.getVacancies())
                .employmentType(dto.getEmploymentType())
                .experience(dto.getExperience())
                .joiningDate(dto.getJoiningDate())
                .priority(dto.getPriority())
                .reportingManager(dto.getReportingManager())
                .reason(dto.getReason())
                .jobDescription(dto.getJobDescription())
                .remarks(dto.getRemarks())
                .createdBy(dto.getCreatedBy())
                .build();
    }

    public HiringRequestDTO toDTO(HiringRequest entity) {
        HiringRequestDTO dto = new HiringRequestDTO();

        dto.setId(entity.getId());
        dto.setRequestId(entity.getRequestId());
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setDesignationId(entity.getDesignationId());
        dto.setCountryId(entity.getCountryId());
        dto.setStateId(entity.getStateId());
        dto.setCityId(entity.getCityId());
        dto.setVacancies(entity.getVacancies());
        dto.setEmploymentType(entity.getEmploymentType());
        dto.setExperience(entity.getExperience());
        dto.setJoiningDate(entity.getJoiningDate());
        dto.setPriority(entity.getPriority());
        dto.setReportingManager(entity.getReportingManager());
        dto.setReason(entity.getReason());
        dto.setJobDescription(entity.getJobDescription());
        dto.setRemarks(entity.getRemarks());
        dto.setHiredCount(entity.getHiredCount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());

        if (entity.getCreatedDate() != null) {
            dto.setCreatedDate(entity.getCreatedDate().toString());
        }

        return dto;
    }

    public CareerJobDTO toCareerDTO(HiringRequest entity) {
        return CareerJobDTO.builder()
                .id(entity.getId())
                .requestId(entity.getRequestId())
                .departmentId(entity.getDepartmentId())
                .designationId(entity.getDesignationId())
                .countryId(entity.getCountryId())
                .stateId(entity.getStateId())
                .cityId(entity.getCityId())
                .jobDescription(entity.getJobDescription())
                .vacancies(entity.getVacancies())
                .employmentType(entity.getEmploymentType().name())
                .experience(entity.getExperience())
                .joiningDate(entity.getJoiningDate().toString())
                .priority(entity.getPriority().name())
                .hiredCount(entity.getHiredCount())
                .status(entity.getStatus().name())
                .build();
    }
}