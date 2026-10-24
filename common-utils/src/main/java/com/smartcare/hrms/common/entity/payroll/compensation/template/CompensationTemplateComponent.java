package com.smartcare.hrms.common.entity.payroll.compensation.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.enumeration.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompensationTemplateComponent {

    private Long id;
    private Long componentId;
    private String componentName;
    private String componentCode;
    private ComponentType componentType;
    private CalculationType calculationType;
    private String viewFormula;
    private String expression;
    private Map<String, Double> vars;
}
