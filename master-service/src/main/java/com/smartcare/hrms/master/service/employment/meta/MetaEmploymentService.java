package com.smartcare.hrms.master.service.employment.meta;

import com.smartcare.hrms.common.bean.master.employment.BranchDetailsBean;
import com.smartcare.hrms.common.bean.master.employment.CompanyDetailsBean;
import com.smartcare.hrms.common.dto.master.employment.*;
import com.smartcare.hrms.common.dto.master.employment.meta.MetaEmploymentDTO;
import com.smartcare.hrms.common.entity.master.employment.*;
import com.smartcare.hrms.common.repository.master.employment.*;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MetaEmploymentService {

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private WorkLocationRepository workLocationRepository;

    @Autowired
    private SubWorkLocationRepository subWorkLocationRepository;

    @Autowired
    private BranchDetailsRepository branchDetailsRepository;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    /**
     * Retrieves all master/employment-related data for dropdowns and reference fields.
     *
     * @return a {@link MetaEmploymentDTO} containing lists of departments, designations,
     *         categories, grades, work locations, sub work locations, branches, and companies
     */
    public MetaEmploymentDTO getAllEmploymentMasterData(){

        MetaEmploymentDTO metaEmploymentDTO = new MetaEmploymentDTO();

        // Populate Designation master data lists
        metaEmploymentDTO.setDesignationList(designationRepository.findAll()
                .stream()
                .map(this::toDesignationDto)
                .collect(Collectors.toList()));

        // Populate Department master data lists
        metaEmploymentDTO.setDepartmentList(departmentRepository.findAll()
                .stream()
                .map(this::toDepartmentDto)
                .collect(Collectors.toList()));

        // Populate Category master data lists
        metaEmploymentDTO.setCategoryList(categoryRepository.findAll()
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList()));

        // Populate Grade master data lists
        metaEmploymentDTO.setGradeList(gradeRepository.findAll()
                .stream()
                .map(this::toGradeDto)
                .collect(Collectors.toList()));

        // Populate WorkLocation master data lists
        metaEmploymentDTO.setWorkLocationList(workLocationRepository.findAll()
                .stream()
                .map(this::toWorkLocationDto)
                .collect(Collectors.toList()));

        // Populate SubWorkLocation master data lists
        metaEmploymentDTO.setSubWorkLocationList(subWorkLocationRepository.findAll()
                .stream()
                .map(this::toSubWorkLocationDto)
                .collect(Collectors.toList()));

        // Populate Branch master data lists
        metaEmploymentDTO.setBranchDetailsList(branchDetailsRepository.getAll()
                .stream()
                .map(this::toBranchDto)
                .collect(Collectors.toList()));

        // Populate Company master data lists
        metaEmploymentDTO.setCompanyDetailsList(companyDetailsRepository.getAll()
                .stream()
                .map(this::toCompanyDto)
                .collect(Collectors.toList()));

        return metaEmploymentDTO;
    }



    // Conversion of Designation entity to DTO
    private DesignationDto toDesignationDto(Designation designation) {
        if (designation == null) {
            return null;
        }
        return DesignationDto.builder()
                .id(designation.getId())
                .name(designation.getName())
                .code(designation.getCode())
                .build();

    }

    // Conversion of Department entity to DTO
    private DepartmentDto toDepartmentDto(Department department) {
        if (department == null) {
            return null;
        }
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();

    }

    // Conversion of Category entity to DTO
    private CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .build();

    }

    // Conversion of Grade entity to DTO
    private GradeDto toGradeDto(Grade grade) {
        if (grade == null) {
            return null;
        }
        return GradeDto.builder()
                .id(grade.getId())
                .name(grade.getName())
                .code(grade.getCode())
                .build();

    }

    // Conversion of WorkLocation entity to DTO
    private WorkLocationDto toWorkLocationDto(WorkLocation workLocation) {
        if (workLocation == null) {
            return null;
        }
        return WorkLocationDto.builder()
                .id(workLocation.getId())
                .name(workLocation.getName())
                .code(workLocation.getCode())
                .build();

    }

    // Conversion of SubWorkLocation entity to DTO
    private SubWorkLocationDto toSubWorkLocationDto(SubWorkLocation subWorkLocation) {
        if (subWorkLocation == null) {
            return null;
        }
        WorkLocationDto workLocationDto = null;
        if (!ValidateUtils.isNullObject(subWorkLocation.getWorkLocation())) {
            workLocationDto = WorkLocationDto.builder()
                    .id(subWorkLocation.getWorkLocation().getId())
                    .name(subWorkLocation.getWorkLocation().getName())
                    .code(subWorkLocation.getWorkLocation().getCode())
                    .build();
        }
        return SubWorkLocationDto.builder()
                .id(subWorkLocation.getId())
                .name(subWorkLocation.getName())
                .code(subWorkLocation.getCode())
                .workLocation(workLocationDto)
                .build();
    }

    // Conversion of Branch Details projection to DTO
    private BranchDetailsDto toBranchDto(BranchDetailsBean branchDetailsBean) {
        return BranchDetailsDto.builder()
                .id(branchDetailsBean.getId())
                .name(branchDetailsBean.getName())
                .code(branchDetailsBean.getCode())
                .address(branchDetailsBean.getAddress())
                .countryId(branchDetailsBean.getCountryId())
                .countryName(branchDetailsBean.getCountryName())
                .stateId(branchDetailsBean.getStateId())
                .stateName(branchDetailsBean.getStateName())
                .cityId(branchDetailsBean.getCityId())
                .cityName(branchDetailsBean.getCityName()).build();
    }

    // Conversion of Company projection to DTO
    private CompanyDetailsDto toCompanyDto(CompanyDetailsBean companyDetailsBean) {
        DesignationDto contactPersonDesignationDto = null;
        if (!ValidateUtils.isNullObject(companyDetailsBean) && !ValidateUtils.isNullObject(companyDetailsBean.getDesignationId())) {
            contactPersonDesignationDto = DesignationDto.builder()
                    .id(companyDetailsBean.getDesignationId())
                    .name(companyDetailsBean.getDesignationName())
                    .code(companyDetailsBean.getDesignationCode())
                    .build();
        }

        return CompanyDetailsDto.builder()
                .id(companyDetailsBean.getId())
                .name(companyDetailsBean.getName())
                .code(companyDetailsBean.getCode())
                .registeredName(companyDetailsBean.getRegisteredName())
                .registeredNumber(companyDetailsBean.getRegisteredNumber())
                .incorporationDate(DateHelper.formatDateObjectToUserDateView(companyDetailsBean.getIncorporationDate()))
                .panNumber(companyDetailsBean.getPanNumber())
                .tanNumber(companyDetailsBean.getTanNumber())
                .cinNumber(companyDetailsBean.getCinNumber())
                .gstin(companyDetailsBean.getGstin())
                .ptRegNumber(companyDetailsBean.getPtRegNumber())
                .pfRegistrationNumber(companyDetailsBean.getPfRegistrationNumber())
                .esicRegNumber(companyDetailsBean.getEsicRegNumber())
                .msmeRegNo(companyDetailsBean.getMsmeRegNo())
                .iecCode(companyDetailsBean.getIecCode())
                .ptZone(companyDetailsBean.getPtZone())
                .pfZone(companyDetailsBean.getPfZone())
                .esicZone(companyDetailsBean.getEsicZone())
                .industryType(companyDetailsBean.getIndustryType())
                .address(companyDetailsBean.getAddress())
                .countryName(companyDetailsBean.getCountryName())
                .stateId(companyDetailsBean.getStateId())
                .stateName(companyDetailsBean.getStateName())
                .cityId(companyDetailsBean.getCityId())
                .cityName(companyDetailsBean.getCityName())
                .pincode(companyDetailsBean.getPincode())
                .contactPersonName(companyDetailsBean.getContactPersonName())
                .designation(contactPersonDesignationDto)
                .contactPersonEmail(companyDetailsBean.getContactPersonEmail())
                .phone(companyDetailsBean.getPhone())
                .email(companyDetailsBean.getEmail())
                .website(companyDetailsBean.getWebsite())
                .twitter(companyDetailsBean.getTwitter())
                .facebook(companyDetailsBean.getFacebook())
                .instagram(companyDetailsBean.getInstagram())
                .imageUrl(companyDetailsBean.getImageUrl() != null ? serverBaseUrl + companyDetailsBean.getImageUrl() : null)
                .build();
    }
}
