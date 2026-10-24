package com.smartcare.hrms.master.service.attendance.shift;

import com.smartcare.hrms.common.converters.attendance.shift.ShiftConverter;
import com.smartcare.hrms.common.dto.master.attendance.approvalgroup.ApprovalGroupDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftLateMarkRuleDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDashboardDTO;
import com.smartcare.hrms.common.entity.master.attendance.shift.LateMarkRule;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.enumeration.LateMarkType;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.attendance.shift.ShiftTemplateRepository;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.TimeHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.master.config.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class ShiftTemplateService {

    private @Autowired ShiftTemplateRepository shiftTemplateRepository;
    private @Autowired MessageResource messageResource;

    /**
     * Creates a new shift template based on the provided request DTO.
     *
     * @param requestDTO the {@link ShiftTemplateDTO} containing shift template details.
     * @throws ValidationException if the request DTO is invalid.
     */
    @Transactional
    public void create(ShiftTemplateDTO requestDTO){
     /* Validate shift request */
        validateRequest(requestDTO);
        validateByRepository(requestDTO);
        ShiftTemplate entity = ShiftConverter.toEntity(requestDTO);
        shiftTemplateRepository.save(entity);
    }

    /**
     * Updates an existing shift template with the provided details.
     *
     * @param requestDTO the {@link ShiftTemplateDTO} containing updated shift template information.
     * @throws ValidationException if the request DTO is invalid.
     */
    @Transactional
    public void update(ShiftTemplateDTO requestDTO){
        if(ValidateUtils.isLongZero(requestDTO.getId())){
            throw new ValidationException(messageResource.getEnglishMessage("shift.id.is.zero"));
        }
        validateRequest(requestDTO);

        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(requestDTO.getId());
        if(shiftTemplateOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("shift.data.not.found"));
        }
        ShiftTemplate shiftTemplate = shiftTemplateOptional.get();
        if(!shiftTemplate.getCode().equals(requestDTO.getCode())){
            validateByRepository(requestDTO);
        }

        /** update entity with dto **/
        convertForUpdate(shiftTemplate, requestDTO);
        shiftTemplateRepository.save(shiftTemplate);
    }


    /**
     * Retrieves a shift template by its ID.
     *
     * @param id the ID of the shift template to retrieve.
     * @return the corresponding {@link ShiftTemplateDTO} if found.
     * @throws ValidationException if no shift template is found with the given ID.
     */
    public ShiftTemplateDTO getById(Long id){
        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(id);
        if(shiftTemplateOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("shift.data.not.found"));
        }else{
            return ShiftConverter.toDTO(shiftTemplateOptional.get());
        }
    }

    /**
     * Retrieves a list of shift templates filtered by name or code.
     * Returns all shift templates if no filters are provided.
     *
     * @param shiftTemplateDTO the {@link ShiftTemplateDTO} containing optional name or code filters.
     * @return list of matching {@link ShiftTemplateDashboardDTO} entries.
     */
    public List<ShiftTemplateDashboardDTO>  getAllOrByNameOrCode(ShiftTemplateDTO shiftTemplateDTO){
        List<ShiftTemplateDashboardDTO> list = new ArrayList<>();
        if(!ValidateUtils.isStringEmpty(shiftTemplateDTO.getName())){
            List<ShiftTemplate> byNameContainingIgnoreCase = shiftTemplateRepository.findByNameContainingIgnoreCase(shiftTemplateDTO.getName());
            if(!CollectionUtils.isEmpty(byNameContainingIgnoreCase)){
                for (ShiftTemplate shiftTemplate : byNameContainingIgnoreCase) {
                    list.add(ShiftConverter.convertToDashboardDTO(shiftTemplate));
                }
            }else{
                throw new ValidationException(messageResource.getEnglishMessage("shift.data.not.found"));
            }
        }else if(!ValidateUtils.isStringEmpty(shiftTemplateDTO.getCode())){
            ShiftTemplate byCode = shiftTemplateRepository.findByCode(shiftTemplateDTO.getCode());
            if(!ValidateUtils.isNullObject(byCode)){
                list.add(ShiftConverter.convertToDashboardDTO(byCode));
            }else{
                throw new ValidationException(messageResource.getEnglishMessage("shift.data.not.found"));
            }
        }else{
            List<ShiftTemplate> all = shiftTemplateRepository.findAll();
            if(!CollectionUtils.isEmpty(all)){
                for (ShiftTemplate shiftTemplate : all) {
                    list.add(ShiftConverter.convertToDashboardDTO(shiftTemplate));
                }
            }
        }
        list.sort(Comparator.comparing(ShiftTemplateDashboardDTO::getName,String.CASE_INSENSITIVE_ORDER).reversed());
        return list;
    }

    /**
     * Deletes a shift template by its ID.
     *
     * @param id the ID of the shift template to delete.
     * @throws ValidationException if no shift template is found with the given ID.
     */
    @Transactional
    public void delete(Long id) {
    	ShiftTemplate shiftTemplate= shiftTemplateRepository.findById(id).orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("shift.data.not.found")));
    	shiftTemplateRepository.delete(shiftTemplate);
    	
    }

    private void validateRequest(ShiftTemplateDTO requestDTO){
        if (ValidateUtils.isNullObject(requestDTO)) {
            throw new ValidationException(messageResource.getEnglishMessage("shift.detail.required") );
        }

        if (ValidateUtils.isStringEmpty(requestDTO.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if(!ValidateUtils.isValidCodeFormat(requestDTO.getCode())){
        	throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }

        if (ValidateUtils.isStringEmpty(requestDTO.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
        }
        if(!ValidateUtils.isValidNameFormat(requestDTO.getName())) {
        	throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }

        if (ValidateUtils.isNullObject(requestDTO.getInTime())) {
            throw new ValidationException(messageResource.getEnglishMessage("in-time.required"));
        }

        if (ValidateUtils.isNullObject(requestDTO.getOutTime())) {
            throw new ValidationException(messageResource.getEnglishMessage("out-time.required"));
        }

        if (ValidateUtils.isNullObject(requestDTO.getTotalHours())) {
            throw new ValidationException(messageResource.getEnglishMessage("working.hour.required"));
        }


        if (Boolean.TRUE.equals(requestDTO.isHalfDay())) {
            if (ValidateUtils.isNullObject(requestDTO.getFirstHalfOutTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("working.hour.required"));
            }

            if (ValidateUtils.isNullObject(requestDTO.getSecondHalfInTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("time.required"));
            }
        }
        if(ValidateUtils.isZero(requestDTO.getCutOffTime())){
            throw new ValidationException(messageResource.getEnglishMessage("at.least.one.hour.required"));
        }

        if (!CollectionUtils.isEmpty(requestDTO.getLateMarkRuleDTOList())) {
            for (ShiftLateMarkRuleDTO rule : requestDTO.getLateMarkRuleDTOList()) {

                if (rule.getFromTime()>=rule.getToTime()) {
                    throw new ValidationException(messageResource.getEnglishMessage("invalid.time"));
                }

                if (rule.getMarkCount()<=0) {
                    throw new ValidationException(messageResource.getEnglishMessage("mark.count.required"));
                }

                if (rule.getTotalMarkCount()<=0) {
                    throw new ValidationException(messageResource.getEnglishMessage("mark.count.required"));
                }

                if (ValidateUtils.isNullObject(rule.getType())) {
                    throw new ValidationException(messageResource.getEnglishMessage("type.required"));
                }
            }
        }



    }

    private void validateByRepository(ShiftTemplateDTO shiftTemplateDTO){
        ShiftTemplate byCode = shiftTemplateRepository.findByCode(shiftTemplateDTO.getCode());
        if(!ValidateUtils.isNullObject(byCode)){
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }


    private  void convertForUpdate(ShiftTemplate entity, ShiftTemplateDTO dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setNightShift(dto.isNightShift());
        entity.setHalfDay(dto.isHalfDay());
        entity.setHourBased(dto.isHourBased());
        entity.setInTime(TimeHelper.convertToLocalTime(dto.getInTime()));
        entity.setOutTime(TimeHelper.convertToLocalTime(dto.getOutTime()));
        entity.setFirstHalfOutTime(TimeHelper.convertToLocalTime(dto.getFirstHalfOutTime()));
        entity.setSecondHalfInTime(TimeHelper.convertToLocalTime(dto.getSecondHalfInTime()));
        entity.setTotalHalfDayHours(dto.getTotalHalfDayHours() != null ? TimeHelper.convertToLocalTime(dto.getTotalHalfDayHours()) : null);
        entity.setBufferTime(dto.getBufferTime());
        entity.setCutOffTime(dto.getCutOffTime());
        entity.setHalfRate(dto.getHalfRate()!=null?dto.getHalfRate():0);
        entity.setFullRate(dto.getFullRate()!=null?dto.getFullRate():0);
        //check if dto collection is empty
        if (!ValidateUtils.isNullObject(dto.getLateMarkRuleDTOList())) {
            //new rule to be added
            List<ShiftLateMarkRuleDTO> newRuleList = dto.getLateMarkRuleDTOList().stream()
                    .filter(predicate -> predicate.getId() == null).toList();

            if (!CollectionUtils.isEmpty(entity.getLateMarkRules())) {
                //update existing late mark rule
                //existing rule
                List<ShiftLateMarkRuleDTO> existingRuleList = dto.getLateMarkRuleDTOList().stream()
                        .filter(predicate -> predicate.getId() != null).toList();

                entity.getLateMarkRules().forEach(obj -> {
                    Optional<ShiftLateMarkRuleDTO> first = existingRuleList.stream().filter(pre -> pre.getId().equals(obj.getId())).findFirst();
                    if (first.isPresent()) {
                        ShiftLateMarkRuleDTO shiftLateMarkRuleDTO = first.get();
                        obj.setFromTime(shiftLateMarkRuleDTO.getFromTime());
                        obj.setToTime(shiftLateMarkRuleDTO.getToTime());
                        obj.setMarkCount(shiftLateMarkRuleDTO.getMarkCount());
                        obj.setTotalMarkCount(shiftLateMarkRuleDTO.getTotalMarkCount());
                        obj.setType(EnumConverter.toEnum(shiftLateMarkRuleDTO.getType(), LateMarkType.class));
                    }else{
                        obj.setRemove(true);
                    }
                });
                //new rule
                if (!CollectionUtils.isEmpty(newRuleList)) {
                    for (ShiftLateMarkRuleDTO shiftLateMarkRuleDTO : newRuleList) {
                        entity.getLateMarkRules().add(ShiftConverter.toEntity(shiftLateMarkRuleDTO, entity));
                    }
                }
                //delete existing
                entity.getLateMarkRules().removeIf(LateMarkRule::isRemove);
            }else {
                if (entity.getLateMarkRules() == null) {
                    entity.setLateMarkRules(new ArrayList<>());
                }

                for (ShiftLateMarkRuleDTO shiftLateMarkRuleDTO : newRuleList) {
                    entity.getLateMarkRules().add(
                            ShiftConverter.toEntity(shiftLateMarkRuleDTO, entity)
                    );
                }
            }
        }
    }
}
