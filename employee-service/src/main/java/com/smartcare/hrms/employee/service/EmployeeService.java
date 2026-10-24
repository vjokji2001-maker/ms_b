package com.smartcare.hrms.employee.service;

import com.smartcare.hrms.common.utility.Constants;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.employee.config.MessageResource;
import com.smartcare.hrms.employee.converter.EmployeeConverter;
import com.smartcare.hrms.employee.dao.EmployeeDashboardDAO;
import com.smartcare.hrms.employee.dto.*;
import com.smartcare.hrms.common.bean.employee.EmployeeBean;
import com.smartcare.hrms.common.bean.employee.EmployeeNameCodeBean;
import com.smartcare.hrms.common.dto.master.employment.*;
import com.smartcare.hrms.common.entity.employee.*;
import com.smartcare.hrms.common.entity.master.employment.*;
import com.smartcare.hrms.common.enumeration.*;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.employment.*;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.employee.dto.external.UserMDTO;
import com.smartcare.hrms.employee.service.external.SmartCareIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService {

	@Autowired
	private MessageResource messageResource;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private CompanyDetailsRepository companyDetailsRepository;

	@Autowired
	private BranchDetailsRepository branchDetailsRepository;

	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private WorkLocationRepository workLocationRepository;

	@Autowired
	private SubWorkLocationRepository subWorkLocationRepository;

	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private EmployeeDashboardDAO employeeDashboardDAO;

    @Value("${file.upload.dir}")
    private String baseUploadDir;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SmartCareIntegrationService smartCareIntegrationService;


	/**
	 *
	 * @param searchDTO
	 * @return Paginated response details
	 */
	public PaginatedDataResponse<EmployeeDashboardDTO> getPaginatedEmployeeList(EmployeeSearchDTO searchDTO) {
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
		return employeeDashboardDAO.getData(searchDTO);
	}

    /**
     * Creates a new Employee entity based on the provided DTO and associates it with a clinic.
     *
     * @param requestDto the EmployeeDTO containing employee details
     * @param clinicId the ID of the clinic to associate with the employee
     */
	@Transactional
	public void create(EmployeeDTO requestDto, String clinicId) {
		/* Validate employee request */
		validateRequest(requestDto);
        validateByRepository(requestDto);

		// Convert DTO to entity WITHOUT file info
		Employee entity = EmployeeConverter.toEntity(requestDto);
		entity.setAadhaarFileName(null);
		entity.setPanFileName(null);

        // Employee experience files
        if (entity.getEmployeeExperiences() != null){
            for (EmployeeExperience employeeExperience : entity.getEmployeeExperiences()){
                employeeExperience.setSalarySlipsName(null);
                employeeExperience.setExperienceCertificateName(null);
                employeeExperience.setRelievingCertificateName(null);
            }
        }
        // Employee education files
        if (entity.getEmployeeEducations() != null){
            for (EmployeeEducation employeeEducation : entity.getEmployeeEducations()){
                employeeEducation.setDocumentName(null);
            }
        }
        // Employee skills files
        if (entity.getEmployeeSkills() != null){
             for (EmployeeSkills employeeSkills : entity.getEmployeeSkills()){
                 employeeSkills.setDocumentName(null);
             }
        }

		// Save entity to generate ID
		Employee savedEntity = employeeRepository.save(entity);
		UUID employeeId = savedEntity.getId();

		// Store Aadhar document file
        if (requestDto.getAadhaarFileName() != null){
            String aadharFileName = storeBase64File(requestDto.getAadhaarFileName(), employeeId, clinicId);
            savedEntity.setAadhaarFileName(aadharFileName);
        }
        // Store Pan document file
        if (requestDto.getPanFileName() != null){
            String panFileName = storeBase64File(requestDto.getPanFileName(), employeeId, clinicId);
            savedEntity.setPanFileName(panFileName);
        }
        // handle employee experience list files
        if (requestDto.getEmployeeExperienceDTOList() != null && savedEntity.getEmployeeExperiences() != null){

            // Iterating over the list using index i, it will work only, if DTO list and ENTITY list matches
           for (int i = 0; i < requestDto.getEmployeeExperienceDTOList().size(); i++){

               EmployeeExperienceDTO employeeExperienceDTO = requestDto.getEmployeeExperienceDTOList().get(i);
               EmployeeExperience employeeExperience = savedEntity.getEmployeeExperiences().get(i);

               if (employeeExperienceDTO.getSalarySlipsName() != null){
                   String salarySlipName = storeBase64File(employeeExperienceDTO.getSalarySlipsName(), employeeId, clinicId);
                   String experienceCertificateName = storeBase64File(employeeExperienceDTO.getExperienceCertificateName(), employeeId, clinicId);
                   String relievingCertificateName = storeBase64File(employeeExperienceDTO.getRelievingCertificateName(), employeeId, clinicId);
                   employeeExperience.setSalarySlipsName(salarySlipName);
                   employeeExperience.setExperienceCertificateName(experienceCertificateName);
                   employeeExperience.setRelievingCertificateName(relievingCertificateName);
               }
           }
        }
        // handle employee education list files
        if (requestDto.getEmployeeEducationDTOsList() != null && savedEntity.getEmployeeEducations() != null){

            // Iterating over the list using index i, it will work only, if DTO list and ENTITY list matches
            for (int i = 0; i < requestDto.getEmployeeEducationDTOsList().size(); i++){

                EmployeeEducationDTO employeeEducationDTO = requestDto.getEmployeeEducationDTOsList().get(i);
                EmployeeEducation employeeEducation = savedEntity.getEmployeeEducations().get(i);

                if (employeeEducationDTO.getDocumentName() != null){
                    String documentName = storeBase64File(employeeEducationDTO.getDocumentName(), employeeId, clinicId);
                    employeeEducation.setDocumentName(documentName);
                }
            }
        }
        // handle employee skills list files
        if (requestDto.getEmployeeSkillsDTOsList() != null && savedEntity.getEmployeeSkills() != null){

            // Iterating over the list using index i, it will work only, if DTO list and ENTITY list matches
            for (int i = 0; i < requestDto.getEmployeeSkillsDTOsList().size(); i++){

                EmployeeSkillsDTO employeeSkillsDTO = requestDto.getEmployeeSkillsDTOsList().get(i);
                EmployeeSkills employeeSkills = savedEntity.getEmployeeSkills().get(i);

                if (employeeSkillsDTO.getDocumentName() != null){
                    String documentName = storeBase64File(employeeSkillsDTO.getDocumentName(), employeeId, clinicId);
                    employeeSkills.setDocumentName(documentName);
                }
            }
        }

        // Save again with file info
		employeeRepository.save(savedEntity);

        // Prepare SmartCare request
        UserMDTO userDto = toUserMDTO(requestDto, employeeId);
        Map<String, String> dynamicHeaders = new HashMap<>();
        dynamicHeaders.put("zoneid", Constants.INDIAN_ZONEID); // Asia/Kolkata
        dynamicHeaders.put("clinicid", clinicId + Constants.HRMS_CLINIC_SUFFIX);// clinicName+_hrms
//        dynamicHeaders.put("clinicid", "elitemedicity");

        // Call SmartCare (may throw exception rollback)
        smartCareIntegrationService.createUserInSmartcare(userDto, dynamicHeaders);

        // If success set flag true
        savedEntity.setSmartCareUserCreated(true);
        employeeRepository.save(savedEntity);
	}

    /**
     * Updates an existing Employee entity using the provided DTO and clinic ID.
     *
     * @param requestDto the EmployeeDTO containing updated employee details
     * @param clinicId the ID of the clinic associated with the employee
     */
	@Transactional
	public void update(EmployeeDTO requestDto, String clinicId) {
		if (ValidateUtils.isNullObject(requestDto.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
		}
		validateRequest(requestDto);
		Optional<Employee> employeeOptional = employeeRepository.findById(requestDto.getId());
		if (employeeOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("record.not.found"));
		}
		Employee employee = employeeOptional.get();
        if (!employee.getEmpCode().equals(requestDto.getEmpCode())){
            validateByRepository(requestDto);
        }

		/** update entity with dto **/
		convertForUpdate(employee, requestDto, clinicId);

        UUID employeeId = employee.getId();

        /* FILE HANDLING */

        // Aadhar
        employee.setAadhaarFileName(handleFileUpdate(employee.getAadhaarFileName(), requestDto.getAadhaarFileName(), employeeId, clinicId));

        // PAN
        employee.setPanFileName(handleFileUpdate(employee.getPanFileName(), requestDto.getPanFileName(), employeeId, clinicId));

        // Experience docs
        if (requestDto.getEmployeeExperienceDTOList() != null) {
            Map<Long, EmployeeExperienceDTO> dtoMap = requestDto.getEmployeeExperienceDTOList().stream()
                    .filter(dto -> dto.getId() != null)
                    .collect(Collectors.toMap(EmployeeExperienceDTO::getId, dto -> dto));

            // Remove orphaned experiences and delete old files
            employee.getEmployeeExperiences().removeIf(exp -> {
                if (!dtoMap.containsKey(exp.getId())) {
                    deleteOldFileIfExists(exp.getSalarySlipsName(), employeeId, clinicId);
                    deleteOldFileIfExists(exp.getExperienceCertificateName(), employeeId, clinicId);
                    deleteOldFileIfExists(exp.getRelievingCertificateName(), employeeId, clinicId);
                    return true;
                }
                return false;
            });

            // Update existing or add new
            for (EmployeeExperienceDTO dto : requestDto.getEmployeeExperienceDTOList()) {
                EmployeeExperience exp = employee.getEmployeeExperiences().stream()
                        .filter(e -> e.getId() != null && e.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null);

                if (exp != null) {
                    // Existing update files
                    exp.setSalarySlipsName(handleFileUpdate(exp.getSalarySlipsName(), dto.getSalarySlipsName(), employeeId, clinicId));
                    exp.setExperienceCertificateName(handleFileUpdate(exp.getExperienceCertificateName(), dto.getExperienceCertificateName(), employeeId, clinicId));
                    exp.setRelievingCertificateName(handleFileUpdate(exp.getRelievingCertificateName(), dto.getRelievingCertificateName(), employeeId, clinicId));
                } else {
                    // New create entity
                    EmployeeExperience newExp = EmployeeConverter.toEmployeeExperienceEntity(dto, employee);
                    newExp.setSalarySlipsName(handleFileUpdate(null, dto.getSalarySlipsName(), employeeId, clinicId));
                    newExp.setExperienceCertificateName(handleFileUpdate(null, dto.getExperienceCertificateName(), employeeId, clinicId));
                    newExp.setRelievingCertificateName(handleFileUpdate(null, dto.getRelievingCertificateName(), employeeId, clinicId));
                    employee.getEmployeeExperiences().add(newExp);
                }
            }
        }

        // Education docs
        if (requestDto.getEmployeeEducationDTOsList() != null) {
            Map<Long, EmployeeEducationDTO> dtoMap = requestDto.getEmployeeEducationDTOsList().stream()
                    .filter(dto -> dto.getId() != null)
                    .collect(Collectors.toMap(EmployeeEducationDTO::getId, dto -> dto));

            employee.getEmployeeEducations().removeIf(edu -> {
                if (!dtoMap.containsKey(edu.getId())) {
                    deleteOldFileIfExists(edu.getDocumentName(), employeeId, clinicId);
                    return true;
                }
                return false;
            });

            for (EmployeeEducationDTO dto : requestDto.getEmployeeEducationDTOsList()) {
                EmployeeEducation edu = employee.getEmployeeEducations().stream()
                        .filter(e -> e.getId() != null && e.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null);

                if (edu != null) {
                    edu.setDocumentName(handleFileUpdate(edu.getDocumentName(), dto.getDocumentName(), employeeId, clinicId));
                } else {
                    EmployeeEducation newEdu = EmployeeConverter.toEmployeeEducationEntity(dto, employee);
                    newEdu.setDocumentName(handleFileUpdate(null, dto.getDocumentName(), employeeId, clinicId));
                    employee.getEmployeeEducations().add(newEdu);
                }
            }
        }

        // Skills docs
        if (requestDto.getEmployeeSkillsDTOsList() != null) {
            Map<Long, EmployeeSkillsDTO> dtoMap = requestDto.getEmployeeSkillsDTOsList().stream()
                    .filter(dto -> dto.getId() != null)
                    .collect(Collectors.toMap(EmployeeSkillsDTO::getId, dto -> dto));

            employee.getEmployeeSkills().removeIf(skill -> {
                if (!dtoMap.containsKey(skill.getId())) {
                    deleteOldFileIfExists(skill.getDocumentName(), employeeId, clinicId);
                    return true;
                }
                return false;
            });

            for (EmployeeSkillsDTO dto : requestDto.getEmployeeSkillsDTOsList()) {
                EmployeeSkills skill = employee.getEmployeeSkills().stream()
                        .filter(s -> s.getId() != null && s.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null);

                if (skill != null) {
                    skill.setDocumentName(handleFileUpdate(skill.getDocumentName(), dto.getDocumentName(), employeeId, clinicId));
                } else {
                    EmployeeSkills newSkill = EmployeeConverter.toEmployeeSkillEntity(dto, employee);
                    newSkill.setDocumentName(handleFileUpdate(null, dto.getDocumentName(), employeeId, clinicId));
                    employee.getEmployeeSkills().add(newSkill);
                }
            }
        }
        employeeRepository.save(employee);
	}

    /**
     * Deletes the Employee entity identified by the given ID and clinic ID.
     *
     * @param id the UUID of the Employee to delete
     * @param clinicId the ID of the clinic associated with the employee
     * @throws ValidationException if the employee is not found or deletion fails
     */
    @Transactional
	public void delete(UUID id, String clinicId) {
		Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("record.not.found")));

		employeeRepository.delete(employee);

        //  Build folder path dynamically
        Path folderPath = Paths.get(baseUploadDir, clinicId, "HR", "Employee", employee.getId().toString());

        // Delete folder and files
        try {
            if (Files.exists(folderPath)) {
                Files.walk(folderPath)
                        .sorted(Comparator.reverseOrder()) // delete files first, then folders
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.error("Failed to delete file/folder: {}", path, e);
                            }
                        });
                log.info("Deleted employee folder: {}", folderPath);
            }
        } catch (IOException e) {
            log.error("Error deleting employee folder {}", folderPath, e);
        }
	}

    /**
     * Retrieves an EmployeeDTO by its ID.
     *
     * @param id the ID of the Employee to retrieve; must not be empty
     * @return the EmployeeDTO if found
     */
	public EmployeeDTO getById(String id) {
        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("record.not.found"));
        }
        List<EmployeeBean> employeeBeans = employeeRepository.getAllById(id);
        if (CollectionUtils.isEmpty(employeeBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("record.not.found"));
        }
        return convertBeanToDto(employeeBeans);
	}

    /**
     * Retrieves a list of employees with their names and codes.
     * Can filter results based on criteria provided in the DTO.
     *
     * @param employeeNameCodeDTO DTO containing optional filter criteria
     * @return list of EmployeeNameCodeDTO matching the criteria
     */
    public List<EmployeeNameCodeDTO> getEmployeeNameAndCodeList(EmployeeNameCodeDTO employeeNameCodeDTO){
        List<EmployeeNameCodeDTO> nameCodeDTOList = new ArrayList<>();
        if (!ValidateUtils.isStringEmpty(employeeNameCodeDTO.getFormalName())){
            List<EmployeeNameCodeBean> byFormalName = employeeRepository.getByEmployeeName(employeeNameCodeDTO.getFormalName());
            if (!CollectionUtils.isEmpty(byFormalName)){
                for(EmployeeNameCodeBean employeeNameCodeBean : byFormalName){
                    nameCodeDTOList.add(convertNameCodeBeanToDto(employeeNameCodeBean));
                }
            }
        } else if (!ValidateUtils.isStringEmpty(employeeNameCodeDTO.getEmpCode())) {
            EmployeeNameCodeBean byEmpCode = employeeRepository.getByEmployeeCode(employeeNameCodeDTO.getEmpCode());
            if (!ValidateUtils.isNullObject(byEmpCode)){
                nameCodeDTOList.add(convertNameCodeBeanToDto(byEmpCode));

            }
        }
        else {
            throw new ValidationException(messageResource.getEnglishMessage("record.not.found"));
        }

        return nameCodeDTOList;
    }



	private void validateRequest(EmployeeDTO requestDto) {
		if (ValidateUtils.isNullObject(requestDto)) {
			throw new ValidationException(messageResource.getEnglishMessage("employee.request.null"));
		} 
		if (ValidateUtils.isStringEmpty(requestDto.getFirstName())) {
			throw new ValidationException(messageResource.getEnglishMessage("first.name.required"));
		}
		if (requestDto.getFirstName().length() > 50) {
			throw new ValidationException(messageResource.getEnglishMessage("first.name.too.long"));
		}
//		if (ValidateUtils.isStringEmpty(requestDto.getMiddleName())) {
//			throw new ValidationException(messageResource.getEnglishMessage("middle.name.required"));
//		}
//		if (requestDto.getMiddleName().length() > 50) {
//			throw new ValidationException(messageResource.getEnglishMessage("middle.name.too.long"));
//		}
//		if (ValidateUtils.isStringEmpty(requestDto.getLastName())) {
//			throw new ValidationException(messageResource.getEnglishMessage("last.name.required"));
//		}
//		if (requestDto.getLastName().length() > 50) {
//			throw new ValidationException(messageResource.getEnglishMessage("last.name.too.long"));
//		}
        if(ValidateUtils.isNullObject(requestDto.getGender())){
            throw new ValidationException(messageResource.getEnglishMessage("gender.required"));
        }
		if (ValidateUtils.isStringEmpty(requestDto.getDob())) {
			throw new ValidationException(messageResource.getEnglishMessage("date.of.birth.null"));
		}
        if (ValidateUtils.isNullObject(requestDto.getMaritalStatus())){
            throw new ValidationException(messageResource.getEnglishMessage("marital.status.required"));
        }
		if (ValidateUtils.isStringEmpty(requestDto.getContactNumber())) {
			throw new ValidationException(messageResource.getEnglishMessage("contact.number.required"));
		}
		if (requestDto.getContactNumber().length() > 10) {
			throw new ValidationException(messageResource.getEnglishMessage("contact.number.too.long"));
		}
//		if (requestDto.getAlternateNumber() != null  && requestDto.getAlternateNumber().trim().length() > 10) {
//			throw new ValidationException(messageResource.getEnglishMessage("alternate.number.too.long"));
//		}
//        if (ValidateUtils.isStringEmpty(requestDto.getFormalEmail())){
//            throw new ValidationException(messageResource.getEnglishMessage("formal.email.required"));
//        }
//		if (requestDto.getFormalEmail().trim().length() > 100) {
//			throw new ValidationException(messageResource.getEnglishMessage("formal.email.too.long"));
//		}
//        if (!ValidateUtils.isValidEmailFormat(requestDto.getFormalEmail())){
//            throw new ValidationException(messageResource.getEnglishMessage("invalid.formal.email.format"));
//        }
//		if (ValidateUtils.isStringEmpty(requestDto.getPersonalEmail())) {
//			throw new ValidationException(messageResource.getEnglishMessage("personal.email.required"));
//		}
//		if (requestDto.getPersonalEmail().length() > 100) {
//			throw new ValidationException(messageResource.getEnglishMessage("personal.email.too.long"));
//		}
//        if (!ValidateUtils.isValidEmailFormat(requestDto.getPersonalEmail())){
//            throw new ValidationException(messageResource.getEnglishMessage("invalid.personal.email.format"));
//        }
		if (ValidateUtils.isStringEmpty(requestDto.getAadhaarNumber())) {
			throw new ValidationException(messageResource.getEnglishMessage("aadhaar.number.required"));
		}
		if (requestDto.getAadhaarNumber().length() > 13) {
			throw new ValidationException(messageResource.getEnglishMessage("aadhaar.number.too.long"));
		}
//		if (ValidateUtils.isStringEmpty(requestDto.getPanNumber())) {
//			throw new ValidationException(messageResource.getEnglishMessage("pan.number.required"));
//		}
//		if (requestDto.getPanNumber().length() > 10) {
//			throw new ValidationException(messageResource.getEnglishMessage("pan.number.too.long"));
//		}
        // NEW: Document validations - Mandatory documents
//        if (ValidateUtils.isStringEmpty(requestDto.getAadhaarFileName())) {
//            throw new ValidationException(messageResource.getEnglishMessage("aadhaar.document.required"));
//        }
//        if (ValidateUtils.isStringEmpty(requestDto.getPanFileName())) {
//            throw new ValidationException(messageResource.getEnglishMessage("pan.document.required"));
//        }
		if (ValidateUtils.isStringEmpty(requestDto.getNationality())) {
			throw new ValidationException(messageResource.getEnglishMessage("nationality.required"));
		}
        if (ValidateUtils.isZero(requestDto.getBirthCountryId())){
            throw new ValidationException(messageResource.getEnglishMessage("birth.country.required"));
        }
        if (ValidateUtils.isZero(requestDto.getStateId())){
            throw new ValidationException(messageResource.getEnglishMessage("state.required"));
        }
        if (ValidateUtils.isZero(requestDto.getCityId())){
            throw new ValidationException(messageResource.getEnglishMessage("city.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getCompanyDetails().getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("company.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getBranchDetails().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("branch.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getDesignation().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("designation.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getDepartment().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("department.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getWorkLocation().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("work.location.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getGrade().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("grade.required"));
        }
        if (ValidateUtils.isLongZero(requestDto.getCategory().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("category.required"));
        }
        if (ValidateUtils.isNullObject(requestDto.getStatus())){
            throw new ValidationException(messageResource.getEnglishMessage("employee.status.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getEmpCode())){
            throw new ValidationException(messageResource.getEnglishMessage("employee.code.required"));
        }
		if (requestDto.getEmpCode().length() > 50) {
			throw new ValidationException(messageResource.getEnglishMessage("employee.code.too.long"));
		}
		if (requestDto.getBiometricCode() != null  &&  requestDto.getBiometricCode().length() > 50) {
			throw new ValidationException(messageResource.getEnglishMessage("biometric.code.too.long"));
		}
		if (ValidateUtils.isStringEmpty(requestDto.getJoiningDate())) {
			throw new ValidationException(messageResource.getEnglishMessage("joining.date.null"));
		}
		if (ValidateUtils.isStringEmpty(requestDto.getContractStartDate())) {
			throw new ValidationException(messageResource.getEnglishMessage("contract.start.date.null"));
		}
//		if (ValidateUtils.isStringEmpty(requestDto.getProbationStartDate())) {
//			throw new ValidationException(messageResource.getEnglishMessage("probation.start.date.null"));
//		}
        // Employee Address details
//        if (CollectionUtils.isEmpty(requestDto.getEmployeeAddressDTOList())){
//            throw new ValidationException(messageResource.getEnglishMessage("address.details.required"));
//        }
//        for(EmployeeAddressDTO employeeAddressDTO : requestDto.getEmployeeAddressDTOList()){
//            AddressType addressType = EnumConverter.toEnum(employeeAddressDTO.getAddressType(), AddressType.class);
//            switch (addressType) {
//                case CURRENT -> validateCurrentAddress(employeeAddressDTO);
//                case PAST -> validatePastAddress(employeeAddressDTO);
//                default -> throw new ValidationException(messageResource.getEnglishMessage("invalid.address.type"));
//            }
//        }
        // Employee Family details
//        if (CollectionUtils.isEmpty(requestDto.getEmployeeFamilyDetailsDTOList())){
//            throw new ValidationException(messageResource.getEnglishMessage("family.details.required"));
//        }
//        for(EmployeeFamilyDetailsDTO familyDetailsDTO : requestDto.getEmployeeFamilyDetailsDTOList()){
//            if (ValidateUtils.isStringEmpty(familyDetailsDTO.getName())){
//                throw new ValidationException(messageResource.getEnglishMessage("family.relation.name.required"));
//            }
//            if (ValidateUtils.isStringEmpty(familyDetailsDTO.getRelation())){
//                throw new ValidationException(messageResource.getEnglishMessage("family.relation.required"));
//            }
//            if (ValidateUtils.isStringEmpty(familyDetailsDTO.getContactNumber())){
//                throw new ValidationException(messageResource.getEnglishMessage("family.relation.contact.required"));
//            }
//        }
        // Employee Education details
//        if (CollectionUtils.isEmpty(requestDto.getEmployeeEducationDTOsList())){
//            throw new ValidationException(messageResource.getEnglishMessage("education.details.required"));
//        }
//        for (EmployeeEducationDTO eduDto : requestDto.getEmployeeEducationDTOsList()) {
//            if (ValidateUtils.isNullObject(eduDto.getEducationType())){
//                throw new ValidationException(messageResource.getEnglishMessage("education.type.required"));
//            }
//            if (ValidateUtils.isStringEmpty(eduDto.getInstituteUniversityName())){
//                throw new ValidationException(messageResource.getEnglishMessage("institute.or.university.name.required"));
//            }
//            if (ValidateUtils.isStringEmpty(eduDto.getSchoolCollegeName())){
//               throw new ValidationException(messageResource.getEnglishMessage("school.or.college.name.required"));
//            }
//            if (ValidateUtils.isStringEmpty(eduDto.getFromMonthYear())){
//               throw new ValidationException(messageResource.getEnglishMessage("education.from.month.year.required"));
//            }
//            if (ValidateUtils.isStringEmpty(eduDto.getToMonthYear())){
//                throw new ValidationException(messageResource.getEnglishMessage("education.to.month.year.required"));
//            }
//            if (ValidateUtils.isStringEmpty(eduDto.getDocumentName())) {
//                throw new ValidationException(messageResource.getEnglishMessage("education.document.required"));
//            }
//        }
        // Employee Experience details
//        if (CollectionUtils.isEmpty(requestDto.getEmployeeExperienceDTOList())){
//            throw new ValidationException(messageResource.getEnglishMessage("experience.details.required"));
//        }
//        for (EmployeeExperienceDTO experienceDTO : requestDto.getEmployeeExperienceDTOList()){
//            if (ValidateUtils.isNullObject(experienceDTO.getExperienceType())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.type.required"));
//            }
//            if (ValidateUtils.isNullObject(experienceDTO.getCompanyName())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.company.name.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getDesignation())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.designation.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getFromDate())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.from.date.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getToDate())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.to.date.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getFromMonthYear())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.from.month.year.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getToMonthYear())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.to.month.year.required"));
//            }
//            if (ValidateUtils.isStringEmpty(experienceDTO.getExperienceCertificateName())){
//                throw new ValidationException(messageResource.getEnglishMessage("experience.certificate.required"));
//            }
//        }
        // Employee Skills details
//        if (CollectionUtils.isEmpty(requestDto.getEmployeeSkillsDTOsList())){
//            throw new ValidationException(messageResource.getEnglishMessage("skills.details.required"));
//        }
//        for (EmployeeSkillsDTO skillsDTO : requestDto.getEmployeeSkillsDTOsList()){
//            if (ValidateUtils.isStringEmpty(skillsDTO.getSkill())){
//              throw new ValidationException(messageResource.getEnglishMessage("skills.name.required"));
//            }
//            if (ValidateUtils.isStringEmpty(skillsDTO.getFromMonthYear())){
//                throw new ValidationException(messageResource.getEnglishMessage("skills.from.month.year.required"));
//            }
//        }
        // Employee Statutory details
//        if (ValidateUtils.isNullObject(requestDto.getEmployeeStatutoryInfoDTO())){
//            throw new ValidationException(messageResource.getEnglishMessage("statutory.details.required"));
//        }
//        if (ValidateUtils.isNullObject(requestDto.getEmployeeStatutoryInfoDTO().getPfApplicable())){
//            throw new ValidationException(messageResource.getEnglishMessage("pf.applicable.required"));
//        }
//        if (ValidateUtils.isNullObject(requestDto.getEmployeeStatutoryInfoDTO().getEsicApplicable())){
//            throw new ValidationException(messageResource.getEnglishMessage("esic.applicable.required"));
//        }
//        if (ValidateUtils.isNullObject(requestDto.getEmployeeStatutoryInfoDTO().getNpsApplicable())){
//            throw new ValidationException(messageResource.getEnglishMessage("nps.applicable.required"));
//        }
//        if (ValidateUtils.isNullObject(requestDto.getEmployeeStatutoryInfoDTO().getPaymentMethod())){
//            throw new ValidationException(messageResource.getEnglishMessage("payment.method.required"));
//        }
	}

    private void validateCurrentAddress(EmployeeAddressDTO dto) {
        if (ValidateUtils.isZero(dto.getCountryId())) {
            throw new ValidationException(messageResource.getEnglishMessage("country.required"));
        }
        if (ValidateUtils.isZero(dto.getStateId())) {
            throw new ValidationException(messageResource.getEnglishMessage("state.required"));
        }
        if (ValidateUtils.isZero(dto.getCityId())) {
            throw new ValidationException(messageResource.getEnglishMessage("city.required"));
        }
        if (ValidateUtils.isStringEmpty(dto.getAddress())) {
            throw new ValidationException(messageResource.getEnglishMessage("address.required"));
        }
        if (ValidateUtils.isStringEmpty(dto.getPinCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("pin.code.required"));
        }
    }

//    private void validatePastAddress(EmployeeAddressDTO dto) {
//        if (ValidateUtils.isStringEmpty(dto.getFromDate())) {
//            throw new ValidationException(messageResource.getEnglishMessage("from.date.required"));
//        }
//        if (ValidateUtils.isStringEmpty(dto.getToDate())) {
//            throw new ValidationException(messageResource.getEnglishMessage("to.date.required"));
//        }
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        LocalDate fromDate = LocalDate.parse(dto.getFromDate(), formatter);
//        LocalDate toDate   = LocalDate.parse(dto.getToDate(), formatter);
//        if (fromDate.isAfter(toDate)) {
//            throw new ValidationException(messageResource.getEnglishMessage("from.date.after.to.date"));
//        }
//    }


    private void validateByRepository(EmployeeDTO employeeDTO){
        Employee byEmpCode = employeeRepository.findByEmpCode(employeeDTO.getEmpCode());
        if (!ValidateUtils.isNullObject(byEmpCode)){
            throw new ValidationException(messageResource.getEnglishMessage("employee.code.exists"));
        }
    }


	private void convertForUpdate(Employee entity, EmployeeDTO dto,  String clinicId ) {

        UUID employeeId = entity.getId();

		StringBuilder formalName = new StringBuilder();
		if (dto.getFirstName() != null)
			formalName.append(dto.getFirstName()).append(" ");
		if (dto.getMiddleName() != null)
			formalName.append(dto.getMiddleName()).append(" ");
		if (dto.getLastName() != null)
			formalName.append(dto.getLastName());
		// Basic Personal Details
		entity.setSalutation(EnumConverter.toEnum(dto.getSalutation(), Salutation.class));
		entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
		entity.setLastName(dto.getLastName());
		entity.setFormalName(formalName.toString().trim());
		entity.setGender(EnumConverter.toEnum(dto.getGender(), Gender.class));
		entity.setDob(DateHelper.convertUserDateToLocalDate(dto.getDob()));
		entity.setMaritalStatus(EnumConverter.toEnum(dto.getMaritalStatus(), MaritalStatus.class));
		entity.setMarriedSince(DateHelper.convertUserDateToLocalDate(dto.getMarriedSince()));
		// Contact Details
        entity.setContactCountryCode(dto.getContactCountryCode());
		entity.setContactNumber(dto.getContactNumber());
        entity.setAlternateCountryCode(dto.getAlternateCountryCode());
		entity.setAlternateNumber(dto.getAlternateNumber());
		entity.setFormalEmail(dto.getFormalEmail());
		entity.setPersonalEmail(dto.getPersonalEmail());
		// Identity Details
		entity.setAadhaarNumber(dto.getAadhaarNumber());
        entity.setDocumentCountry(dto.getDocumentCountry());
		entity.setPanNumber(dto.getPanNumber());
        // Location / Nationality
		entity.setNationality(dto.getNationality());
		entity.setBirthCountryId(dto.getBirthCountryId());
		entity.setStateId(dto.getStateId());
		entity.setCityId(dto.getCityId());
		entity.setBloodGroup(EnumConverter.toEnum(dto.getBloodGroup(), BloodGroup.class));
		// Employment Details
		if (dto.getCompanyDetails() != null && dto.getCompanyDetails().getId() != null) {
			CompanyDetails companyDetails = companyDetailsRepository.findById(dto.getCompanyDetails().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("company.not.found")));
			entity.setCompany(companyDetails);
		}
		if (dto.getBranchDetails() != null && dto.getBranchDetails().getId() != null) {
			BranchDetails branchDetails = branchDetailsRepository.findById(dto.getBranchDetails().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("branch.not.found")));
			entity.setBranch(branchDetails);
		}
		if (dto.getDesignation() != null && dto.getDesignation().getId() != null) {
			Designation designation = designationRepository.findById(dto.getDesignation().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("designation.not.found")));
			entity.setDesignation(designation);
		}
		if (dto.getDepartment() != null && dto.getDepartment().getId() != null) {
			Department department = departmentRepository.findById(dto.getDepartment().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("department.not.found")));
			entity.setDepartment(department);
		}
		if (dto.getWorkLocation() != null && dto.getWorkLocation().getId() != null) {
			WorkLocation workLocation = workLocationRepository.findById(dto.getWorkLocation().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("work.location.not.found")));
			entity.setWorkLocation(workLocation);
		}
		if (dto.getSubWorkLocation() != null && dto.getSubWorkLocation().getId() != null) {
			SubWorkLocation subWorkLocation = subWorkLocationRepository.findById(dto.getSubWorkLocation().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("sub.work.location.not.found")));
			entity.setSubWorkLocation(subWorkLocation);
		}
		if (dto.getGrade() != null && dto.getGrade().getId() != null) {
			Grade grade = gradeRepository.findById(dto.getGrade().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("grade.not.found")));
			entity.setGrade(grade);
		}
		if (dto.getCategory() != null && dto.getCategory().getId() != null) {
			Category category = categoryRepository.findById(dto.getCategory().getId())
					.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("category.not.found")));
			entity.setCategory(category);
		}
		entity.setStatus(EnumConverter.toEnum(dto.getStatus(), Status.class));
		entity.setEmpCode(dto.getEmpCode());
		entity.setBiometricCode(dto.getBiometricCode());
		// Pre-Joining Dates
		entity.setOfferDate(DateHelper.convertUserDateToLocalDate(dto.getOfferDate()));
		entity.setOfferAcceptanceDate(DateHelper.convertUserDateToLocalDate(dto.getOfferAcceptanceDate()));
		// Onboarding Dates
		entity.setJoiningDate(DateHelper.convertUserDateToLocalDate(dto.getJoiningDate()));
		entity.setContractStartDate(DateHelper.convertUserDateToLocalDate(dto.getContractStartDate()));
		// Probation & Confirmation
		entity.setProbationStartDate(DateHelper.convertUserDateToLocalDate(dto.getProbationStartDate()));
		entity.setProbationEndDate(DateHelper.convertUserDateToLocalDate(dto.getProbationEndDate()));
		entity.setConfirmationDate(DateHelper.convertUserDateToLocalDate(dto.getConfirmationDate()));
		// Exit Process
		entity.setResignationDate(DateHelper.convertUserDateToLocalDate(dto.getResignationDate()));
		entity.setNoticePeriodStartDate(DateHelper.convertUserDateToLocalDate(dto.getNoticePeriodStartDate()));
		entity.setNoticePeriodEndDate(DateHelper.convertUserDateToLocalDate(dto.getNoticePeriodEndDate()));
		entity.setTerminationDate(DateHelper.convertUserDateToLocalDate(dto.getTerminationDate()));
		entity.setLastWorkingDate(DateHelper.convertUserDateToLocalDate(dto.getLastWorkingDate()));


		/**
		 *  Check if employee address dto collection is empty
		 */
		if (!CollectionUtils.isEmpty(dto.getEmployeeAddressDTOList())) {
			// new employee address entry to be added
			List<EmployeeAddressDTO> addAddress = dto.getEmployeeAddressDTOList().stream()
					.filter(predicate -> predicate.getId() == null).toList();

			// check if employee address collection is empty
			if (!CollectionUtils.isEmpty(entity.getEmployeeAddresses())) {

				// update existing employee address
				List<EmployeeAddressDTO> existingEmployeeAddress = dto.getEmployeeAddressDTOList().stream()
						.filter(predicate -> predicate.getId() != null).toList();
				entity.getEmployeeAddresses().forEach(updateField -> {
					Optional<EmployeeAddressDTO> optional = existingEmployeeAddress.stream()
							.filter(pre -> pre.getId().equals(updateField.getId())).findFirst();
					if (optional.isPresent()) {
						EmployeeAddressDTO employeeAddressDTO = optional.get();
						updateField.setCountryId(employeeAddressDTO.getCountryId());
						updateField.setStateId(employeeAddressDTO.getStateId());
						updateField.setCityId(employeeAddressDTO.getCityId());
						updateField.setAddress(employeeAddressDTO.getAddress());
						updateField.setPinCode(employeeAddressDTO.getPinCode());
                        updateField.setAddressType(EnumConverter.toEnum(employeeAddressDTO.getAddressType(), AddressType.class));
						updateField.setFromDate(DateHelper.convertUserDateToLocalDate(employeeAddressDTO.getFromDate()));
						updateField.setToDate(DateHelper.convertUserDateToLocalDate(employeeAddressDTO.getToDate()));
					} else {
						updateField.setRemove(true);
					}
				});
				// new employee address entry to be added
				if (!CollectionUtils.isEmpty(addAddress)) {
					for (EmployeeAddressDTO address : addAddress) {
						entity.getEmployeeAddresses().add(EmployeeConverter.toEmployeeAddressEntity(address, entity));
					}
				}
				// delete existing address entry
				entity.getEmployeeAddresses().removeIf(EmployeeAddress::isRemove);
			}
		}

		/**
		 * Check if employee family details dto collection is empty
		 */
		if (!CollectionUtils.isEmpty(dto.getEmployeeFamilyDetailsDTOList())) {
			// new employee family details entry to be added
			List<EmployeeFamilyDetailsDTO> addFamilyDetails = dto.getEmployeeFamilyDetailsDTOList().stream()
					.filter(predicate -> predicate.getId() == null).toList();

			// check if employee family details collection is empty
			if (!CollectionUtils.isEmpty(entity.getEmployeeFamilyDetails())) {

				// update existing employee family details
				List<EmployeeFamilyDetailsDTO> existingEmployeeFamilyDetails = dto.getEmployeeFamilyDetailsDTOList()
						.stream().filter(predicate -> predicate.getId() != null).toList();
				entity.getEmployeeFamilyDetails().forEach(updateField -> {
					Optional<EmployeeFamilyDetailsDTO> optional = existingEmployeeFamilyDetails.stream()
							.filter(pre -> pre.getId().equals(updateField.getId())).findFirst();
					if (optional.isPresent()) {
						EmployeeFamilyDetailsDTO employeeFamilyDetailsDTO = optional.get();
						updateField.setName(employeeFamilyDetailsDTO.getName());
						updateField.setRelation(employeeFamilyDetailsDTO.getRelation());
						updateField.setContactNumber(employeeFamilyDetailsDTO.getContactNumber());
						updateField.setDob(DateHelper.convertUserDateToLocalDate(employeeFamilyDetailsDTO.getDob()));
					} else {
						updateField.setRemove(true);
					}
				});
				// new employee family details entry to be added
				if (!CollectionUtils.isEmpty(addFamilyDetails)) {
					for (EmployeeFamilyDetailsDTO familyDetails : addFamilyDetails) {
						entity.getEmployeeFamilyDetails()
								.add(EmployeeConverter.toEmployeeFamilyDetailsEntity(familyDetails, entity));
					}
				}
				// delete existing family details entry
				entity.getEmployeeFamilyDetails().removeIf(EmployeeFamilyDetails::isRemove);
			}

		}

		/**
		 * Check if employee experience dto collection is empty
		 */
		if (!CollectionUtils.isEmpty(dto.getEmployeeExperienceDTOList())) {
			// new employee experience entry to be added
			List<EmployeeExperienceDTO> addExperience = dto.getEmployeeExperienceDTOList().stream()
					.filter(predicate -> predicate.getId() == null).toList();

			// check if employee experience collection is empty
			if (!CollectionUtils.isEmpty(entity.getEmployeeExperiences())) {
				// update existing employee experience

				List<EmployeeExperienceDTO> existingEmployeeExperience = dto.getEmployeeExperienceDTOList().stream()
						.filter(predicate -> predicate.getId() != null).toList();
				entity.getEmployeeExperiences().forEach(updateField -> {
					Optional<EmployeeExperienceDTO> optional = existingEmployeeExperience.stream()
							.filter(pre -> pre.getId().equals(updateField.getId())).findFirst();
					if (optional.isPresent()) {
						EmployeeExperienceDTO employeeExperienceDTO = optional.get();
						updateField.setExperienceType(EnumConverter.toEnum(employeeExperienceDTO.getExperienceType(), ExperienceType.class));
						updateField.setCompanyName(employeeExperienceDTO.getCompanyName());
						updateField.setAddress(employeeExperienceDTO.getAddress());
						updateField.setDesignation(employeeExperienceDTO.getDesignation());

                        LocalDate fromDate = DateHelper.convertUserDateToLocalDate(employeeExperienceDTO.getFromDate());
                        LocalDate toDate = DateHelper.convertUserDateToLocalDate(employeeExperienceDTO.getToDate());
                        updateField.setFromDate(fromDate);
                        updateField.setToDate(toDate);

                        // Calculate and set totalSpan
						updateField.setTotalSpan(DateHelper.calculateTotalSpan(fromDate, toDate));

						updateField.setReferenceName(employeeExperienceDTO.getReferenceName());
						updateField.setReferenceContact(employeeExperienceDTO.getReferenceContact());
						updateField.setReferenceDesignation(employeeExperienceDTO.getReferenceDesignation());
						updateField.setFromMonthYear(DateHelper.toLocalDate(employeeExperienceDTO.getFromMonthYear()));
						updateField.setToMonthYear(DateHelper.toLocalDate(employeeExperienceDTO.getToMonthYear()));

					} else {
						updateField.setRemove(true);
					}
				});
				// new employee experience entry to be added
				if (!CollectionUtils.isEmpty(addExperience)) {
					for (EmployeeExperienceDTO experience : addExperience) {
						entity.getEmployeeExperiences().add(EmployeeConverter.toEmployeeExperienceEntity(experience, entity));
					}
				}
				// delete existing employee experience entry
			    List<EmployeeExperience> toRemove =  entity.getEmployeeExperiences().stream().filter(EmployeeExperience::isRemove).toList();
                for(EmployeeExperience employeeExperience : toRemove){
                    deleteOldFileIfExists(employeeExperience.getSalarySlipsName(), employeeId, clinicId);
                    deleteOldFileIfExists(employeeExperience.getExperienceCertificateName(), employeeId, clinicId);
                    deleteOldFileIfExists(employeeExperience.getRelievingCertificateName(), employeeId, clinicId);
                }

			}

		}

		/**
		 * Check if employee education dto collection is empty
		 */
		if (!CollectionUtils.isEmpty(dto.getEmployeeEducationDTOsList())) {
			// new employee education entry to be added
			List<EmployeeEducationDTO> addEducation = dto.getEmployeeEducationDTOsList().stream()
					.filter(predicate -> predicate.getId() == null).toList();

			// check if employee education collection is empty
			if (!CollectionUtils.isEmpty(entity.getEmployeeEducations())) {
				// update existing employee education

				List<EmployeeEducationDTO> existingEmployeeEducation = dto.getEmployeeEducationDTOsList().stream()
						.filter(predicate -> predicate.getId() != null).toList();
				entity.getEmployeeEducations().forEach(updateField -> {
					Optional<EmployeeEducationDTO> optional = existingEmployeeEducation.stream()
							.filter(pre -> pre.getId().equals(updateField.getId())).findFirst();
					if (optional.isPresent()) {
						EmployeeEducationDTO employeeEducationDTO = optional.get();
						updateField.setEducationType(EnumConverter.toEnum(employeeEducationDTO.getEducationType(), EducationType.class));
						updateField.setInstituteUniversityName(employeeEducationDTO.getInstituteUniversityName());
						updateField.setSchoolCollegeName(employeeEducationDTO.getSchoolCollegeName());
						updateField.setFromMonthYear(DateHelper.toLocalDate(employeeEducationDTO.getFromMonthYear()));
						updateField.setToMonthYear(DateHelper.toLocalDate(employeeEducationDTO.getToMonthYear()));
					} else {
						updateField.setRemove(true);
					}
				});
				// new education entry to be added
				if (!CollectionUtils.isEmpty(addEducation)) {
					for (EmployeeEducationDTO education : addEducation) {
						entity.getEmployeeEducations()
                       .add(EmployeeConverter.toEmployeeEducationEntity(education, entity));
					}
				}
				// delete existing employee education entry
				List<EmployeeEducation> toRemove =  entity.getEmployeeEducations().stream().filter(EmployeeEducation::isRemove).toList();
                for(EmployeeEducation employeeEducation : toRemove){
                    deleteOldFileIfExists(employeeEducation.getDocumentName(), employeeId, clinicId);
                }

			}
		}

		/**
		 * Check if employee skill dto collection is empty
		 */
		if (!CollectionUtils.isEmpty(dto.getEmployeeSkillsDTOsList())) {
			// new employee skill entry to be added
			List<EmployeeSkillsDTO> addSkills = dto.getEmployeeSkillsDTOsList().stream()
					.filter(predicate -> predicate.getId() == null).toList();

			// check if employee skill collection is empty
			if (!CollectionUtils.isEmpty(entity.getEmployeeSkills())) {
				// update existing employee skill

				List<EmployeeSkillsDTO> existingEmployeeSkills = dto.getEmployeeSkillsDTOsList().stream()
						.filter(predicate -> predicate.getId() != null).toList();
				entity.getEmployeeSkills().forEach(updateField -> {
					Optional<EmployeeSkillsDTO> optional = existingEmployeeSkills.stream()
							.filter(pre -> pre.getId().equals(updateField.getId())).findFirst();
					if (optional.isPresent()) {
						EmployeeSkillsDTO employeeSkillsDTO = optional.get();
						updateField.setSkill(employeeSkillsDTO.getSkill());
						updateField.setInstituteUniversityName(employeeSkillsDTO.getInstituteUniversityName());
						updateField.setRating(employeeSkillsDTO.getRating());
						updateField.setFromMonthYear(DateHelper.toLocalDate(employeeSkillsDTO.getFromMonthYear()));
						updateField.setToMonthYear(DateHelper.toLocalDate(employeeSkillsDTO.getToMonthYear()));
					} else {
						updateField.setRemove(true);
					}
				});
				// new skills entry to be added
				if (!CollectionUtils.isEmpty(addSkills)) {
					for (EmployeeSkillsDTO skills : addSkills) {
						entity.getEmployeeSkills().add(EmployeeConverter.toEmployeeSkillEntity(skills, entity));
					}
				}
				// delete existing employee skill entry
				List<EmployeeSkills> toRemove =  entity.getEmployeeSkills().stream().filter(EmployeeSkills::isRemove).toList();
                for (EmployeeSkills employeeSkills : toRemove){
                    deleteOldFileIfExists(employeeSkills.getDocumentName(), employeeId, clinicId);
                }
			}
		}
	}


	private EmployeeDTO convertBeanToDto(List<EmployeeBean> beans) {
        EmployeeBean employeeBean = beans.get(0);

        // Concatenate contact number (never null)
        String fullContactNumber = !ValidateUtils.isStringEmpty(employeeBean.getContactCountryCode()) ? employeeBean.getContactCountryCode() + " " + employeeBean.getContactNumber() : employeeBean.getContactNumber();

        // Concatenate alternate number (can be null)
        String fullAlternateNumber = null;
        if (!ValidateUtils.isStringEmpty(employeeBean.getAlternateNumber())) {
            fullAlternateNumber = !ValidateUtils.isStringEmpty(employeeBean.getAlternateCountryCode()) ? employeeBean.getAlternateCountryCode() + " " + employeeBean.getAlternateNumber() : employeeBean.getAlternateNumber();
        }


        // Parent data
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employeeBean.getEmpId());
        dto.setSalutation(EnumConverter.toDisplayString(employeeBean.getSalutation(), Salutation.class));
        dto.setFirstName(employeeBean.getFirstName());
        dto.setMiddleName(employeeBean.getMiddleName());
        dto.setLastName(employeeBean.getLastName());
        dto.setFormalName(employeeBean.getFormalName());
        dto.setGender(EnumConverter.toDisplayString(employeeBean.getGender(), Gender.class));
        dto.setDob(DateHelper.formatDateObjectToUserDateView(employeeBean.getDob()));
        dto.setMaritalStatus(employeeBean.getMaritalStatus() != null ? EnumConverter.toDisplayString(employeeBean.getMaritalStatus(), MaritalStatus.class) : null);
        dto.setMarriedSince(DateHelper.formatDateObjectToUserDateView(employeeBean.getMarriedSince()));
        dto.setContactCountryCode(employeeBean.getContactCountryCode());
        dto.setContactNumber(employeeBean.getContactNumber());
        dto.setAlternateCountryCode(employeeBean.getAlternateCountryCode());
        dto.setAlternateNumber(employeeBean.getAlternateNumber());
        dto.setFormalEmail(employeeBean.getFormalEmail());
        dto.setPersonalEmail(employeeBean.getPersonalEmail());
        dto.setAadhaarNumber(employeeBean.getAadhaarNumber());
        dto.setAadhaarFileName(employeeBean.getAadhaarFileName() != null ? serverBaseUrl + employeeBean.getAadhaarFileName() : null);
        dto.setDocumentCountry(employeeBean.getDocumentCountry());
        dto.setPanNumber(employeeBean.getPanNumber());
        dto.setPanFileName(employeeBean.getPanFileName() != null ? serverBaseUrl + employeeBean.getPanFileName() : null);
        dto.setNationality(employeeBean.getNationality());
        dto.setBirthCountryId(employeeBean.getBirthCountryId());
        dto.setBirthCountryName(employeeBean.getBirthCountryName());
        dto.setStateId(employeeBean.getStateId());
        dto.setStateName(employeeBean.getStateName());
        dto.setCityId(employeeBean.getCityId());
        dto.setCityName(employeeBean.getCityName());
        dto.setBloodGroup(employeeBean.getBloodGroup() != null ? EnumConverter.toDisplayString(employeeBean.getBloodGroup(), BloodGroup.class) : null);
        dto.setStatus(EnumConverter.toDisplayString(employeeBean.getStatus(), Status.class));
        dto.setEmpCode(employeeBean.getEmpCode());
        dto.setBiometricCode(employeeBean.getBiometricCode());
        dto.setOfferDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getOfferDate()));
        dto.setOfferAcceptanceDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getOfferAcceptanceDate()));
        dto.setJoiningDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getJoiningDate()));
        dto.setContractStartDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getContractStartDate()));
        dto.setProbationStartDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getProbationStartDate()));
        dto.setProbationEndDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getProbationEndDate()));
        dto.setConfirmationDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getConfirmationDate()));
        dto.setResignationDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getResignationDate()));
        dto.setNoticePeriodStartDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getNoticePeriodStartDate()));
        dto.setNoticePeriodEndDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getNoticePeriodEndDate()));
        dto.setTerminationDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getTerminationDate()));
        dto.setLastWorkingDate(DateHelper.formatDateObjectToUserDateView(employeeBean.getLastWorkingDate()));

        // Employment details DTOs
        CompanyDetailsDto companyDetails = null;
        if (!ValidateUtils.isLongZero(employeeBean.getCompanyId()) || !ValidateUtils.isStringEmpty(employeeBean.getCompany())) {
            companyDetails = CompanyDetailsDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getCompanyId()) ? employeeBean.getCompanyId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getCompany()) ? employeeBean.getCompany() : null)
                    .build();
        }
        dto.setCompanyDetails(companyDetails);
        BranchDetailsDto branchDetails = null;
        if (!ValidateUtils.isLongZero(employeeBean.getBranchId()) || !ValidateUtils.isStringEmpty(employeeBean.getBranch())) {
            branchDetails = BranchDetailsDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getBranchId()) ? employeeBean.getBranchId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getBranch()) ? employeeBean.getBranch() : null)
                    .build();
        }
        dto.setBranchDetails(branchDetails);
        DesignationDto designation = null;
        if (!ValidateUtils.isLongZero(employeeBean.getDesignationId()) || !ValidateUtils.isStringEmpty(employeeBean.getDesignation())) {
            designation = DesignationDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getDesignationId()) ? employeeBean.getDesignationId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getDesignation()) ? employeeBean.getDesignation() : null)
                    .build();
        }
        dto.setDesignation(designation);
        DepartmentDto department = null;
        if (!ValidateUtils.isLongZero(employeeBean.getDepartmentId()) || !ValidateUtils.isStringEmpty(employeeBean.getDepartment())) {
            department = DepartmentDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getDepartmentId()) ? employeeBean.getDepartmentId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getDepartment()) ? employeeBean.getDepartment() : null)
                    .build();
        }
        dto.setDepartment(department);
        WorkLocationDto workLocation = null;
        if (!ValidateUtils.isLongZero(employeeBean.getWorkLocationId()) || !ValidateUtils.isStringEmpty(employeeBean.getWorkLocation())) {
            workLocation = WorkLocationDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getWorkLocationId()) ? employeeBean.getWorkLocationId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getWorkLocation()) ? employeeBean.getWorkLocation() : null)
                    .build();
        }
        dto.setWorkLocation(workLocation);
        SubWorkLocationDto subWorkLocation = null;
        if (!ValidateUtils.isLongZero(employeeBean.getSubWorkLocationId()) || !ValidateUtils.isStringEmpty(employeeBean.getSubWorkLocation())) {
            subWorkLocation = SubWorkLocationDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getSubWorkLocationId()) ? employeeBean.getSubWorkLocationId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getSubWorkLocation()) ? employeeBean.getSubWorkLocation() : null)
                    .build();
        }
        dto.setSubWorkLocation(subWorkLocation);
        GradeDto grade = null;
        if (!ValidateUtils.isLongZero(employeeBean.getGradeId()) || !ValidateUtils.isStringEmpty(employeeBean.getGrade())) {
            grade = GradeDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getGradeId()) ? employeeBean.getGradeId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getGrade()) ? employeeBean.getGrade() : null)
                    .build();
        }
        dto.setGrade(grade);
        CategoryDto category = null;
        if (!ValidateUtils.isLongZero(employeeBean.getCategoryId()) || !ValidateUtils.isStringEmpty(employeeBean.getCategory())) {
            category = CategoryDto.builder()
                    .id(!ValidateUtils.isLongZero(employeeBean.getCategoryId()) ? employeeBean.getCategoryId() : null)
                    .name(!ValidateUtils.isStringEmpty(employeeBean.getCategory()) ? employeeBean.getCategory() : null)
                    .build();
        }
        dto.setCategory(category);

        // Child lists
        dto.setEmployeeAddressDTOList(beans.stream()
                .filter(empBean -> !ValidateUtils.isLongZero(empBean.getAddressId()))
                // Sort by fromDate ascending
                .sorted(Comparator.comparing((EmployeeBean addBean) -> addBean.getAddressType().equalsIgnoreCase("CURRENT") ? 0 : 1)
                .thenComparing(EmployeeBean::getFromDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(empAddBean -> {
                    EmployeeAddressDTO employeeAddressDTO = new EmployeeAddressDTO();
                    employeeAddressDTO.setId(empAddBean.getAddressId());
                    employeeAddressDTO.setCountryId(empAddBean.getCountryId());
                    employeeAddressDTO.setCountryName(empAddBean.getCountryName());
                    employeeAddressDTO.setStateId(empAddBean.getAddressStateId());
                    employeeAddressDTO.setStateName(empAddBean.getAddressStateName());
                    employeeAddressDTO.setCityId(empAddBean.getAddressCityId());
                    employeeAddressDTO.setCityName(empAddBean.getAddressCityName());
                    employeeAddressDTO.setAddress(empAddBean.getEmpAddress());
                    employeeAddressDTO.setPinCode(empAddBean.getPincode());
                    employeeAddressDTO.setAddressType(EnumConverter.toDisplayString(empAddBean.getAddressType(), AddressType.class));
                    employeeAddressDTO.setFromDate(DateHelper.formatDateObjectToUserDateView(empAddBean.getFromDate()));
                    employeeAddressDTO.setToDate(DateHelper.formatDateObjectToUserDateView(empAddBean.getToDate()));
                    return employeeAddressDTO;
                }).distinct().toList());

        dto.setEmployeeFamilyDetailsDTOList(beans.stream()
                .filter(empBean -> !ValidateUtils.isLongZero(empBean.getFamilyId()))
                .map(empFamBean -> {
                    EmployeeFamilyDetailsDTO employeeFamilyDetailsDTO = new EmployeeFamilyDetailsDTO();
                    employeeFamilyDetailsDTO.setId(empFamBean.getFamilyId());
                    employeeFamilyDetailsDTO.setName(empFamBean.getFamilyName());
                    employeeFamilyDetailsDTO.setRelation(empFamBean.getRelation());
                    employeeFamilyDetailsDTO.setContactNumber(empFamBean.getFamilyContactNumber());
                    employeeFamilyDetailsDTO.setDob(DateHelper.formatDateObjectToUserDateView(empFamBean.getFamilyDob()));
                    return employeeFamilyDetailsDTO;
                }).distinct().toList());

        dto.setEmployeeExperienceDTOList(beans.stream()
                .filter(empBean -> !ValidateUtils.isLongZero(empBean.getExperienceId()))
                .map(empExpBean -> {
                    EmployeeExperienceDTO employeeExperienceDTO = new EmployeeExperienceDTO();
                    employeeExperienceDTO.setId(empExpBean.getExperienceId());
                    employeeExperienceDTO.setExperienceType(EnumConverter.toDisplayString(empExpBean.getExperienceType(), ExperienceType.class));
                    employeeExperienceDTO.setCompanyName(empExpBean.getExperienceCompanyName());
                    employeeExperienceDTO.setAddress(empExpBean.getExpCompAddress());
                    employeeExperienceDTO.setDesignation(empExpBean.getEmpExpDesignation());
                    employeeExperienceDTO.setFromDate(DateHelper.formatDateObjectToUserDateView(empExpBean.getExpFromDate()));
                    employeeExperienceDTO.setToDate(DateHelper.formatDateObjectToUserDateView(empExpBean.getExpToDate()));
                    employeeExperienceDTO.setTotalSpan(DateHelper.formatTotalSpan(empExpBean.getTotalSpan()));
                    employeeExperienceDTO.setReferenceName(empExpBean.getReferenceName());
                    employeeExperienceDTO.setReferenceContact(empExpBean.getReferenceContact());
                    employeeExperienceDTO.setReferenceDesignation(empExpBean.getReferenceDesignation());
                    employeeExperienceDTO.setFromMonthYear(DateHelper.toYearMonthString(empExpBean.getExpFromMonthYear()));
                    employeeExperienceDTO.setToMonthYear(DateHelper.toYearMonthString(empExpBean.getExpToMonthYear()));
                    employeeExperienceDTO.setSalarySlipsName(employeeBean.getSalarySlipsName() != null ? serverBaseUrl + employeeBean.getSalarySlipsName() : null);
                    employeeExperienceDTO.setExperienceCertificateName(employeeBean.getExperienceCertificateName() != null ? serverBaseUrl + employeeBean.getExperienceCertificateName() : null);
                    employeeExperienceDTO.setRelievingCertificateName(employeeBean.getRelievingCertificateName() != null ? serverBaseUrl + employeeBean.getRelievingCertificateName() : null);
                    return employeeExperienceDTO;
                }).distinct().toList());

        dto.setEmployeeEducationDTOsList(beans.stream()
                .filter(empBean -> !ValidateUtils.isLongZero(empBean.getEducationId()))
                .map(empEduBean -> {
                    EmployeeEducationDTO employeeEducationDTO = new EmployeeEducationDTO();
                    employeeEducationDTO.setId(empEduBean.getEducationId());
                    employeeEducationDTO.setEducationType(EnumConverter.toDisplayString(empEduBean.getEducationType(), EducationType.class));
                    employeeEducationDTO.setInstituteUniversityName(empEduBean.getEduInstituteUniversityName());
                    employeeEducationDTO.setSchoolCollegeName(empEduBean.getSchoolCollegeName());
                    employeeEducationDTO.setFromMonthYear(DateHelper.toYearMonthString(empEduBean.getEduFromMonthYear()));
                    employeeEducationDTO.setToMonthYear(DateHelper.toYearMonthString(empEduBean.getEduToMonthYear()));
                    employeeEducationDTO.setDocumentName(employeeBean.getEduDocumentName() != null ? serverBaseUrl + empEduBean.getEduDocumentName() : null);
                    return employeeEducationDTO;
                }).distinct().toList());

        dto.setEmployeeSkillsDTOsList(beans.stream()
                .filter(empBean -> !ValidateUtils.isLongZero(empBean.getSkillId()))
                .map(empSkillBean -> {
                    EmployeeSkillsDTO employeeSkillsDTO = new EmployeeSkillsDTO();
                    employeeSkillsDTO.setId(empSkillBean.getSkillId());
                    employeeSkillsDTO.setSkill(empSkillBean.getSkillName());
                    employeeSkillsDTO.setInstituteUniversityName(empSkillBean.getSkillInstituteUniversityName());
                    employeeSkillsDTO.setRating(empSkillBean.getRating());
                    employeeSkillsDTO.setFromMonthYear(DateHelper.toYearMonthString(empSkillBean.getSkillFromMonthYear()));
                    employeeSkillsDTO.setToMonthYear(DateHelper.toYearMonthString(empSkillBean.getSkillToMonthYear()));
                    employeeSkillsDTO.setDocumentName(employeeBean.getSkillDocumentName() != null ?  serverBaseUrl + empSkillBean.getSkillDocumentName() : null);
                    return employeeSkillsDTO;
                }).distinct().toList());

        return dto;
    }

    public EmployeeNameCodeDTO convertNameCodeBeanToDto(EmployeeNameCodeBean employeeNameCodeBean){
        EmployeeNameCodeDTO employeeNameCodeDTO = new EmployeeNameCodeDTO();
        employeeNameCodeDTO.setId(employeeNameCodeBean.getEmpId());
        employeeNameCodeDTO.setFormalName(employeeNameCodeBean.getFormalName());
        employeeNameCodeDTO.setEmpCode(employeeNameCodeBean.getEmpCode());
        return employeeNameCodeDTO;
    }



    /**
     * Converts a base64-encoded string representing a binary file and stores it
     * associated with the given employee and clinic IDs.
     *
     * @param base64Data the base64-encoded file data
     * @param employeeId the UUID of the employee
     * @param clinicId the ID of the clinic
     * @return a String representing the stored file path or identifier
     */
    private String storeBase64File(String base64Data,  UUID employeeId, String clinicId) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }
        try {
            // Remove possible "data:image/png;base64," prefix
            String cleanBase64 = base64Data.contains(",") ? base64Data.split(",")[1] : base64Data;

            // Decode Base64
            byte[] fileBytes = Base64.getDecoder().decode(cleanBase64);

            // Validate file size (2MB limit)
            if (fileBytes.length > 2 * 1024 * 1024) {
                throw new ValidationException(messageResource.getEnglishMessage("file.size.exceeds"));
            }

            // Detect file type from Base64 (simple check)
            String fileType = detectImageType(fileBytes);
            if (fileType == null || !(fileType.equals("jpg") || fileType.equals("png"))) {
                throw new ValidationException(messageResource.getEnglishMessage("invalid.file.type"));
            }

            // File name: employeeId + extension
            String uniqueId = UUID.randomUUID().toString();
            String timestamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
            String fileName = employeeId.toString() + "_" + uniqueId + "_" + timestamp + "." + fileType;

            // Directory path
            Path folderPath = Paths.get(baseUploadDir,clinicId, "hr", "employee", employeeId.toString()); // path
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // Save file
            Path filePath = folderPath.resolve(fileName);
            Files.write(filePath, fileBytes);

            return String.join("/", clinicId, "hr", "employee", employeeId.toString(), fileName);

        } catch (IllegalArgumentException e) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.format"));
        } catch (IOException e) {
            throw new RuntimeException(messageResource.getEnglishMessage("failed.to.store.file"), e);
        }
    }

    /**
     * Detects the image type from the provided file bytes.
     *
     * @param fileBytes the byte array of the file to analyze
     * @return the image type as a String (e.g., "png", "jpeg"), or null if type cannot be determined
     */
    private String detectImageType(byte[] fileBytes) {
        // Simple number check
        if (fileBytes.length >= 4) {
            // JPG numbers
            if ((fileBytes[0] & 0xFF) == 0xFF && (fileBytes[1] & 0xFF) == 0xD8) {
                return "jpg"; // 0xFF and 0xD8 are the hexadecimal values of the first two bytes in any JPEG file.
            }
            // PNG numbers
            if ((fileBytes[0] & 0xFF) == 0x89 && (fileBytes[1] & 0xFF) == 0x50 &&
                    (fileBytes[2] & 0xFF) == 0x4E && (fileBytes[3] & 0xFF) == 0x47) {
                return "png"; // PNG files always start with 89 50 4E 47 in hexadecimal values.
            }
        }
        return null;
    }

    /**
     * Deletes the old file associated with the given employee and clinic if it exists.
     *
     * @param fileName the name of the file to delete
     * @param employeeId the UUID of the employee
     * @param clinicId the ID of the clinic
     */
    private void deleteOldFileIfExists(String fileName, UUID employeeId, String clinicId) {
        if (fileName == null || fileName.isBlank()) return;

        // If it's Base64, do not to delete
        if (isBase64(fileName)) return;

        try {
            // Normalize path separators (DB stores with "/")
            String normalized = fileName.replace("/", File.separator);

            Path filePath = Paths.get(baseUploadDir, normalized);
            log.info("Deleting file: {}", filePath.toAbsolutePath());

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            log.error("Failed to delete old file: {}", fileName, e);
        }
    }

    /**
     * Updates the file: keeps old if unchanged, deletes if new is null,
     * or stores new Base64 file.
     *
     * @param oldFileName current file name
     * @param newFileData new file data as Base64 string or null
     * @param employeeId employee UUID
     * @param clinicId clinic ID
     * @return updated file name or null if deleted
     */
    private String handleFileUpdate(String oldFileName, String newFileData, UUID employeeId, String clinicId) {
        // Null or empty → delete old file and return null
        if (newFileData == null || newFileData.trim().isEmpty()) {
            deleteOldFileIfExists(oldFileName, employeeId, clinicId);
            return null;
        }

        // If Base64 → check if content actually differs
        if (isBase64(newFileData)) {
            // Optional: compare new file bytes with existing file bytes
            if (oldFileName != null && !oldFileName.isBlank()) {
                Path existingFilePath = Paths.get(baseUploadDir, oldFileName.replace("/", File.separator));
                try {
                    byte[] oldBytes = Files.readAllBytes(existingFilePath);
                    String cleanBase64 = newFileData.contains(",") ? newFileData.split(",")[1] : newFileData;
                    byte[] newBytes = Base64.getDecoder().decode(cleanBase64);
                    if (Arrays.equals(oldBytes, newBytes)) {
                        // No change → keep old file name
                        return oldFileName;
                    }
                } catch (IOException e) {
                    log.warn("Could not read old file for comparison: {}", oldFileName, e);
                }
            }
            // Content changed → delete old and store new
            deleteOldFileIfExists(oldFileName, employeeId, clinicId);
            return storeBase64File(newFileData, employeeId, clinicId);
        }

        // Else: assume unchanged, keep old path
        return oldFileName;
    }

    /**
     * Checks if the given string is a valid Base64-encoded value.
     *
     * @param data the string to check
     * @return true if the string is Base64-encoded, false otherwise
     */
    private boolean isBase64(String data) {
        if (data == null || data.trim().isEmpty()) return false;
        // Usually, Base64 images start with "data:image/"
        return data.startsWith("data:image/") || data.matches("^[A-Za-z0-9+/=\\r\\n]+$");
    }


    public static UserMDTO toUserMDTO(EmployeeDTO dto, UUID empId) {
        if (dto == null) {
            return null;
        }

        return UserMDTO.builder()
                .initial(dto.getSalutation())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .clinicName(null)
                .userType(4)
                .mobile(dto.getContactNumber())
                .email(dto.getFormalEmail())
                .country(dto.getNationality())
                .password("Welcome@123")
                .userId(dto.getEmpCode())
                .discription("1")
                .branchId(1)
                .department(null)
                .departmentCode(null)
                .doctor(0)
                .isGlobalAccess(true)
                .isActiveInactive(true)
                .sectionId(0)
                .clinicId(1)
                .doNotAddInPayroll(false)
                .empId(String.valueOf(empId))
                .build();
    }
}
