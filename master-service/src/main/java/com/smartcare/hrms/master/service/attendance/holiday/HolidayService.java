package com.smartcare.hrms.master.service.attendance.holiday;

import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.converters.attendance.holiday.HolidayConverter;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayChildDTO;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayDashboardDTO;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayParentDTO;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayChild;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayChildRepository;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayParentRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

	private @Autowired HolidayParentRepository holidayParentRepository;
    private @Autowired HolidayChildRepository holidayChildRepository;
	private @Autowired MessageResource messageResource;


    /**
     * Creates new holiday details.
     *
     * @param holidayParentDTO the holiday details to create
     */
	@Transactional
	public void create(HolidayParentDTO holidayParentDTO) {
		// validate holiday request
		validateRequest(holidayParentDTO);
		// validate code if exist thorw exception
		validateByRepository(holidayParentDTO);
		HolidayParent holidayParent= HolidayConverter.toEntity(holidayParentDTO);
		holidayParentRepository.save(holidayParent);
	}

    /**
     * Updates existing holiday details.
     *
     * @param holidayParentDTO the holiday details to update
     */
	@Transactional
	public void update(HolidayParentDTO holidayParentDTO) {
		if(ValidateUtils.isLongZero(holidayParentDTO.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("holiday.id.is.zero"));
		}
		validateRequest(holidayParentDTO);
		//find holiday details by id
		Optional<HolidayParent> holidayParentOptional= holidayParentRepository.findById(holidayParentDTO.getId());
		if(holidayParentOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
		}
		HolidayParent holidayParent= holidayParentOptional.get();

        // check if code exists
		if(!holidayParent.getCode().equals(holidayParentDTO.getCode())) {
            HolidayParent byCode= holidayParentRepository.findByCode(holidayParentDTO.getCode());
            if(!ValidateUtils.isNullObject(byCode)) {
                throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
            }
		}
        // check if holiday name of child data exists
        for (HolidayChildDTO holidayChildDTO : holidayParentDTO.getHolidayChildDTO()){
            Optional<HolidayChild> byName = holidayChildRepository.findByName(holidayChildDTO.getName());
            if (byName.isPresent()){
               HolidayChild holidayChild = byName.get();
                if (!holidayChild.getId().equals(holidayChildDTO.getId())) {
                    throw new ValidationException(messageResource.getEnglishMessage("holiday.name.exists"));
                }
            }
        }
		convertForUpdate(holidayParent, holidayParentDTO);
		holidayParentRepository.save(holidayParent);
	}

    /**
     * Retrieves holiday details by ID.
     *
     * @param id the ID of the holiday
     * @return the corresponding HolidayParentDTO
     */
	public HolidayParentDTO getById(Long id) {
		Optional<HolidayParent> holidayOptional = holidayParentRepository.findById(id);
		if(holidayOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
		}else {
			return HolidayConverter.toDTO(holidayOptional.get());
		}
	}

    /**
     * Returns a list of holidays filtered by name containing or code, or all if no filter is provided.
     *
     * @param holidayParentDTO DTO containing optional name or code filters.
     * @return list of matching {@link HolidayDashboardDTO} entries.
     */
	public List<HolidayDashboardDTO> getAllOrByNameOrCode(HolidayParentDTO holidayParentDTO){
		List<HolidayDashboardDTO> list= new ArrayList<>();
		/* find holiday details by name */
		if(!ValidateUtils.isStringEmpty(holidayParentDTO.getName())) {
			List<HolidayParent> byName= holidayParentRepository.findByNameContainingIgnoreCase(holidayParentDTO.getName());
			
			if(!CollectionUtils.isEmpty(byName)) {
				for(HolidayParent holidayParent:byName) {
					list.add(HolidayConverter.toDashboardDTO(holidayParent));
				}
			}else {
				throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
			}
		}
		/* find holiday details by code */
		else if(!ValidateUtils.isStringEmpty(holidayParentDTO.getCode())) {
		HolidayParent byCode= holidayParentRepository.findByCode(holidayParentDTO.getCode());
			if(!ValidateUtils.isNullObject(byCode)) {
				list.add(HolidayConverter.toDashboardDTO(byCode));
			}else {
				throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
			}
		}
		else if(holidayParentDTO.getYear() != null) {
			List<HolidayParent> byYear= holidayParentRepository.findByYear(holidayParentDTO.getYear());
			if(!CollectionUtils.isEmpty(byYear)) {
				for(HolidayParent holidayParent:byYear) {
					list.add(HolidayConverter.toDashboardDTO(holidayParent));
				}
				}else {
					throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
				}
			}
		else {
			List<HolidayParent> allHoliday= holidayParentRepository.findAll();
			
			if(!CollectionUtils.isEmpty(allHoliday)) {
				for(HolidayParent holidayParent:allHoliday) {
					list.add(HolidayConverter.toDashboardDTO(holidayParent));
				}
			}else {
				 throw new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found"));
			}
		}
        list.sort(Comparator.comparing(HolidayDashboardDTO::getName, String.CASE_INSENSITIVE_ORDER).reversed());

        return list;
	}


    /**
     * Deletes a holiday entry by its ID.
     *
     * @param id the ID of the holiday to delete.
     */
    @Transactional
	public void delete(Long id) {
		HolidayParent holidayName = holidayParentRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("holiday.data.not.found")));

		holidayParentRepository.delete(holidayName);
	}


	private void convertForUpdate(HolidayParent entity, HolidayParentDTO dto) {
		entity.setName(dto.getName());
		entity.setCode(dto.getCode());
		entity.setYear(dto.getYear());
		entity.setTotalDays(dto.getTotalDays());
		if(!CollectionUtils.isEmpty(dto.getHolidayChildDTO())) {
			List<HolidayChildDTO> newHoliday= dto.getHolidayChildDTO().stream()
					.filter(predicate->predicate.getId()==null).toList();
			
			if(!CollectionUtils.isEmpty(entity.getHolidayChild())) {
				List<HolidayChildDTO> existingHoliday= dto.getHolidayChildDTO().stream()
						.filter(predicate-> predicate.getId() !=null).toList();
				
				entity.getHolidayChild().forEach(obj->{
					Optional<HolidayChildDTO> first= existingHoliday.stream().filter(predicate->predicate.getId().equals(obj.getId())).findFirst();
				if(first.isPresent()) {
					HolidayChildDTO holidayChildDTO = first.get();
					obj.setName(holidayChildDTO.getName());
					obj.setDate(DateHelper.convertUserDateToLocalDate(holidayChildDTO.getDate()));
                    obj.setChecked(holidayChildDTO.isChecked());
				}else {
					obj.setRemove(true);
				}
				});
  
				if(!CollectionUtils.isEmpty(newHoliday)) {
					for(HolidayChildDTO holidayChildDTO:newHoliday) {
						entity.getHolidayChild().add(HolidayConverter.toEntity(holidayChildDTO, entity));
					}
				}
				entity.getHolidayChild().removeIf(HolidayChild::isRemove);
			}else {
			List<HolidayChild>	newHolidayRule= new ArrayList<>();
			for(HolidayChildDTO holidayChildDTO:newHoliday) {
				newHolidayRule.add(HolidayConverter.toEntity(holidayChildDTO, entity));
			}
			entity.setHolidayChild(newHolidayRule);
			}
		}
	}


	private void validateRequest(HolidayParentDTO holidayParentDTO) {
		if(ValidateUtils.isNullObject(holidayParentDTO)) {
			throw new ValidationException(messageResource.getEnglishMessage("holiday.data.required"));
		}
		if(ValidateUtils.isStringEmpty(holidayParentDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("name.required"));
		}
		if (holidayParentDTO.getName().length() >= 255) {
	        throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
	    }
		if(!ValidateUtils.isValidNameFormat(holidayParentDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
		}
		if(!ValidateUtils.isValidCodeFormat(holidayParentDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
		}
		if(ValidateUtils.isStringEmpty(holidayParentDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("code.required"));
		}
	
		if (holidayParentDTO.getCode().length() >= 30) {
	        throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
	    }
		if(ValidateUtils.isNullObject(holidayParentDTO.getYear())) {
			throw new ValidationException(messageResource.getEnglishMessage("year.required"));
		}

		if(!CollectionUtils.isEmpty(holidayParentDTO.getHolidayChildDTO())) {
			for(HolidayChildDTO dto : holidayParentDTO.getHolidayChildDTO()) {
				if(ValidateUtils.isStringEmpty(dto.getName())) {
					throw new ValidationException(messageResource.getEnglishMessage("name.required"));
				}
				if(ValidateUtils.isNullObject(dto.getDate())) {
					throw new ValidationException(messageResource.getEnglishMessage("date.required"));
				}
			}


		} else {
			throw new ValidationException(messageResource.getEnglishMessage("holiday.detail.required"));
		}
		}


	private void validateByRepository(HolidayParentDTO holidayParentDTO) {
		HolidayParent byCode= holidayParentRepository.findByCode(holidayParentDTO.getCode());
		if(!ValidateUtils.isNullObject(byCode)) {
			throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
		}
        if (!CollectionUtils.isEmpty(holidayParentDTO.getHolidayChildDTO())){
            for (HolidayChildDTO holidayChildDTO : holidayParentDTO.getHolidayChildDTO()){
                Optional<HolidayChild> byName = holidayChildRepository.findByName(holidayChildDTO.getName());
                if (byName.isPresent()){
                   throw new ValidationException(messageResource.getEnglishMessage("holiday.name.exists"));
                }
            }
        }

	}
}
