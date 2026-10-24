package com.smartcare.hrms.master.service.employment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.master.config.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.smartcare.hrms.common.dto.master.employment.DesignationDto;
import com.smartcare.hrms.common.entity.master.employment.Designation;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.DesignationRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;


@Service
public class DesignationService {
	
	@Autowired
	private DesignationRepository designationRepository;

    @Autowired
    private MessageResource messageResource;

    /**
     * Creates a new designation.
     *
     * @param designationDto the DTO containing designation details
     * @return the created DesignationDto with persisted data
     */
    @Transactional
	public DesignationDto create(DesignationDto designationDto) {
		
		// Validate designation request 
		validateDesignationDto(designationDto);
        validateByRepository(designationDto);

        Designation designation = Designation.builder()
                .name(designationDto.getName())
                .code(designationDto.getCode())
                .build();

		Designation save = designationRepository.save(designation);
		return toDto(save);
	}


    /**
     * Fetches designations by code, by name (containing), or all if no filters are provided.
     *
     * @param designationDto the search criteria (code or name)
     * @return list of matching designations
     */
	public List<DesignationDto> getByNameOrCodeOrAll(DesignationDto designationDto) {
		List<DesignationDto> dtoList = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(designationDto.getName())) {
			List<Designation> byName = designationRepository.findByNameContainingIgnoreCase(designationDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (Designation designation : byName) {
					dtoList.add(toDto(designation));
				}

			}
		} else if (!ValidateUtils.isStringEmpty(designationDto.getCode())) {
			Designation byCode = designationRepository.findByCode(designationDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				dtoList.add(toDto(byCode));
			}
		} else {
			List<Designation> all = designationRepository.findAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (Designation designation : all) {
					dtoList.add(toDto(designation));
				}
			}
		}
		if (dtoList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("designation.data.not.found"));
		}
        dtoList.sort(Comparator.comparing(DesignationDto::getName).reversed());
		return dtoList;
	}


    /**
     * Deletes a designation by its ID.
     *
     * @param id the ID of the designation to delete
     */
    @Transactional
	public void delete(Long id) {
		Designation designation = designationRepository.findById(id)
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("designation.data.not.found")));
		designationRepository.delete(designation);
		
	}


    /**
     * Updates an existing designation.
     *
     * @param designationDto the updated designation details
     * @return the updated DesignationDto
     */
    @Transactional
	public DesignationDto update(DesignationDto designationDto) {
		if (ValidateUtils.isLongZero(designationDto.getId())) {
		  throw	new ValidationException(messageResource.getEnglishMessage("designation.details.required"));
		}
		validateDesignationDto(designationDto);

		Optional<Designation> designationOptional = designationRepository.findById(designationDto.getId());
		if (designationOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("designation.data.not.found"));
		}
        Designation designation = designationOptional.get();
		
		if (!designation.getCode().equals(designationDto.getCode())) {
			validateByRepository(designationDto);
		}

		convertForUpdate(designation, designationDto);
		Designation updated = designationRepository.save(designation);
		return toDto(updated);
	}


    /**
     * Retrieves a designation by its ID.
     *
     * @param id the ID of the designation
     * @return the corresponding DesignationDto
     */
	public DesignationDto getById(Long id) {
		Designation designation = designationRepository.findById(id)
				.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("designation.data.not.found")));
		return toDto(designation);
	}


	private DesignationDto toDto(Designation designation) {
        if (designation == null) {
            return null;
        }
        return DesignationDto.builder()
                .id(designation.getId())
                .name(designation.getName())
                .code(designation.getCode())
                .build();
    }

	private void convertForUpdate(Designation entity, DesignationDto dto) {
	    entity.setName(dto.getName());
	    entity.setCode(dto.getCode());
	}


	private void validateDesignationDto(DesignationDto dto) {
	    if (ValidateUtils.isStringEmpty(dto.getName())) {
	        throw new ValidationException(messageResource.getEnglishMessage("name.required"));
	    }
	    if (dto.getName().length() > 255) {
	        throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
	    }
        if (!ValidateUtils.isValidNameFormat(dto.getName())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
	    if (ValidateUtils.isStringEmpty(dto.getCode())) {
	        throw new ValidationException(messageResource.getEnglishMessage("code.required"));
	    }
	    if (dto.getCode().length() > 10) {
			throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
		}
        if (!ValidateUtils.isValidCodeFormat(dto.getCode())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
	}

	private void validateByRepository(DesignationDto designationDto){
        Designation byCode = designationRepository.findByCode(designationDto.getCode());
        if(!ValidateUtils.isNullObject(byCode)){
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }

}
