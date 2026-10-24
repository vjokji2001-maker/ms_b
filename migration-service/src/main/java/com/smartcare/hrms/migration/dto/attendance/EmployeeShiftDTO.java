package com.smartcare.hrms.migration.dto.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeShiftDTO {

    private String empId;

    private Long shiftId;

    private String date;

    @Builder.Default
    private Integer isDefault = 1;
}
