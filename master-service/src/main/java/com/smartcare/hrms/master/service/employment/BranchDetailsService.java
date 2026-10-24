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

import com.smartcare.hrms.common.bean.master.employment.BranchDetailsBean;
import com.smartcare.hrms.common.dto.master.employment.BranchDetailsDto;
import com.smartcare.hrms.common.entity.master.employment.BranchDetails;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.BranchDetailsRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class BranchDetailsService {


	@Autowired
	private BranchDetailsRepository branchDetailsRepository;

    @Autowired
    private MessageResource messageResource;


    /**
     * Retrieves a branch details entry by its ID.
     *
     * @param id the ID of the branch
     * @return the corresponding BranchDetailsDto
     */
    public BranchDetailsDto getById(Long id) {
        BranchDetailsBean branchDetailsBean = branchDetailsRepository.getAllById(id);
        if (!ValidateUtils.isNullObject(branchDetailsBean)) {
            return convertBeanToDTO(branchDetailsBean);
        } else {
            throw new ValidationException(messageResource.getEnglishMessage("branch.data.not.found"));
        }
    }


    /**
     * Fetches branch details by code, by name (containing), or all if no filters are provided.
     *
     * @param branchDetailsRequestDto the search criteria (code or name)
     * @return list of matching BranchDetailsDto objects
     */
	public List<BranchDetailsDto> getByNameOrCodeOrAll(BranchDetailsDto branchDetailsRequestDto) {
		List<BranchDetailsDto> allBranch = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(branchDetailsRequestDto.getCode())) {
			BranchDetailsBean byCode = branchDetailsRepository.getByCode(branchDetailsRequestDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				allBranch.add(convertBeanToDTO(byCode));

			}
		} else if (!ValidateUtils.isStringEmpty(branchDetailsRequestDto.getName())) {
			List<BranchDetailsBean> byName = branchDetailsRepository.getByName(branchDetailsRequestDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (BranchDetailsBean branchDetailsBean : byName) {
					allBranch.add(convertBeanToDTO(branchDetailsBean));
				}
			}
		} else {
			List<BranchDetailsBean> all = branchDetailsRepository.getAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (BranchDetailsBean branchDetailsBean : all) {
					allBranch.add(convertBeanToDTO(branchDetailsBean));
				}
			}
		}
		if (allBranch.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("branch.data.not.found"));
		}
        allBranch.sort(Comparator.comparing(BranchDetailsDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());

        return allBranch;
	}



    /**
     * Creates a new branch along with its country, state, and city details.
     *
     * @param branchDetailsDto the branch details to create
     * @return the created BranchDetailsDto
     */
    @Transactional
	public BranchDetailsDto create(BranchDetailsDto branchDetailsDto) {

		// Validate branch details
		validateBranchDetailsDto(branchDetailsDto);
		validateByRepository(branchDetailsDto);
		
		BranchDetails branchDetails = BranchDetails.builder()
                .name(branchDetailsDto.getName())
                .code(branchDetailsDto.getCode())
                .address(branchDetailsDto.getAddress())
                .countryId(branchDetailsDto.getCountryId())
                .stateId(branchDetailsDto.getStateId())
                .cityId(branchDetailsDto.getCityId())
                .build();

		BranchDetails save = branchDetailsRepository.save(branchDetails);
		return toDto(save);
	}


    /**
     * Deletes a branch by its ID.
     *
     * @param id the ID of the branch to delete
     */
    @Transactional
	public void delete(Long id) {
		BranchDetails branchDetails = branchDetailsRepository.findById(id)
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("branch.data.not.found")));

		branchDetailsRepository.delete(branchDetails);
	}


    /**
     * Updates an existing branch along with its country, state, and city details.
     *
     * @param branchDetailsDto the updated branch details
     * @return the updated BranchDetailsDto
     */
    @Transactional
	public BranchDetailsDto update(BranchDetailsDto branchDetailsDto) {

		if (ValidateUtils.isLongZero(branchDetailsDto.getId())) {
			  throw	new ValidationException(messageResource.getEnglishMessage("branch.details.required"));
		}
		validateBranchDetailsDto(branchDetailsDto);
		
		Optional<BranchDetails> branchOptional = branchDetailsRepository.findById(branchDetailsDto.getId());
		if (branchOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("branch.data.not.found"));
		}
		
		BranchDetails branchDetails = branchOptional.get();
		
		if (!branchDetails.getCode().equals(branchDetailsDto.getCode())) {
			validateByRepository(branchDetailsDto);
		}
		convertForUpdate(branchDetails, branchDetailsDto);
		BranchDetails update = branchDetailsRepository.save(branchDetails);
		return toDto(update);
		
	}


    private BranchDetailsDto convertBeanToDTO(BranchDetailsBean branchDetailsBean) {
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
                .cityName(branchDetailsBean.getCityName())
                .build();
    }

    private BranchDetailsDto toDto(BranchDetails branchDetails) {
        if (branchDetails == null) {
            return null;
        }
        return BranchDetailsDto.builder()
                .id(branchDetails.getId())
                .name(branchDetails.getName())
                .code(branchDetails.getCode())
                .address(branchDetails.getAddress())
                .countryId(branchDetails.getCountryId())
                .stateId(branchDetails.getStateId())
                .cityId(branchDetails.getCityId())
                .build();
    }

	private void convertForUpdate(BranchDetails entity, BranchDetailsDto dto) {
		entity.setName(dto.getName());
		entity.setCode(dto.getCode());
		entity.setAddress(dto.getAddress());
		entity.setCountryId(dto.getCountryId());
		entity.setStateId(dto.getStateId());
		entity.setCityId(dto.getCityId());	
		}


	private void validateBranchDetailsDto(BranchDetailsDto dto) {
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
	    if (ValidateUtils.isStringEmpty(dto.getAddress())) {
	        throw new ValidationException(messageResource.getEnglishMessage("branch.address.required"));
	    }
	    if (ValidateUtils.isZero(dto.getCountryId())) {
			throw new ValidationException(messageResource.getEnglishMessage("country.id.required"));
		}
	    if (ValidateUtils.isZero(dto.getStateId())) {
			throw new ValidationException(messageResource.getEnglishMessage("state.id.required"));
		}
	    if (ValidateUtils.isZero(dto.getCityId())) {
			throw new ValidationException(messageResource.getEnglishMessage("city.id.required"));
		}
	}

	private void validateByRepository(BranchDetailsDto branchDetailsDto){
        BranchDetails byCode = branchDetailsRepository.findByCode(branchDetailsDto.getCode());
        if(!ValidateUtils.isNullObject(byCode)){
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }

}
