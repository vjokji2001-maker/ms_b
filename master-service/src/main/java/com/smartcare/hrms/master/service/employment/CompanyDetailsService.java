package com.smartcare.hrms.master.service.employment;

import com.smartcare.hrms.common.bean.master.employment.CompanyDetailsBean;
import com.smartcare.hrms.common.dto.master.employment.CompanyDetailsDto;
import com.smartcare.hrms.common.dto.master.employment.DesignationDto;
import com.smartcare.hrms.common.entity.master.employment.CompanyDetails;
import com.smartcare.hrms.common.entity.master.employment.Designation;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.CompanyDetailsRepository;
import com.smartcare.hrms.common.repository.master.employment.DesignationRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.master.config.MessageResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class CompanyDetailsService {
	
	
	@Autowired
	private CompanyDetailsRepository companyDetailsRepository;
	
	@Autowired
	private DesignationRepository designationRepository;

    @Autowired
    private MessageResource messageResource;

    @Value("${file.upload.dir}")
    private String baseUploadDir;

    @Value("${server.base.url}")
    private String serverBaseUrl;


    /**
     * Retrieves a company details entry by its ID.
     *
     * @param id the ID of the company
     * @return the corresponding CompanyDetailsDto
     */
	public CompanyDetailsDto getById(Long id) {
		CompanyDetailsBean companyDetailsBean = companyDetailsRepository.getAllById(id);
		if (!ValidateUtils.isNullObject(companyDetailsBean)) {
			return convertBeanToDto(companyDetailsBean);
		} else {
			throw new ValidationException(messageResource.getEnglishMessage("company.data.not.found"));
		}
	}


    /**
     * Fetches company details by code, by name (containing), or all if no filters are provided.
     *
     * @param companyDetailsDto the search criteria (code or name)
     * @return list of matching CompanyDetailsDto objects
     */
    public List<CompanyDetailsDto> getByNameOrCodeOrAll(CompanyDetailsDto companyDetailsDto){
		List<CompanyDetailsDto> companyList = new ArrayList<>();
		if(!ValidateUtils.isStringEmpty(companyDetailsDto.getName())) {
			List<CompanyDetailsBean> byName = companyDetailsRepository.getByName(companyDetailsDto.getName());
			if(!CollectionUtils.isEmpty(byName)) {
				for(CompanyDetailsBean companyDetailsBean : byName) {
					companyList.add(convertBeanToDto(companyDetailsBean));
				}
			}
		} else if (!ValidateUtils.isStringEmpty(companyDetailsDto.getCode())) {
            CompanyDetailsBean byCode = companyDetailsRepository.getByCode(companyDetailsDto.getCode());
            if (!ValidateUtils.isNullObject(byCode)){
                companyList.add(convertBeanToDto(byCode));
            }
        } else {
				List<CompanyDetailsBean> listCompany = companyDetailsRepository.getAll();
				if(!CollectionUtils.isEmpty(listCompany)) {
					for(CompanyDetailsBean companyDetailsBean : listCompany) {
						companyList.add(convertBeanToDto(companyDetailsBean));
					}
				}
			}
		if (companyList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("company.data.not.found"));
		}
        companyList.sort(Comparator.comparing(CompanyDetailsDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
		return companyList;
	}



    /**
     * Creates a new company along with its designation, country, state, and city details.
     *
     * @param companyDetailsDto the company details to create
     * @return the created CompanyDetailsDto
     */
    @Transactional
	public CompanyDetailsDto create(CompanyDetailsDto companyDetailsDto) {
		
		// Validating company details
		validateRequest(companyDetailsDto); 
	    validateByRepository(companyDetailsDto);
	
		Designation designation = designationRepository.findById(companyDetailsDto.getDesignation().getId())
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("designation.data.not.found")));
		
		CompanyDetails companyDetails = CompanyDetails.builder()
		    .name(companyDetailsDto.getName())
            .code(companyDetailsDto.getCode())
		    .registeredName(companyDetailsDto.getRegisteredName())
		    .registeredNumber(companyDetailsDto.getRegisteredNumber())
		    .incorporationDate(DateHelper.convertUserDateToLocalDate(companyDetailsDto.getIncorporationDate()))
		    .panNumber(normalizeEmpty(safeToUpper(companyDetailsDto.getPanNumber())))
		    .tanNumber(normalizeEmpty(safeToUpper(companyDetailsDto.getTanNumber())))
		    .cinNumber(normalizeEmpty(safeToUpper(companyDetailsDto.getCinNumber())))
		    .gstin(normalizeEmpty(safeToUpper(companyDetailsDto.getGstin())))
		    .ptRegNumber(normalizeEmpty(companyDetailsDto.getPtRegNumber()))
		    .pfRegistrationNumber(normalizeEmpty(safeToUpper(companyDetailsDto.getPfRegistrationNumber())))
		    .esicRegNumber(normalizeEmpty(safeToUpper(companyDetailsDto.getEsicRegNumber())))
		    .msmeRegNo(normalizeEmpty(safeToUpper(companyDetailsDto.getMsmeRegNo())))
		    .iecCode(normalizeEmpty(safeToUpper(companyDetailsDto.getIecCode())))
		    .ptZone(normalizeEmpty(companyDetailsDto.getPtZone()))
		    .pfZone(normalizeEmpty(companyDetailsDto.getPfZone()))
		    .esicZone(normalizeEmpty(companyDetailsDto.getEsicZone()))
		    .industryType(companyDetailsDto.getIndustryType())
		    .address(normalizeEmpty(companyDetailsDto.getAddress()))
            .countryId(companyDetailsDto.getCountryId())
		    .stateId((companyDetailsDto.getStateId()))
		    .cityId((companyDetailsDto.getCityId()))
		    .pincode(normalizeEmpty(companyDetailsDto.getPincode()))
		    .contactPersonName(companyDetailsDto.getContactPersonName())
		    .designation(designation)
		    .contactPersonEmail(companyDetailsDto.getContactPersonEmail())
            .countryCode(companyDetailsDto.getCountryCode())
		    .phone(companyDetailsDto.getPhone())
		    .email(normalizeEmpty(companyDetailsDto.getEmail()))
		    .website(normalizeEmpty(companyDetailsDto.getWebsite()))
		    .twitter(normalizeEmpty(companyDetailsDto.getTwitter()))
		    .facebook(normalizeEmpty(companyDetailsDto.getFacebook()))
		    .instagram(normalizeEmpty(companyDetailsDto.getInstagram()))
		    .imageUrl(null)
		    .build();

        // Save entity to generate ID
	    CompanyDetails create =	companyDetailsRepository.save(companyDetails);
        Long companyId = create.getId();

        // Store company document file
        String logoImageUrl = storeBase64File(companyDetailsDto.getImageUrl(), companyId);
        create.setImageUrl(logoImageUrl);

        companyDetailsRepository.save(create);
        return toDto(create);
		
	}


    /**
     * Deletes a company details entry by its ID.
     *
     * @param id the ID of the company to delete
     */
    @Transactional
    public void delete(Long id) {
        CompanyDetails companyDetails = companyDetailsRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("company.data.not.found")));
        // Utility method is invoked, if file exists
        deleteFileIfExists(companyDetails.getImageUrl());

        companyDetailsRepository.delete(companyDetails);
    }


    /**
     * Updates an existing company details entry, including related designation, country, state, and city.
     *
     * @param companyDetailsDto the updated company details
     * @return the updated CompanyDetailsDto
     * @throws IOException if any file or data handling error occurs
     */
    @Transactional
	public CompanyDetailsDto update(CompanyDetailsDto companyDetailsDto) throws IOException {

		if (ValidateUtils.isLongZero(companyDetailsDto.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("company.details.required"));
		}
		// Validating company details
		validateRequest(companyDetailsDto); 

		Optional<CompanyDetails> optional = companyDetailsRepository.findById(companyDetailsDto.getId());
		if (optional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("company.data.not.found"));
		}
		CompanyDetails companyDetails = optional.get();

        boolean changedCode           = !Objects.equals(companyDetails.getCode(), companyDetailsDto.getCode());
        boolean changedRegisteredName = !Objects.equals(companyDetails.getRegisteredName(), companyDetailsDto.getRegisteredName());
		boolean changedNumber         = !Objects.equals(companyDetails.getRegisteredNumber(), companyDetailsDto.getRegisteredNumber());
		boolean changedPan            = !Objects.equals(companyDetails.getPanNumber(), companyDetailsDto.getPanNumber());
		boolean changedTan            = !Objects.equals(companyDetails.getTanNumber(), companyDetailsDto.getTanNumber());
		boolean changedCin            = !Objects.equals(companyDetails.getCinNumber(), companyDetailsDto.getCinNumber());
		boolean changedGstin          = !Objects.equals(companyDetails.getGstin(), companyDetailsDto.getGstin());
		boolean changedPt             = !Objects.equals(companyDetails.getPtRegNumber(), companyDetailsDto.getPtRegNumber());
		boolean changedPf             = !Objects.equals(companyDetails.getPfRegistrationNumber(), companyDetailsDto.getPfRegistrationNumber());
		boolean changedEsic           = !Objects.equals(companyDetails.getEsicRegNumber(), companyDetailsDto.getEsicRegNumber());
		boolean changedMsme           = !Objects.equals(companyDetails.getMsmeRegNo(), companyDetailsDto.getMsmeRegNo());
		boolean changedIec            = !Objects.equals(companyDetails.getIecCode(), companyDetailsDto.getIecCode());


        if (changedCode) {
            String newValue = normalizeEmpty(companyDetailsDto.getCode());
            if (newValue != null) {
                CompanyDetails byCode = companyDetailsRepository.findByCode(newValue);
                if (byCode != null && !byCode.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
                }
                companyDetails.setCode(newValue);
            } else {
                companyDetails.setCode(null);
            }
        }

        if (changedRegisteredName) {
            String newValue = normalizeEmpty(companyDetailsDto.getRegisteredName());
            if (newValue != null) {
                CompanyDetails byRegisteredName = companyDetailsRepository.findByRegisteredName(newValue);
                if (byRegisteredName != null && !byRegisteredName.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("registered.name.exists"));
                }
                companyDetails.setRegisteredName(newValue);
            } else {
                companyDetails.setRegisteredName(null);
            }
        }

        if (changedNumber) {
            String newValue = normalizeEmpty(companyDetailsDto.getRegisteredNumber());
            if (newValue != null) {
                CompanyDetails byRegNum = companyDetailsRepository.findByRegisteredNumber(newValue);
                if (byRegNum != null && !byRegNum.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("registered.number.exists"));
                }
                companyDetails.setRegisteredNumber(newValue);
            } else {
                companyDetails.setRegisteredNumber(null);
            }
        }

        if (changedPan) {
            String newValue = normalizeEmpty(companyDetailsDto.getPanNumber());
            if (newValue != null) {
                CompanyDetails byPan = companyDetailsRepository.findByPanNumber(newValue);
                if (byPan != null && !byPan.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("pan.number.exists"));
                }
                companyDetails.setPanNumber(newValue);
            } else {
                companyDetails.setPanNumber(null);
            }
        }

        if (changedTan) {
            String newValue = normalizeEmpty(companyDetailsDto.getTanNumber());
            if (newValue != null) {
                CompanyDetails byTan = companyDetailsRepository.findByTanNumber(newValue);
                if (byTan != null && !byTan.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("tan.number.exists"));
                }
                companyDetails.setTanNumber(newValue);
            } else {
                companyDetails.setTanNumber(null);
            }
        }

        if (changedCin) {
            String newValue = normalizeEmpty(companyDetailsDto.getCinNumber());
            if (newValue != null) {
                CompanyDetails byCin = companyDetailsRepository.findByCinNumber(newValue);
                if (byCin != null && !byCin.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("cin.number.exists"));
                }
                companyDetails.setCinNumber(newValue);
            } else {
                companyDetails.setCinNumber(null);
            }
        }

        if (changedGstin) {
            String newValue = normalizeEmpty(companyDetailsDto.getGstin());
            if (newValue != null) {
                CompanyDetails byGstin = companyDetailsRepository.findByGstin(newValue);
                if (byGstin != null && !byGstin.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("gstin.number.exists"));
                }
                companyDetails.setGstin(newValue);
            } else {
                companyDetails.setGstin(null);
            }
        }

        if (changedPt) {
            String newValue = normalizeEmpty(companyDetailsDto.getPtRegNumber());
            if (newValue != null) {
                CompanyDetails byPtRegNum = companyDetailsRepository.findByPtRegNumber(newValue);
                if (byPtRegNum != null && !byPtRegNum.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("pt.registration.exists"));
                }
                companyDetails.setPtRegNumber(newValue);
            } else {
                companyDetails.setPtRegNumber(null);
            }
        }

        if (changedPf) {
            String newValue = normalizeEmpty(companyDetailsDto.getPfRegistrationNumber());
            if (newValue != null) {
                CompanyDetails byPfRegNum = companyDetailsRepository.findByPfRegistrationNumber(newValue);
                if (byPfRegNum != null && !byPfRegNum.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("pf.registration.exists"));
                }
                companyDetails.setPfRegistrationNumber(newValue);
            } else {
                companyDetails.setPfRegistrationNumber(null);
            }
        }

        if (changedEsic) {
            String newValue = normalizeEmpty(companyDetailsDto.getEsicRegNumber());
            if (newValue != null) {
                CompanyDetails byEsic = companyDetailsRepository.findByEsicRegNumber(newValue);
                if (byEsic != null && !byEsic.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("esic.registration.number.exists"));
                }
                companyDetails.setEsicRegNumber(newValue);
            } else {
                companyDetails.setEsicRegNumber(null);
            }
        }

        if (changedMsme) {
            String newValue = normalizeEmpty(companyDetailsDto.getMsmeRegNo());
            if (newValue != null) {
                CompanyDetails byMsme = companyDetailsRepository.findByMsmeRegNo(newValue);
                if (byMsme != null && !byMsme.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("msme.registration.number.exists"));
                }
                companyDetails.setMsmeRegNo(newValue);
            } else {
                companyDetails.setMsmeRegNo(null);
            }
        }

        if (changedIec) {
            String newValue = normalizeEmpty(companyDetailsDto.getIecCode());
            if (newValue != null) {
                CompanyDetails byIec = companyDetailsRepository.findByIecCode(newValue);
                if (byIec != null && !byIec.getId().equals(companyDetailsDto.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("iec.code.exists"));
                }
                companyDetails.setIecCode(newValue);
            } else {
                companyDetails.setIecCode(null);
            }
        }

		convertForUpdate(companyDetails, companyDetailsDto);

        Long companyId = companyDetails.getId();
        String oldImage = companyDetails.getImageUrl();
        String newImage = companyDetailsDto.getImageUrl();

        // Image unchanged keep the existing image as it is
        if (Objects.equals(oldImage, newImage)) {
            log.info("Image unchanged; no update required for company ID {}", companyId);
        }
        // If image is removed
        else if (newImage == null || newImage.trim().isEmpty()) {
            if (oldImage != null) {
                Path oldFilePath = Paths.get(baseUploadDir, oldImage);
                try {
                    Files.deleteIfExists(oldFilePath);
                    deleteFileIfExists(oldImage);
                } catch (IOException e) {
                    log.error("Failed to delete old logo file: {}", oldFilePath, e);
                }
            }
            companyDetails.setImageUrl(null);
        }
        // If frontend sends existing file path (URL) → ignore, keep old image
        else if (newImage.startsWith("http")) {
            log.info("Received image as URL; keeping existing image for company ID {}", companyId);
            companyDetails.setImageUrl(oldImage);
        }
        // New image uploaded → replace old file
        else {
            // First store the new file
            String newImagePath = storeBase64File(newImage, companyId);

            if (newImagePath != null) {
                // Delete old file after new one is saved
                if (oldImage != null && !Objects.equals(newImagePath, oldImage)) {
                    Path oldFilePath = Paths.get(baseUploadDir, oldImage);
                    try {
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        log.error("Failed to delete old logo file: {}", oldFilePath, e);
                    }
                }
                // Update in DB with new path
                companyDetails.setImageUrl(newImagePath);
            }
        }

        // Save only once at the end
        CompanyDetails updated = companyDetailsRepository.save(companyDetails);
		return toDto(updated);
	}


    private CompanyDetailsDto convertBeanToDto(CompanyDetailsBean companyDetailsBean) {
        DesignationDto contactPersonDesignationDto = null;
        if (!ValidateUtils.isNullObject(companyDetailsBean) && !ValidateUtils.isLongZero(companyDetailsBean.getDesignationId())) {
            contactPersonDesignationDto = DesignationDto.builder()
                    .id(companyDetailsBean.getDesignationId())
                    .name(companyDetailsBean.getDesignationName())
                    .code(companyDetailsBean.getDesignationCode())
                    .build();
        }

        // Concatenate country code and phone

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
                .countryId(companyDetailsBean.getCountryId())
                .countryName(companyDetailsBean.getCountryName())
                .stateId(companyDetailsBean.getStateId())
                .stateName(companyDetailsBean.getStateName())
                .cityId(companyDetailsBean.getCityId())
                .cityName(companyDetailsBean.getCityName())
                .pincode(companyDetailsBean.getPincode())
                .contactPersonName(companyDetailsBean.getContactPersonName())
                .designation(contactPersonDesignationDto)
                .contactPersonEmail(companyDetailsBean.getContactPersonEmail())
                .countryCode(companyDetailsBean.getCountryCode())
                .phone(companyDetailsBean.getPhone())
                .email(companyDetailsBean.getEmail())
                .website(companyDetailsBean.getWebsite())
                .twitter(companyDetailsBean.getTwitter())
                .facebook(companyDetailsBean.getFacebook())
                .instagram(companyDetailsBean.getInstagram())
                .imageUrl(companyDetailsBean.getImageUrl() != null ? serverBaseUrl + companyDetailsBean.getImageUrl() : null)
                .build();
    }



	private CompanyDetailsDto toDto(CompanyDetails companyDetails) {
		if (companyDetails == null) {
			return null;
		}
        DesignationDto designationDto = null;
        if (!ValidateUtils.isNullObject(companyDetails.getDesignation()) && !ValidateUtils.isLongZero(companyDetails.getDesignation().getId())) {
            designationDto = DesignationDto.builder()
                    .id(companyDetails.getDesignation().getId())
                    .name(companyDetails.getDesignation().getName())
                    .code(companyDetails.getDesignation().getCode())
                    .build();
        }

        // Concatenate country code and phone
        String fullPhone = null;
        if (!ValidateUtils.isStringEmpty(companyDetails.getCountryCode()) && !ValidateUtils.isStringEmpty(companyDetails.getPhone())) {
            fullPhone = companyDetails.getCountryCode() + " " + companyDetails.getPhone();
        }
        return CompanyDetailsDto.builder()
                .id(companyDetails.getId())
                .name(companyDetails.getName())
                .code(companyDetails.getCode())
                .registeredName(companyDetails.getRegisteredName())
                .registeredNumber(companyDetails.getRegisteredNumber())
                .incorporationDate(DateHelper.formatDateObjectToUserDateView(companyDetails.getIncorporationDate()))
                .panNumber(companyDetails.getPanNumber())
                .tanNumber(companyDetails.getTanNumber())
                .cinNumber(companyDetails.getCinNumber())
                .gstin(companyDetails.getGstin())
                .ptRegNumber(companyDetails.getPtRegNumber())
                .pfRegistrationNumber(companyDetails.getPfRegistrationNumber())
                .esicRegNumber(companyDetails.getEsicRegNumber())
                .msmeRegNo(companyDetails.getMsmeRegNo())
                .iecCode(companyDetails.getIecCode())
                .ptZone(companyDetails.getPtZone())
                .pfZone(companyDetails.getPfZone())
                .esicZone(companyDetails.getEsicZone())
                .industryType(companyDetails.getIndustryType())
                .address(companyDetails.getAddress())
                .countryId(companyDetails.getCountryId())
                .stateId(companyDetails.getStateId())
                .cityId(companyDetails.getCityId())
                .pincode(companyDetails.getPincode())
                .contactPersonName(companyDetails.getContactPersonName())
                .designation(designationDto)
                .contactPersonEmail(companyDetails.getContactPersonEmail())
                .countryCode(companyDetails.getCountryCode())
                .phone(fullPhone)
                .email(companyDetails.getEmail())
                .website(companyDetails.getWebsite())
                .twitter(companyDetails.getTwitter())
                .facebook(companyDetails.getFacebook())
                .instagram(companyDetails.getInstagram())
                .imageUrl(companyDetails.getImageUrl() != null ? serverBaseUrl + companyDetails.getImageUrl() : null)
                .build();

	}


	private void convertForUpdate(CompanyDetails entity, CompanyDetailsDto dto) {
		entity.setName(dto.getName());
        entity.setCode(dto.getCode());
		entity.setRegisteredName(dto.getRegisteredName());
		entity.setRegisteredNumber(dto.getRegisteredNumber());
		entity.setIncorporationDate(DateHelper.convertUserDateToLocalDate(dto.getIncorporationDate()));
		entity.setPanNumber(normalizeEmpty(safeToUpper(dto.getPanNumber())));
		entity.setTanNumber(normalizeEmpty(safeToUpper(dto.getTanNumber())));
		entity.setCinNumber(normalizeEmpty(safeToUpper(dto.getCinNumber())));
		entity.setGstin(normalizeEmpty(safeToUpper(dto.getGstin())));
		entity.setPtRegNumber(normalizeEmpty(dto.getPtRegNumber()));
		entity.setPfRegistrationNumber(normalizeEmpty(safeToUpper(dto.getPfRegistrationNumber())));
		entity.setEsicRegNumber(normalizeEmpty(safeToUpper(dto.getEsicRegNumber())));
		entity.setMsmeRegNo(normalizeEmpty(safeToUpper(dto.getMsmeRegNo())));
		entity.setIecCode(normalizeEmpty(safeToUpper(dto.getIecCode())));
		entity.setPtZone(normalizeEmpty(dto.getPtZone()));
		entity.setPfZone(normalizeEmpty(dto.getPfZone()));
		entity.setEsicZone(normalizeEmpty(dto.getEsicZone()));
		entity.setIndustryType((dto.getIndustryType()));
		entity.setAddress(normalizeEmpty(dto.getAddress()));
		entity.setCountryId(dto.getCountryId());
		entity.setStateId(dto.getStateId());
		entity.setCityId(dto.getCityId());
		entity.setPincode(normalizeEmpty(dto.getPincode()));
		entity.setContactPersonName(dto.getContactPersonName());
		if (dto.getDesignation() != null && dto.getDesignation().getId() != null) {
			Designation designation = designationRepository.findById(dto.getDesignation().getId())
			.orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("designation.data.not.found")));
			entity.setDesignation(designation);
		}
		entity.setContactPersonEmail(dto.getContactPersonEmail());
        entity.setCountryCode(dto.getCountryCode());
        entity.setCountryCode(dto.getCountryCode());
		entity.setPhone(dto.getPhone());
		entity.setEmail(normalizeEmpty(dto.getEmail()));
		entity.setWebsite(normalizeEmpty(dto.getWebsite()));
		entity.setTwitter(normalizeEmpty(dto.getTwitter()));
		entity.setFacebook(normalizeEmpty(dto.getFacebook()));
		entity.setInstagram(normalizeEmpty(dto.getInstagram()));
	}


    private void validateRequest(CompanyDetailsDto companyDetailsDto) {
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("company.name.required"));
        }
        if (companyDetailsDto.getName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("company.name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(companyDetailsDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.company.name.format"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if (companyDetailsDto.getCode().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
        }
        if (!ValidateUtils.isValidCodeFormat(companyDetailsDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getRegisteredName())) {
            throw new ValidationException(messageResource.getEnglishMessage("registered.name.required"));
        }
        if (companyDetailsDto.getRegisteredName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("registered.name.too.long"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getRegisteredNumber())) {
            throw new ValidationException(messageResource.getEnglishMessage("registered.number.required"));
        }
        if (companyDetailsDto.getRegisteredNumber().length() > 50) {
            throw new ValidationException(messageResource.getEnglishMessage("registered.number.too.long"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getIncorporationDate())){
            throw new ValidationException(messageResource.getEnglishMessage("incorporation.date.required"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getIndustryType())) {
            throw new ValidationException(messageResource.getEnglishMessage("industry.type.required"));
        }
        if (companyDetailsDto.getIndustryType().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("industry.type.too.long"));
        }
        if (ValidateUtils.isZero(companyDetailsDto.getCountryId())) {
            throw new ValidationException(messageResource.getEnglishMessage("country.id.required"));
        }
        if (ValidateUtils.isZero(companyDetailsDto.getStateId())) {
            throw new ValidationException(messageResource.getEnglishMessage("state.id.required"));
        }
        if (ValidateUtils.isZero(companyDetailsDto.getCityId())) {
            throw new ValidationException(messageResource.getEnglishMessage("city.id.required"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getContactPersonName())) {
            throw new ValidationException(messageResource.getEnglishMessage("contact.person.name.required"));
        }
        if (companyDetailsDto.getContactPersonName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("contact.person.name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(companyDetailsDto.getContactPersonName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.contact.person.name.format"));
        }
        if (ValidateUtils.isLongZero(companyDetailsDto.getDesignation().getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("contact.person.designation.required"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getContactPersonEmail())) {
            throw new ValidationException(messageResource.getEnglishMessage("contact.person.email.required"));
        }
        if (companyDetailsDto.getContactPersonEmail().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("contact.person.email.too.long"));
        }
        if (!ValidateUtils.isValidEmailFormat(companyDetailsDto.getContactPersonEmail())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.contact.person.email.format"));
        }
        if (ValidateUtils.isStringEmpty(companyDetailsDto.getPhone())) {
            throw new ValidationException(messageResource.getEnglishMessage("phone.number.required"));
        }
        if (companyDetailsDto.getPhone().length() > 15) {
            throw new ValidationException(messageResource.getEnglishMessage("phone.number.too.long"));
        }
        if (companyDetailsDto.getPanNumber() != null && companyDetailsDto.getPanNumber().trim().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("pan.number.too.long"));
        }
        if (companyDetailsDto.getTanNumber() != null && companyDetailsDto.getTanNumber().trim().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("tan.number.too.long"));
        }
        if (companyDetailsDto.getCinNumber() != null && companyDetailsDto.getCinNumber().trim().length() > 21) {
            throw new ValidationException(messageResource.getEnglishMessage("cin.number.too.long"));
        }
        if (companyDetailsDto.getGstin() != null && companyDetailsDto.getGstin().trim().length() > 15) {
            throw new ValidationException(messageResource.getEnglishMessage("gstin.number.too.long"));
        }
        if (companyDetailsDto.getPtRegNumber() != null && companyDetailsDto.getPtRegNumber().trim().length() > 50) {
            throw new ValidationException(messageResource.getEnglishMessage("pt.registration.number.too.long"));
        }
        if (companyDetailsDto.getPfRegistrationNumber() != null && companyDetailsDto.getPfRegistrationNumber().trim().length() > 50) {
            throw new ValidationException(messageResource.getEnglishMessage("pf.registration.number.too.long"));
        }
        if (companyDetailsDto.getEsicRegNumber() != null && companyDetailsDto.getEsicRegNumber().trim().length() > 17) {
            throw new ValidationException(messageResource.getEnglishMessage("esic.registration.number.too.long"));
        }
        if (companyDetailsDto.getMsmeRegNo() != null && companyDetailsDto.getMsmeRegNo().trim().length() > 16) {
            throw new ValidationException(messageResource.getEnglishMessage("msme.registration.number.too.long"));
        }
        if (companyDetailsDto.getIecCode() != null && companyDetailsDto.getIecCode().trim().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("iec.code.too.long"));
        }
        if (companyDetailsDto.getPtZone() != null && companyDetailsDto.getPtZone().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("pt.zone.too.long"));
        }
        if (companyDetailsDto.getPfZone() != null && companyDetailsDto.getPfZone().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("pf.zone.too.long"));
        }
        if (companyDetailsDto.getEsicZone() != null && companyDetailsDto.getEsicZone().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("esic.zone.too.long"));
        }
        if (companyDetailsDto.getAddress() != null && companyDetailsDto.getAddress().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("address.too.long"));
        }
        if (companyDetailsDto.getPincode() != null && companyDetailsDto.getPincode().trim().length() > 30) {
            throw new ValidationException(messageResource.getEnglishMessage("pin.code.too.long"));
        }
        if (companyDetailsDto.getEmail() != null && companyDetailsDto.getEmail().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("email.too.long"));
        }
        if (companyDetailsDto.getEmail() != null && !ValidateUtils.isValidEmailFormat(companyDetailsDto.getEmail())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.email.format"));
        }
        if (companyDetailsDto.getWebsite() != null && companyDetailsDto.getWebsite().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("website.url.too.long"));
        }
        if (companyDetailsDto.getTwitter() != null && companyDetailsDto.getTwitter().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("twitter.url.too.long"));
        }
        if (companyDetailsDto.getFacebook() != null && companyDetailsDto.getFacebook().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("facebook.url.too.long"));
        }
        if (companyDetailsDto.getInstagram() != null && companyDetailsDto.getInstagram().trim().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("instagram.url.too.long"));
        }
    }

	   private void validateByRepository(CompanyDetailsDto companyDetailsDto) {
        CompanyDetails byCode = companyDetailsRepository.findByCode(companyDetailsDto.getCode());
        if (!ValidateUtils.isNullObject(byCode)) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
        CompanyDetails byRegisteredName = companyDetailsRepository.findByRegisteredName(companyDetailsDto.getRegisteredName());
        if (!ValidateUtils.isNullObject(byRegisteredName)){
            throw new ValidationException(messageResource.getEnglishMessage("registered.name.exists"));
        }
		CompanyDetails byRegisteredNumber = companyDetailsRepository.findByRegisteredNumber(companyDetailsDto.getRegisteredNumber());
		if (!ValidateUtils.isNullObject(byRegisteredNumber)) {
			throw new ValidationException(messageResource.getEnglishMessage("registered.number.exists"));
		}
        String panNumber = companyDetailsDto.getPanNumber();
        if (panNumber != null) {
            CompanyDetails byPanNumber = companyDetailsRepository.findByPanNumber(panNumber);
            if (byPanNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("pan.number.exists"));
            }
        }
        String tanNumber = companyDetailsDto.getTanNumber();
        if (tanNumber != null) {
            CompanyDetails byTanNumber = companyDetailsRepository.findByTanNumber(tanNumber);
            if (byTanNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("tan.number.exists"));
            }
        }
        String cinNumber = companyDetailsDto.getCinNumber();
        if (cinNumber != null) {
            CompanyDetails byCinNumber = companyDetailsRepository.findByCinNumber(cinNumber);
            if (byCinNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("cin.number.exists"));
            }
        }
        String gstin = companyDetailsDto.getGstin();
        if (gstin != null) {
            CompanyDetails byGstin = companyDetailsRepository.findByGstin(gstin);
            if (byGstin != null) {
                throw new ValidationException(messageResource.getEnglishMessage("gstin.number.exists"));
            }
        }
        String ptRegNumber = companyDetailsDto.getPtRegNumber();
        if (ptRegNumber != null) {
            CompanyDetails byPtRegNumber = companyDetailsRepository.findByPtRegNumber(ptRegNumber);
            if (byPtRegNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("pt.registration.exists"));
            }
        }
        String pfRegNumber = companyDetailsDto.getPfRegistrationNumber();
        if (pfRegNumber != null) {
            CompanyDetails byPfRegistrationNumber = companyDetailsRepository.findByPfRegistrationNumber(pfRegNumber);
            if (byPfRegistrationNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("pf.registration.exists"));
            }
        }
        String esicRegNumber = companyDetailsDto.getEsicRegNumber();
        if (esicRegNumber != null) {
            CompanyDetails byEsicRegNumber = companyDetailsRepository.findByEsicRegNumber(esicRegNumber);
            if (byEsicRegNumber != null) {
                throw new ValidationException(messageResource.getEnglishMessage("esic.registration.number.exists"));
            }
        }
        String msmeRegNo = companyDetailsDto.getMsmeRegNo();
        if (msmeRegNo != null) {
            CompanyDetails byMsmeRegNo = companyDetailsRepository.findByMsmeRegNo(msmeRegNo);
            if (byMsmeRegNo != null) {
                throw new ValidationException(messageResource.getEnglishMessage("msme.registration.number.exists"));
            }
        }
        String iecCode = companyDetailsDto.getIecCode();
        if (iecCode != null) {
            CompanyDetails byIecCode = companyDetailsRepository.findByIecCode(iecCode);
            if (byIecCode != null) {
                throw new ValidationException(messageResource.getEnglishMessage("iec.code.exists"));
            }
        }
	}

    private String normalizeEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // convert null or empty (after trimming) to null
        }
        return value.trim(); // return trimmed value
    }

    private String safeToUpper(String input) {
        return input == null ? null : input.toUpperCase();
    }


    /**
     * Converts a binary file, provided as a Base64 string, into a stored format.
     *
     * @param base64Data the file data in Base64 format
     * @param id the identifier used for storing the file
     * @return the stored file path or representation
     */
    private String storeBase64File(String base64Data, Long id) {
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

            // File name: companyId + extension
            String fileName = id.toString() + "." + fileType;

            // Directory path
            Path folderPath = Paths.get(baseUploadDir, "company", id.toString()); // path
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // Save file
            Path filePath = folderPath.resolve(fileName);
            Files.write(filePath, fileBytes);

            // Relative path for DB

            return Paths.get("company", id.toString(), fileName).toString().replace("\\", "/");

        } catch (IllegalArgumentException e) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.format"));
        } catch (IOException e) {
            throw new RuntimeException(messageResource.getEnglishMessage("failed.to.store.file"), e);
        }
    }



    /**
     * Detects whether the image is JPG or PNG and validates its size (up to 2 MB).
     *
     * @param fileBytes the image file as a byte array
     * @return the image type (e.g., "jpg" or "png")
     * @throws ValidationException if the file type is invalid or exceeds 2 MB
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
     * Deletes a file if it exists at the given relative path.
     *
     * @param relativePath the relative path of the file to delete
     */
    private void deleteFileIfExists(String relativePath) {
        if (relativePath != null && !relativePath.trim().isEmpty()) {
            try {
                Path absolutePath = Paths.get(baseUploadDir, relativePath); // combine base dir + relative path
                Files.deleteIfExists(absolutePath);

                // Now delete parent folder (id folder)
                Path parentFolder = absolutePath.getParent();
                if (parentFolder != null && Files.isDirectory(parentFolder)) {
                    try {
                        Files.deleteIfExists(parentFolder); // works only if empty
                    } catch (IOException e) {
                        log.warn("Could not delete folder (not empty?): {}", parentFolder, e);
                    }
                }
            } catch (IOException e) {
                log.error("Failed to delete file: {}", relativePath, e);
                // Here log is used instead of throw keyword because deleting company record will not fail whole API call
            }
        }
    }

}
