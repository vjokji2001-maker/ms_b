package com.smartcare.hrms.common.dto.payroll.compensation.component;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentDTO {

    private Long id;

    @DataRequired(
            message = "{name.required}",
            pattern = PatternConstants.COMPONENT_NAME_PATTERN,
            patternMessage = "{name.alpha.only}"
    )
    @Size(max = 100, message = "{name.length}")
    private String name;

    @DataRequired(message = "{code.required}",
            pattern = PatternConstants.ALPHA_NO_SPACE,
            patternMessage = "{code.alpha.only}")
    @Size(max = 10, message = "{code.length}")
    private String code;

    @DataRequired(message = "{type.required}")
    private String type;

}
