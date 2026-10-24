package com.smartcare.hrms.employee.dto;

import com.smartcare.hrms.common.dto.BaseUuidDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeNameCodeDTO extends BaseUuidDTO {

    private String formalName;
    private String empCode;
}
