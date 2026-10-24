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

import com.smartcare.hrms.common.dto.master.employment.SubWorkLocationDto;
import com.smartcare.hrms.common.dto.master.employment.WorkLocationDto;
import com.smartcare.hrms.common.entity.master.employment.SubWorkLocation;
import com.smartcare.hrms.common.entity.master.employment.WorkLocation;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.SubWorkLocationRepository;
import com.smartcare.hrms.common.repository.master.employment.WorkLocationRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class SubWorkLocationService {


	@Autowired
	private SubWorkLocationRepository subWorkLocationRepository;

	@Autowired
	private WorkLocationRepository workLocationRepository;

    @Autowired
    private MessageResource messageResource;


    /**
     * Creates a new sub work location and checks if the parent work location exists.
     *
     * @param subWorkLocationDto the sub work location details to create
     * @return the created SubWorkLocationDto
     * @throws ValidationException if the parent work location does not exist or data is invalid
     */
    @Transactional
	public SubWorkLocationDto create(SubWorkLocationDto subWorkLocationDto) {
		
		// Validate designation request 
		validateSubWorkLocationDto(subWorkLocationDto);
		validateByRepository(subWorkLocationDto);

		WorkLocation workLocation = workLocationRepository.findById(subWorkLocationDto.getWorkLocation().getId())
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found")));

        SubWorkLocation subWorkLocation = SubWorkLocation.builder()
                .name(subWorkLocationDto.getName())
                .code(subWorkLocationDto.getCode())
                .workLocation(workLocation)
                .build();

		SubWorkLocation save = subWorkLocationRepository.save(subWorkLocation);
		return toDto(save);
	}

	
	/**
     * Fetches sub work locations by code, by name (containing), or all if no filters are provided.
     *
     * @param subWorkLocationDto the search criteria (code or name)
     * @return list of matching SubWorkLocationDto objects
     */
	public List<SubWorkLocationDto> getByNameOrCodeOrAll(SubWorkLocationDto subWorkLocationDto) {
		List<SubWorkLocationDto> dtoList = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(subWorkLocationDto.getName())) {
			List<SubWorkLocation> byName = subWorkLocationRepository.findByNameContainingIgnoreCase(subWorkLocationDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (SubWorkLocation subWorkLocation : byName) {
					dtoList.add(toDto(subWorkLocation));
				}
			}
		} else if (!ValidateUtils.isStringEmpty(subWorkLocationDto.getCode())) {
			SubWorkLocation byCode = subWorkLocationRepository.findByCode(subWorkLocationDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				dtoList.add(toDto(byCode));
			}
		} else {
			List<SubWorkLocation> all = subWorkLocationRepository.findAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (SubWorkLocation subWorkLocation : all) {
					dtoList.add(toDto(subWorkLocation));
				}
			}
		}
		if (dtoList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("sub.work.location.data.not.found"));
		}
        dtoList.sort(Comparator.comparing(SubWorkLocationDto::getName).reversed());
		return dtoList;
	}


    /**
     * Deletes a sub work location by its ID.
     *
     * @param id the ID of the sub work location to delete
     */
    @Transactional
    public void delete(Long id) {
        SubWorkLocation subWorkLocation = subWorkLocationRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("sub.work.location.data.not.found")));
        subWorkLocationRepository.delete(subWorkLocation);
    }


    /**
     * Updates an existing sub work location.
     *
     * @param subWorkLocationDto the updated sub work location details
     * @return the updated SubWorkLocationDto
     */
    @Transactional
	public SubWorkLocationDto update(SubWorkLocationDto subWorkLocationDto) {

		if (ValidateUtils.isLongZero(subWorkLocationDto.getId())) {
			  throw	new ValidationException(messageResource.getEnglishMessage("sub.work.location.details.required"));
		}
        validateSubWorkLocationDto(subWorkLocationDto);
        
        Optional<SubWorkLocation> subLocationOptional = subWorkLocationRepository.findById(subWorkLocationDto.getId());
		if (subLocationOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("sub.work.location.data.not.found"));
		}
        SubWorkLocation subWorkLocation = subLocationOptional.get();
		
        if (!subWorkLocation.getCode().equals(subWorkLocationDto.getCode())) {
			validateByRepository(subWorkLocationDto);
		}
        convertForUpdate(subWorkLocation, subWorkLocationDto);
        SubWorkLocation updated = subWorkLocationRepository.save(subWorkLocation);
        return toDto(updated);
	}

    /**
     * Retrieves a sub work location by its ID.
     *
     * @param id the ID of the sub work location
     * @return the corresponding SubWorkLocationDto
     */
	public SubWorkLocationDto getById(Long id){
        Optional<SubWorkLocation> subWorkLocationOptional = subWorkLocationRepository.findById(id);
        if(subWorkLocationOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("sub.work.location.data.not.found"));
        }else{
            return toDto(subWorkLocationOptional.get());
        }
    }


    private SubWorkLocationDto toDto(SubWorkLocation subWorkLocation) {
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


    private void convertForUpdate(SubWorkLocation entity, SubWorkLocationDto dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        if (!ValidateUtils.isNullObject(dto.getWorkLocation()) && !ValidateUtils.isLongZero(dto.getWorkLocation().getId())) {
            WorkLocation workLocation = workLocationRepository.findById(dto.getWorkLocation().getId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("work.location.data.not.found")));
            entity.setWorkLocation(workLocation);
        }
    }


    private void validateSubWorkLocationDto(SubWorkLocationDto dto) {
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
        if (ValidateUtils.isLongZero(dto.getWorkLocation().getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("work.location.null"));
        }
    }

    private void validateByRepository(SubWorkLocationDto subWorkLocationDto) {
        SubWorkLocation byCode = subWorkLocationRepository.findByCode(subWorkLocationDto.getCode());
        if (!ValidateUtils.isNullObject(byCode)) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }
}
