package com.smartcare.hrms.common.dto.payroll.salary;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.dto.BaseDashboardSearchDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollSummaryRequestDTO extends BaseDashboardSearchDTO {
    @DataRequired(message = "Month and Year is required")
    private String monthYear;
    private String jobTitle;
    private String empId;
}
