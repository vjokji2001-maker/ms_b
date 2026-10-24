package com.smartcare.hrms.payroll.config;

import com.smartcare.hrms.common.utility.PayrollMathUtil;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.util.Map;

public class SalaryCalculator {

    public static BigDecimal evaluate(String formulaExpression, Map<String, Double> values) {
        ExpressionBuilder builder = new ExpressionBuilder(formulaExpression);
        // register variables
        builder.variables(values.keySet());
        Expression expression = builder.build();
        // set values
        values.forEach(expression::setVariable);
        return PayrollMathUtil.roundAmount(BigDecimal.valueOf(expression.evaluate()));
    }
}
