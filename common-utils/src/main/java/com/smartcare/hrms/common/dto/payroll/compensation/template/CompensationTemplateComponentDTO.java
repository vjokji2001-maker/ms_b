package com.smartcare.hrms.common.dto.payroll.compensation.template;

import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.enumeration.CalculationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompensationTemplateComponentDTO {
    private Long id;

    @NotNull(message = "{component.required}")
    private Long componentId;

    private String componentName;

    private String componentCode;

    @NotNull(message = "{component.type.required}")
    private ComponentType componentType;

    @NotNull(message = "{calculation.type.required}")
    private CalculationType calculationType;

    private String viewFormula;
    private String expression;
    private Map<String, Double> vars;
}
