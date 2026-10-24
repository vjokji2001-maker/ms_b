package com.smartcare.hrms.common.dto.payroll.compensation.template;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompensationTemplateDTO {
    private Long id;

    @DataRequired(message = "{name.required}")
    @Size(max = 100, message = "{name.length}")
    private String name;

    @DataRequired(message = "{code.required}",
            pattern = PatternConstants.ALPHANUMERIC_ONLY,
            patternMessage = "{code.alpha.only}")
    @Size(max = 10, message = "{code.length}")
    private String code;

    private Long gradeId;

    @NotNull(message = "{min.gross.required}")
    @Positive(message = "{gross.min.required}")
    private Double grossMinAmount;

    @NotNull(message = "{gross.max.required}")
    @Positive(message = "{gross.max.required}")
    private Double grossMaxAmount;

    @NotEmpty(message = "{compensation.componentList.empty}")
    private List< @Valid CompensationTemplateComponentDTO> componentList;
}
