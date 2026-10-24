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

import com.smartcare.hrms.common.dto.master.employment.WorkLocationDto;
import com.smartcare.hrms.common.entity.master.employment.WorkLocation;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.WorkLocationRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class WorkLocationService {

	@Autowired
	private WorkLocationRepository workLocationRepository;

    @Autowired
    private MessageResource messageResource;

    /**
     * Creates a new work location.
     *
     * @param workLocationDto the work location details to create
     * @return the created WorkLocationDto
     */
    @Transactional
	public WorkLocationDto create(WorkLocationDto workLocationDto) {
		
		// Validate work location request 		
		validateWorkLocationDto(workLocationDto);
		validateByRepository(workLocationDto);

		WorkLocation workLocation = WorkLocation.builder()
                .name(workLocationDto.getName())
                .code(workLocationDto.getCode())
                .build();

		WorkLocation save = workLocationRepository.save(workLocation);
		return toDto(save);		
	}

    /**
     * Fetches work locations by code, by name (containing), or all if no filters are provided.
     *
     * @param workLocationDto the search criteria (code or name)
     * @return list of matching WorkLocationDto objects
     */
	public List<WorkLocationDto> getByNameOrCodeOrAll(WorkLocationDto workLocationDto) {
		List<WorkLocationDto> dtoList = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(workLocationDto.getName())) {
			List<WorkLocation> byName = workLocationRepository.findByNameContainingIgnoreCase(workLocationDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (WorkLocation workLocation : byName) {
					dtoList.add(toDto(workLocation));
				}

			}
		} else if (!ValidateUtils.isStringEmpty(workLocationDto.getCode())) {
			WorkLocation byCode = workLocationRepository.findByCode(workLocationDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				dtoList.add(toDto(byCode));
			}
		} else {
			List<WorkLocation> all = workLocationRepository.findAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (WorkLocation workLocation : all) {
					dtoList.add(toDto(workLocation));
				}
			}
		}
		if (dtoList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found"));
		}
        dtoList.sort(Comparator.comparing(WorkLocationDto::getName,String.CASE_INSENSITIVE_ORDER).reversed());
		return dtoList;
	}


    /**
     * Deletes a work location by its ID.
     *
     * @param id the ID of the work location to delete
     */
    @Transactional
	public void delete(Long id) {
		WorkLocation workLocation = workLocationRepository.findById(id)
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found")));
		workLocationRepository.delete(workLocation);
	}

    /**
     * Updates an existing work location.
     *
     * @param workLocationDto the updated work location details
     * @return the updated WorkLocationDto
     */
    @Transactional
	public WorkLocationDto update(WorkLocationDto workLocationDto) {
		if (ValidateUtils.isLongZero(workLocationDto.getId())) {
			  throw	new ValidationException(messageResource.getEnglishMessage("work.location.details.required"));
			}
			validateWorkLocationDto(workLocationDto);

			Optional<WorkLocation> wLocationOptional = workLocationRepository.findById(workLocationDto.getId());
			if (wLocationOptional.isEmpty()) {
				throw new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found"));
			}
	        WorkLocation workLocation = wLocationOptional.get();
			
			if (!workLocation.getCode().equals(workLocationDto.getCode())) {
				validateByRepository(workLocationDto);
			}

			convertForUpdate(workLocation, workLocationDto);
			WorkLocation updated = workLocationRepository.save(workLocation);
			return toDto(updated);
	}


    /**
     * Retrieves a work location by its ID.
     *
     * @param id the ID of the work location
     * @return the corresponding WorkLocationDto
     */
    public WorkLocationDto getById(Long id) {
        WorkLocation workLocation = workLocationRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found")));
        return toDto(workLocation);
    }


    private WorkLocationDto toDto(WorkLocation workLocation) {
        if (workLocation == null) {
            return null;
        }
        return WorkLocationDto.builder()
                .id(workLocation.getId())
                .name(workLocation.getName())
                .code(workLocation.getCode())
                .build();
    }


    private void convertForUpdate(WorkLocation entity, WorkLocationDto dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
    }


    private void validateWorkLocationDto(WorkLocationDto dto) {
        if (ValidateUtils.isStringEmpty(dto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
        }
        if (dto.getName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(dto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
        if (ValidateUtils.isStringEmpty(dto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if (dto.getCode().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
        }
        if (!ValidateUtils.isValidCodeFormat(dto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
    }

    private void validateByRepository(WorkLocationDto workLocationDto) {
        WorkLocation byCode = workLocationRepository.findByCode(workLocationDto.getCode());
        if (!ValidateUtils.isNullObject(byCode)) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }
}