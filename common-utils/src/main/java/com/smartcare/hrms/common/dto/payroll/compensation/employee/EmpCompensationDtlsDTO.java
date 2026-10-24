package com.smartcare.hrms.common.dto.payroll.compensation.employee;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.enumeration.CalculationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpCompensationDtlsDTO {
    private String id;

    @NotNull(message = "{component.dtls.required}")
    private Long componentId;

    @DataRequired(message = "{component.name.required}")
    private String componentName;

    @DataRequired(message = "{component.code.required}")
    private String code;

    @NotNull(message = "{component.type.required}")
    private ComponentType componentType;

    @NotNull(message = "{calculation.type.required}")
    private CalculationType calculationType;

    private FormulaDTO formulaDTO;

    @NotNull(message = "{amount.required}")
    private BigDecimal amount;

    private BigDecimal thresholdAmount;

    @NotNull(message = "{sequence.required}")
    private Integer sequence;

    @Data
    public static class FormulaDTO { // Note the 'static'
        private String viewFormula;
        private String expression;
        private Map<String, Double> vars;
    }

}

