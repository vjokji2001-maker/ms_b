package com.smartcare.hrms.payroll.mappers.compensation.template;

import com.smartcare.hrms.common.dto.payroll.compensation.template.CompensationTemplateComponentDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.template.CompensationTemplateDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.template.CompensationTemplate;
import com.smartcare.hrms.common.entity.payroll.compensation.template.CompensationTemplateComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompensationTemplateMapper {

    // --- Main Template Mappings ---

    CompensationTemplateDTO toDto(CompensationTemplate entity);

    CompensationTemplate toEntity(CompensationTemplateDTO dto);

    List<CompensationTemplateDTO> toDtoList(List<CompensationTemplate> entities);

    // --- Nested Component Mappings ---
    // MapStruct uses these automatically for the List<Component>

    @Mapping(target = "componentId", source = "componentId")
    @Mapping(target = "componentName", source = "componentName")
    @Mapping(target = "componentCode", source = "componentCode")
    @Mapping(target = "componentType", source = "componentType")
    @Mapping(target = "calculationType", source = "calculationType")
    CompensationTemplateComponent toComponentEntity(CompensationTemplateComponentDTO dto);

    @Mapping(target = "componentId", source = "componentId")
    @Mapping(target = "componentName", source = "componentName")
    @Mapping(target = "componentCode", source = "componentCode")
    CompensationTemplateComponentDTO toComponentDto(CompensationTemplateComponent entity);

    // --- Update Method ---
    // This updates the existing 'entity' with data from 'dto'
    void updateEntityFromDto(CompensationTemplateDTO dto, @MappingTarget CompensationTemplate entity);
}

