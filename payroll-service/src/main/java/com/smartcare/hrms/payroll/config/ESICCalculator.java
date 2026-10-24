package com.smartcare.hrms.payroll.config;

import com.smartcare.hrms.common.utility.PayrollMathUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ESICCalculator{
// ESI Constants as per Indian Labor Law
private static final BigDecimal ESI_LIMIT = new BigDecimal("21000");
private static final BigDecimal ESI_RATE = new BigDecimal("0.0075"); // 0.75%

/**
 * Calculates Employee ESI contribution.
 * ESI is 0.75% of Gross Salary if Gross <= 21,000.
 */
public static BigDecimal calculateESI(BigDecimal monthlyGross) {

    if (monthlyGross.compareTo(ESI_LIMIT) > 0) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    BigDecimal esiContribution = monthlyGross.multiply(ESI_RATE);
    return PayrollMathUtil.roundAmount(esiContribution);
}
}
