package com.smartcare.hrms.master.service.attendance.leave;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.globalexception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.converters.attendance.leave.LeaveConverter;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDashboardDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDtlsDTO;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplateDtls;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveTemplateRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;



@Service
public class LeaveTemplateService {
 
	@Autowired
	private LeaveTemplateRepository leaveTemplateRepository;
	private @Autowired MessageResource messageResource;

    /**
     * Creates a new leave template based on the provided DTO.
     *
     * @param leaveTemplateDto the {@link LeaveTemplateDTO} containing leave template details.
     */
	@Transactional
	public void create(LeaveTemplateDTO leaveTemplateDto) {
		// validate leave template request
		validateRequest(leaveTemplateDto);
		//validate by code if exist throw exception
		validateByRepository(leaveTemplateDto);
		//create new leave template data
		LeaveTemplate leaveTemplate= LeaveConverter.toEntity(leaveTemplateDto);
		leaveTemplateRepository.save(leaveTemplate);
	}

    /**
     * Updates an existing leave template with the provided details.
     *
     * @param leaveTemplateDTO the {@link LeaveTemplateDTO} containing updated leave template information.
     */
	@Transactional
	public void update(LeaveTemplateDTO leaveTemplateDTO) {
		if(ValidateUtils.isLongZero(leaveTemplateDTO.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.template.id.is.zero"));
		}
		validateRequest(leaveTemplateDTO);
		// find leave template data by id
		Optional<LeaveTemplate> leaveTemplateOptional= leaveTemplateRepository.findById(leaveTemplateDTO.getId());
		if(leaveTemplateOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found"));
		}
		LeaveTemplate leaveTemplate = leaveTemplateOptional.get();
        if (!leaveTemplate.getCode().equals(leaveTemplateDTO.getCode())) {
            validateByRepository(leaveTemplateDTO);
        }
	
		//update leave template data
		convertForUpdate(leaveTemplate, leaveTemplateDTO);
		leaveTemplateRepository.save(leaveTemplate);
	}


    /**
     * Retrieves leave template data by its ID.
     *
     * @param id the ID of the leave template to fetch.
     * @return the corresponding {@link LeaveTemplateDTO} if found.
     */
	public LeaveTemplateDTO  getById(Long id) {
		// find leave template data by id
		Optional<LeaveTemplate> leaveTemplateOptional = leaveTemplateRepository.findById(id);
		if(leaveTemplateOptional.isEmpty()) {
		throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found"));
	}else {
		return LeaveConverter.toDTO(leaveTemplateOptional.get());
	}
}

    /**
     * Retrieves a list of leave templates filtered by name or code.
     * Returns all leave templates if no filters are provided.
     *
     * @param leaveTemplateDTO the {@link LeaveTemplateDTO} containing optional name or code filters.
     * @return list of matching {@link LeaveTemplateDashboardDTO} entries.
     */
	 public List<LeaveTemplateDashboardDTO> getAllOrByNameOrCode(LeaveTemplateDTO leaveTemplateDTO) {
	        List<LeaveTemplateDashboardDTO> list = new ArrayList<>();

	     //find data by name 
	        if (!ValidateUtils.isStringEmpty(leaveTemplateDTO.getName())) {
	            List<LeaveTemplate> byNameList = leaveTemplateRepository
	                    .findByNameContainingIgnoreCase(leaveTemplateDTO.getName());

	            if (!CollectionUtils.isEmpty(byNameList)) {
	                for (LeaveTemplate template : byNameList) {
	                	list.add(LeaveConverter.toDashboardDTO(template));
	                }
	            } else {
	                throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found") );
	            }
	        
	        }//find data by code 
	        else if (!ValidateUtils.isStringEmpty(leaveTemplateDTO.getCode())) {
	            LeaveTemplate byCode = leaveTemplateRepository.findByCode(leaveTemplateDTO.getCode());

	            if (!ValidateUtils.isNullObject(byCode)) {
	            	list.add(LeaveConverter.toDashboardDTO(byCode));
	            } else {
	                throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found") );
	            }

	        }
	        //find all leave template data 
	        else {
	            List<LeaveTemplate> allTemplates = leaveTemplateRepository.findAll();

	            if (!CollectionUtils.isEmpty(allTemplates)) {
	                for (LeaveTemplate template : allTemplates) {
	                	list.add(LeaveConverter.toDashboardDTO(template));
	                }
	            } else {
	                throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found"));
	            }
	        }
             list.sort(Comparator.comparing(LeaveTemplateDashboardDTO::getName, String.CASE_INSENSITIVE_ORDER).reversed());
	        return list;
	    }

    /**
     * Deletes leave template data by its ID.
     *
     * @param id the ID of the leave template to delete.
     */
    @Transactional
    public void delete(Long id) {
        LeaveTemplate leaveName = leaveTemplateRepository.findById(id)
                        .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.template.data.not.found")));

        leaveTemplateRepository.delete(leaveName);
    }


	private void convertForUpdate(LeaveTemplate entity, LeaveTemplateDTO dto) {
		entity.setName(dto.getName());
		entity.setCode(dto.getCode());
		entity.setTotalLeaveDays(dto.getTotalLeaveDays());
		if(!CollectionUtils.isEmpty(dto.getLeaveTemplateDtlsDTO())) {
			List<LeaveTemplateDtlsDTO> newLeave=dto.getLeaveTemplateDtlsDTO().stream()
					.filter(predicate -> predicate.getId()==null).toList();
		
		if(!CollectionUtils.isEmpty(entity.getLeaveTemplateDtls())) {
			List<LeaveTemplateDtlsDTO> existingLeave=dto.getLeaveTemplateDtlsDTO().stream()
					.filter(predicate -> predicate.getId() !=null).toList();
			
			entity.getLeaveTemplateDtls().forEach(obj->{
				Optional<LeaveTemplateDtlsDTO> first= existingLeave.stream().filter(predicate-> predicate.getId().equals( obj.getId())).findFirst();
				if(first.isPresent()) {
					LeaveTemplateDtlsDTO leaveTemplateDtlsDTO= first.get();
					obj.setLeaveNameId(leaveTemplateDtlsDTO.getLeaveNameId());
					obj.setYearlyCount(leaveTemplateDtlsDTO.getYearlyCount());
					obj.setQuaterlyCount(leaveTemplateDtlsDTO.getQuaterlyCount());
					obj.setMonthlyCount(leaveTemplateDtlsDTO.getMonthlyCount());
					obj.setPastDays(leaveTemplateDtlsDTO.getPastDays()!=null ?leaveTemplateDtlsDTO.getPastDays():0);
					obj.setFutureDays(leaveTemplateDtlsDTO.getFutureDays() !=null ?leaveTemplateDtlsDTO.getFutureDays():0);
					obj.setHalfDayAllowed(leaveTemplateDtlsDTO.isHalfDayAllowed());
				}else {
					obj.setRemove(true);
				}
			});
			
			//new leave
			if(!CollectionUtils.isEmpty(newLeave)) {
				for(LeaveTemplateDtlsDTO leaveTemplateDtlsDTO: newLeave) {
					entity.getLeaveTemplateDtls().add(LeaveConverter.toEntity(leaveTemplateDtlsDTO, entity));
				}
				
			}
			entity.getLeaveTemplateDtls().removeIf(LeaveTemplateDtls::isRemove);
		}else {
			List<LeaveTemplateDtls> newLeaveRules= new ArrayList<>();
			for(LeaveTemplateDtlsDTO leaveTemplateDtlsDTO:newLeave) {
				newLeaveRules.add(LeaveConverter.toEntity(leaveTemplateDtlsDTO, entity));
			}
			entity.setLeaveTemplateDtls(newLeaveRules);
		
	}
		}
	}


	private void validateRequest(LeaveTemplateDTO leaveTemplateDTO) {
		
		if(ValidateUtils.isNullObject(leaveTemplateDTO)) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.template.data.required"));
		}
		if(ValidateUtils.isStringEmpty(leaveTemplateDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("name.required"));
		}
		if (leaveTemplateDTO.getName().length() >= 255) {
	        throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
	    }
		if(!ValidateUtils.isValidNameFormat(leaveTemplateDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
		}
		if(!ValidateUtils.isValidCodeFormat(leaveTemplateDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
		}
		if(ValidateUtils.isStringEmpty(leaveTemplateDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("code.required"));
		}
		if (leaveTemplateDTO.getCode().length() >= 30) {
	        throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
	    }
		if(ValidateUtils.isZero(leaveTemplateDTO.getTotalLeaveDays())) {
			throw new ValidationException(messageResource.getEnglishMessage("total.leaves.required"));
		}
		double totalYearlyCount = 0;

		if(!CollectionUtils.isEmpty(leaveTemplateDTO.getLeaveTemplateDtlsDTO())) {
			for(LeaveTemplateDtlsDTO dtlsDTO: leaveTemplateDTO.getLeaveTemplateDtlsDTO()) {
                if (ValidateUtils.isLongZero(dtlsDTO.getLeaveNameId())){
                    throw new ValidationException(messageResource.getEnglishMessage("leave.data.required"));
                }
				if(ValidateUtils.isZero(dtlsDTO.getYearlyCount())) {
					throw new ValidationException(messageResource.getEnglishMessage("yearly.count"));
				}
				 if (dtlsDTO.getQuaterlyCount() > dtlsDTO.getYearlyCount()) {
	                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.less.than.yearly"));
                 }
				 if (dtlsDTO.getMonthlyCount() > dtlsDTO.getQuaterlyCount()) {
	                    throw new ValidationException(messageResource.getEnglishMessage("monthly.count.less.than.quarterly"));
                 }
				if(ValidateUtils.isZero(dtlsDTO.getQuaterlyCount())) {
					throw new ValidationException(messageResource.getEnglishMessage("quarterly.count.required"));
				}
				if(ValidateUtils.isZero(dtlsDTO.getMonthlyCount())) {
					throw new ValidationException(messageResource.getEnglishMessage("Monthly.count.required"));
				}
                totalYearlyCount += dtlsDTO.getYearlyCount();
			}
			  if (leaveTemplateDTO.getTotalLeaveDays() != totalYearlyCount) {
		            throw new ValidationException(messageResource.getEnglishMessage("total.leaves.count.should.be.greater.than.yearly.count"));
		        }
		}
	}

	private void validateByRepository(LeaveTemplateDTO leaveTemplateDTO) {
		// find by code if exist throw exception
		LeaveTemplate byCode= leaveTemplateRepository.findByCode(leaveTemplateDTO.getCode());
		if(!ValidateUtils.isNullObject(byCode)) {
			throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
		}
	}

}