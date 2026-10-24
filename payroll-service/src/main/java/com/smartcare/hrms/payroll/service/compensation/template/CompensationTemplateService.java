package com.smartcare.hrms.payroll.service.compensation.template;

import com.smartcare.hrms.common.globalexception.ResourceConflictException;
import com.smartcare.hrms.common.dto.payroll.compensation.component.ComponentDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.template.CompensationTemplateDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.template.CompensationTemplate;
import com.smartcare.hrms.payroll.mappers.compensation.template.CompensationTemplateMapper;
import com.smartcare.hrms.common.repository.payroll.compensation.template.CompensationTemplateRepository;
import com.smartcare.hrms.payroll.service.compensation.component.ComponentMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompensationTemplateService {
    private @Autowired CompensationTemplateRepository compensationTemplateRepository;
    private @Autowired CompensationTemplateMapper compensationTemplateMapper;
    private @Autowired ComponentMasterService componentMasterService;
    @Transactional
    public void create(CompensationTemplateDTO templateDTO) {
        if (compensationTemplateRepository.existsByCode(templateDTO.getCode())) {
            throw new ResourceConflictException("compensation.template.code.already.exists");
        } else if (compensationTemplateRepository.existsByName(templateDTO.getName().trim())) {
            throw new ResourceConflictException("compensation.template.name.already.exists");
        }
        List<Long> componentIds = templateDTO.getComponentList().stream().map(componet -> componet.getComponentId()).toList();
        Map<Long,ComponentDTO> componentDtosMap = componentMasterService.getByIds(componentIds).stream().collect(Collectors.toMap(ComponentDTO::getId, Function.identity()));

        templateDTO.getComponentList().forEach(component -> {
            if (!componentDtosMap.containsKey(component.getComponentId())) {
                throw new ResourceConflictException("compensation.component.not.found");
            }
            ComponentDTO componentDTO = componentDtosMap.get(component.getComponentId());
            component.setComponentName(componentDTO.getName());
            component.setComponentCode(componentDTO.getCode());
        });
        CompensationTemplate template = compensationTemplateMapper.toEntity(templateDTO);
        compensationTemplateRepository.save(template);
    }

    public List<CompensationTemplateDTO> getAll() {
        List<CompensationTemplate> templates = compensationTemplateRepository.findAll();
        return compensationTemplateMapper.toDtoList(templates);
    }

    public CompensationTemplateDTO getById(Long id) {
        CompensationTemplate template = compensationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("compensation.template.not.found"));
        return compensationTemplateMapper.toDto(template);
    }

    @Transactional
    public void update(CompensationTemplateDTO templateDTO) {
        CompensationTemplate existingTemplate = compensationTemplateRepository.findById(templateDTO.getId())
                .orElseThrow(() -> new ResourceConflictException("compensation.template.not.found"));

        if (compensationTemplateRepository.existsByCodeAndIdNot(templateDTO.getCode(), templateDTO.getId())) {
            throw new ResourceConflictException("compensation.template.code.already.exists");
        } else if (compensationTemplateRepository.existsByNameAndIdNot(templateDTO.getName().trim(), templateDTO.getId())) {
            throw new ResourceConflictException("compensation.template.name.already.exists");
        }
        List<Long> componentIds = templateDTO.getComponentList().stream().map(componet -> componet.getComponentId()).toList();
        Map<Long,ComponentDTO> componentDtosMap = componentMasterService.getByIds(componentIds).stream().collect(Collectors.toMap(ComponentDTO::getId, Function.identity()));

        templateDTO.getComponentList().forEach(component -> {
            if (!componentDtosMap.containsKey(component.getComponentId())) {
                throw new ResourceConflictException("compensation.component.not.found");
            }
            ComponentDTO componentDTO = componentDtosMap.get(component.getComponentId());
            component.setComponentName(componentDTO.getName());
            component.setComponentCode(componentDTO.getCode());
        });
        compensationTemplateMapper.updateEntityFromDto(templateDTO, existingTemplate);
        compensationTemplateRepository.save(existingTemplate);
    }
}
