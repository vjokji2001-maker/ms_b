package com.smartcare.hrms.master.service.attendance.leave;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.dto.master.attendance.approvalgroup.ApprovalGroupDTO;
import com.smartcare.hrms.common.globalexception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.converters.attendance.leave.LeaveConverter;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveNameDTO;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveName;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveNameRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

import jakarta.transaction.Transactional;

@Service
public class LeaveNameService {

	private @Autowired LeaveNameRepository leaveNameRepository;
	private @Autowired MessageResource messageResource;

    /**
     * Creates a new leave entry based on the provided DTO.
     *
     * @param leaveNameDTO the {@link LeaveNameDTO} containing leave details to be created.
     */
	@Transactional
	public void create(LeaveNameDTO leaveNameDTO) {
		//validate leave request
		validateRequest(leaveNameDTO);
		//validate code if exist throw exception
		validateByRepository(leaveNameDTO);
		//to create leave details
		LeaveName leaveName= LeaveConverter.toEntity(leaveNameDTO);
		leaveNameRepository.save(leaveName);
	}

    /**
     * Updates an existing leave entry with the provided details.
     *
     * @param leaveNameDTO the DTO containing updated leave information.
     */
	@Transactional
	public void update(LeaveNameDTO leaveNameDTO) {
		if(ValidateUtils.isLongZero(leaveNameDTO.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.id.is.zero"));
		}
		validateRequest(leaveNameDTO);
		// find leave detail by id
		Optional<LeaveName> leaveNameOptional = leaveNameRepository.findById(leaveNameDTO.getId());
		if(leaveNameOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.not.found"));
		}
		//if data is present update it
		 LeaveName entity = leaveNameOptional.get();
        if (!entity.getCode().equals(leaveNameDTO.getCode())){
            validateByRepository(leaveNameDTO);
        }
		convertForUpdate(entity, leaveNameDTO);
		leaveNameRepository.save(entity);
	}

    /**
     * Retrieves leave details by ID.
     *
     * @param id the ID of the leave entry to retrieve.
     * @return the corresponding LeaveNameDTO, if found.
     */
	public LeaveNameDTO getById(Long id) {
	    if (ValidateUtils.isLongZero(id)) {
	        throw new ValidationException(messageResource.getEnglishMessage("leave.id.required"));
	    }
	    // find data by id
	    Optional<LeaveName> leaveNameOptional = leaveNameRepository.findById(id);
	    if (leaveNameOptional.isEmpty()) {
	        throw new ValidationException(messageResource.getEnglishMessage("leave.not.found"));
	    }
	    LeaveName entity = leaveNameOptional.get();
	    return convertToDTO(entity); 
	}



    /**
     * Retrieves a list of leave entries filtered by name or code.
     * Returns all leave entries if no filters are provided.
     *
     * @param leaveNameDTO the LeaveNameDTO containing optional name or code filters.
     * @return list of matching LeaveNameDTO entries.
     */
	public List<LeaveNameDTO> getAllOrByNameOrByCode(LeaveNameDTO leaveNameDTO){
		List<LeaveNameDTO> list= new ArrayList<>();
		// find  leave details by name
		if(!ValidateUtils.isStringEmpty(leaveNameDTO.getName())) {
			List<LeaveName> byName= leaveNameRepository.findByNameContainingIgnoreCase(leaveNameDTO.getName());
		if(!CollectionUtils.isEmpty(byName)) {
			for(LeaveName leaveName: byName) {
				list.add(LeaveConverter.toDTO(leaveName));
			}
			
		}else {
			throw new ValidationException(messageResource.getEnglishMessage("leave.not.found"));
		}
		}//find  leave details by code
		else if(!ValidateUtils.isStringEmpty(leaveNameDTO.getCode())) {
			LeaveName byCode= leaveNameRepository.findByCode(leaveNameDTO.getCode());
			if(!ValidateUtils.isNullObject(byCode)) {
				list.add(LeaveConverter.toDTO(byCode));
			}
			else{            
				throw new ValidationException(messageResource.getEnglishMessage("leave.not.found"));       
			}
	}
		// find all leave details
		else{
          List<LeaveName> all = leaveNameRepository.findAll();
          if(!CollectionUtils.isEmpty(all)){
              for (LeaveName leaveName : all) {
                  list.add(LeaveConverter.toDTO(leaveName));
              }
          }
      }
        list.sort(Comparator.comparing(LeaveNameDTO::getName, String.CASE_INSENSITIVE_ORDER).reversed());
      return list;
		
	}

    /**
     * Deletes leave data by its ID.
     *
     * @param id the ID of the leave entry to delete.
     */
    @Transactional
	public void delete(Long id) {
	    LeaveName leaveName = leaveNameRepository.findById(id)
                        .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.not.found")));
        
	    leaveNameRepository.delete(leaveName);
	}

	private LeaveNameDTO convertToDTO(LeaveName entity) {
	    LeaveNameDTO dto = new LeaveNameDTO();
	    dto.setId(entity.getId());
	    dto.setName(entity.getName());
	    dto.setCode(entity.getCode());
	    dto.setColourCode(entity.getColourCode());
	    // add more fields as needed
	    return dto;
	}

	private void convertForUpdate(LeaveName entity, LeaveNameDTO dto) {
		entity.setName(dto.getName());
		entity.setCode(dto.getCode());
		entity.setColourCode(dto.getColourCode());
	}

	private void validateRequest(LeaveNameDTO leaveNameDTO) {
		
		if(ValidateUtils.isNullObject(leaveNameDTO)) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.data.required"));
		}
		if(ValidateUtils.isStringEmpty(leaveNameDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("name.required"));
		}
		if(!ValidateUtils.isValidNameFormat(leaveNameDTO.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
		}
		if(!ValidateUtils.isValidCodeFormat(leaveNameDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
		}
		if(leaveNameDTO.getName().length()>=255) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.name.too.long"));
		}
		if(ValidateUtils.isStringEmpty(leaveNameDTO.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("code.required"));
		}
		if(leaveNameDTO.getCode().length()>=30) {
			throw new ValidationException(messageResource.getEnglishMessage("leave.code.too.long"));
		}
		if(ValidateUtils.isStringEmpty(leaveNameDTO.getColourCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("colour.code.required"));
		}
	}

	private void validateByRepository (LeaveNameDTO leaveNameDTO) {
		// find data by code if exist throw exception
		LeaveName byCode= leaveNameRepository.findByCode(leaveNameDTO.getCode());
		if(!ValidateUtils.isNullObject(byCode)) {
			throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
		}
	}
}
