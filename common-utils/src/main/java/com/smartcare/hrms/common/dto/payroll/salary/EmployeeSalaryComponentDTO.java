package com.smartcare.hrms.common.dto.payroll.salary;

import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.Formula;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.enumeration.SalaryComponentSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSalaryComponentDTO {
    private Integer displayOrder;
    private Long componentId;
    private String componentName;
    private String componentCode;
    private ComponentType componentType;
    private CalculationType calculationType;
    private SalaryComponentSource amountSource;
    private Formula formula;
    private BigDecimal payableAmount;
    private BigDecimal monthlyGrossAmount;
}
