package com.smartcare.hrms.common.entity.payroll.salary;

import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.Formula;
import com.smartcare.hrms.common.enumeration.SalaryComponentSource;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor  // Required for JSON deserialization
@AllArgsConstructor
@EqualsAndHashCode
public class EmpSalaryComponentDetails implements Serializable {

    private static final long serialVersionUID = 1L;

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
