package com.smartcare.hrms.master.service.attendance.weekOff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.enumeration.DayOfWeek;
import com.smartcare.hrms.common.enumeration.WeekNo;
import com.smartcare.hrms.common.utility.EnumConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.converters.attendance.weekOff.WeekOffConverter;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffDetailDto;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffParentDto;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffTemplateDto;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffDetail;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.attendance.weekOff.WeekOffParentRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class WeekOffParentService {

	@Autowired
	private WeekOffParentRepository weekOffParentRepository;

    @Autowired
	private MessageResource messageResource;

    /**
     * Creates a new week off entry based on the provided DTO.
     *
     * @param requestDTO the {@link WeekOffParentDto} containing week off details.
     * @throws ValidationException if the request DTO is invalid.
     */
	@Transactional
    public void create(WeekOffParentDto requestDTO){
     /* Validate week off request */
        validateRequest(requestDTO);
        validateByRepository(requestDTO);
        WeekOffParent entity = WeekOffConverter.toEntity(requestDTO);
        weekOffParentRepository.save(entity);
    }

    /**
     * Updates an existing WeekOffParent entity.
     *
     * @param requestDTO the DTO with updated data; must have a valid non-zero ID
     * @throws ValidationException if the ID is zero or null
     */
	@Transactional
    public void update(WeekOffParentDto requestDTO){
        if(ValidateUtils.isLongZero(requestDTO.getId())){
            throw new ValidationException(messageResource.getEnglishMessage("week-off.id.is.zero"));
        }
        validateRequest(requestDTO);

        Optional<WeekOffParent> weekOffOptional = weekOffParentRepository.findById(requestDTO.getId());
        if(weekOffOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("week-off.data.not.found"));
        }
        WeekOffParent weekOff = weekOffOptional.get();
        if(!weekOff.getCode().equals(requestDTO.getCode())){
            validateByRepository(requestDTO);
        }

        /** update entity with dto **/
        convertForUpdate(weekOff, requestDTO);
        weekOffParentRepository.save(weekOff);
    }

    /**
     * Retrieves a WeekOffParent entity by its ID.
     *
     * @param id the ID of the WeekOffParent to retrieve
     * @return the corresponding WeekOffParentDto if found, otherwise null or throws exception
     */
	public WeekOffParentDto getById(Long id){
        Optional<WeekOffParent> weekOffOptional = weekOffParentRepository.findById(id);
        if(weekOffOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("week-off.data.not.found"));
        }else{
            return WeekOffConverter.toDto(weekOffOptional.get());
        }
    }

    /**
     * Retrieves a list of WeekOffTemplateDto filtered by name or code.
     * If no filters are provided in the DTO, returns all entries.
     *
     * @param weekOffParentDto DTO containing optional filter criteria (name, code)
     * @return list of matching WeekOffTemplateDto objects
     */
	public List<WeekOffTemplateDto>  getAllOrByNameOrCode(WeekOffParentDto weekOffParentDto){
        List<WeekOffTemplateDto> list = new ArrayList<>();
        if(!ValidateUtils.isStringEmpty(weekOffParentDto.getName())){
            List<WeekOffParent> byNameContainingIgnoreCase = weekOffParentRepository.findByNameContainingIgnoreCase(weekOffParentDto.getName());
            if(!CollectionUtils.isEmpty(byNameContainingIgnoreCase)){
                for (WeekOffParent weekOff : byNameContainingIgnoreCase) {
                    list.add(WeekOffConverter.convertToDashboardDto(weekOff));
                }
            }else{
                throw new ValidationException("Record not found");
            }
        }else if(!ValidateUtils.isStringEmpty(weekOffParentDto.getCode())){
            WeekOffParent byCode = weekOffParentRepository.findByCode(weekOffParentDto.getCode());
            if(!ValidateUtils.isNullObject(byCode)){
                list.add(WeekOffConverter.convertToDashboardDto(byCode));
            }else{
                throw new ValidationException(messageResource.getEnglishMessage("week-off.data.not.found"));
            }
        }else{
            List<WeekOffParent> all = weekOffParentRepository.findAll();
            if(!CollectionUtils.isEmpty(all)){
                for (WeekOffParent weekOff : all) {
                    list.add(WeekOffConverter.convertToDashboardDto(weekOff));
                }
            }
        }
        list.sort(Comparator.comparing(WeekOffTemplateDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        return list;
    }

    /**
     * Deletes the WeekOffParent entity with the specified ID.
     *
     * @param id the ID of the WeekOffParent to delete
     * @throws ValidationException if no entity with the given ID is found
     */
    @Transactional
	public void delete(Long id) {
		WeekOffParent weekOff = weekOffParentRepository.findById(id)
				.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("week-off.data.not.found")));

		weekOffParentRepository.delete(weekOff);
	}


	 private void validateRequest(WeekOffParentDto requestDTO){
	        if (ValidateUtils.isNullObject(requestDTO)) {
	            throw new ValidationException(messageResource.getEnglishMessage("week-off.detail.required"));
	        }
	        if (ValidateUtils.isStringEmpty(requestDTO.getName())) {
	            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
	        }
	        if (requestDTO.getName().length() >= 30) {
				throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
			}	
	        if(!ValidateUtils.isValidNameFormat(requestDTO.getName())) {
	        	throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
	        }
	        if (ValidateUtils.isStringEmpty(requestDTO.getCode())) {
	            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
	        }
	        if (!ValidateUtils.isValidCodeFormat(requestDTO.getCode())) {
	            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
	        }
	        if (requestDTO.getCode().length() >= 30) {
				throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
			}
	        if (CollectionUtils.isEmpty(requestDTO.getWeekOffDetail())) {
                throw new ValidationException(messageResource.getEnglishMessage("week-off.detail.required"));
            }
           for (WeekOffDetailDto weekOffDetailDto : requestDTO.getWeekOffDetail()) {
             if (ValidateUtils.isNullObject(weekOffDetailDto.getDay())) {
                 throw new ValidationException(messageResource.getEnglishMessage("week.day.required"));
             }
             if (ValidateUtils.isNullObject(weekOffDetailDto.getWeekNo())) {
                 throw new ValidationException(messageResource.getEnglishMessage("week.number.required"));
             }
         }
	 }


	private void validateByRepository(WeekOffParentDto weekOffParentDto) {
		WeekOffParent byCode = weekOffParentRepository.findByCode(weekOffParentDto.getCode());
		if (!ValidateUtils.isNullObject(byCode)) {
			throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
		}
	}

	private  void convertForUpdate(WeekOffParent entity, WeekOffParentDto dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
         
        //check if dto collection is empty
        if (!ValidateUtils.isNullObject(dto.getWeekOffDetail())) {
            //new week off to be added
            List<WeekOffDetailDto> newWeekOffList = dto.getWeekOffDetail().stream()
                    .filter(predicate -> predicate.getId() == null).toList();

            if (!CollectionUtils.isEmpty(entity.getWeekOffDetail())) {
            	
                //update existing week off
                List<WeekOffDetailDto> existingWeekOffList = dto.getWeekOffDetail().stream()
                        .filter(predicate -> predicate.getId() != null).toList();

                entity.getWeekOffDetail().forEach(obj -> {
                    Optional<WeekOffDetailDto> first = existingWeekOffList.stream().filter(pre -> pre.getId().equals(obj.getId())).findFirst();
                    if (first.isPresent()) {
                        WeekOffDetailDto weekOffDetailDto = first.get();
                        obj.setDay(EnumConverter.toEnum(weekOffDetailDto.getDay(), DayOfWeek.class));
                        obj.setWeekNo(EnumConverter.toEnum(weekOffDetailDto.getWeekNo(), WeekNo.class));
                        
                    }else{
                        obj.setRemove(true);
                    }
                });
                //new week off
                if (!CollectionUtils.isEmpty(newWeekOffList)) {
                    for (WeekOffDetailDto weekOffDetailDto : newWeekOffList) {
                        entity.getWeekOffDetail().add(WeekOffConverter.toEntity(weekOffDetailDto, entity));
                    }
                }
                //delete existing
                entity.getWeekOffDetail().removeIf(WeekOffDetail::isRemove);
            }else {
                List<WeekOffDetail> newWeekOffDetail =  new ArrayList<>();
                for (WeekOffDetailDto weekOffDetailDto : newWeekOffList) {
                    newWeekOffDetail.add(WeekOffConverter.toEntity(weekOffDetailDto, entity));
                }
                entity.setWeekOffDetail(newWeekOffDetail);
            }
        }
    }
}
