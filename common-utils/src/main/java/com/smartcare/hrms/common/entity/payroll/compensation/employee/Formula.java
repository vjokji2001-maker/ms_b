package com.smartcare.hrms.common.entity.payroll.compensation.employee;

import lombok.*;

import java.io.Serializable;
import java.util.Map;
@Getter
@Setter
@Builder
@NoArgsConstructor  // Required for JSON deserialization
@AllArgsConstructor
@EqualsAndHashCode
public class Formula implements Serializable {

    private static final long serialVersionUID = 1L;

    private String viewFormula;
    private String expression;
    private Map<String, Double> vars;
}
