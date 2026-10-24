package com.smartcare.hrms.payroll.mappers.compensation.component;

import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.dto.payroll.compensation.component.ComponentDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.component.ComponentMaster;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ComponentMasterMapper {

    @Mapping(source = "type", target = "type")
    ComponentDTO toDto(ComponentMaster entity);

    @Mapping(source = "type", target = "type")
    ComponentMaster toEntity(ComponentDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "type", target = "type")
    void updateEntityFromDto(
            ComponentDTO dto,
            @MappingTarget ComponentMaster entity
    );


    // Enum -> DTO (display name)
    default String map(ComponentType type) {
        return type != null ? type.getDisplayName() : null;
    }

    // DTO -> Enum (accept both enum constant name and display name)
    default ComponentType map(String type) {
        if (type == null) return null;
        for (ComponentType t : ComponentType.values()) {
            if (t.name().equalsIgnoreCase(type) || t.getDisplayName().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid ComponentType: " + type);
    }
}
