package com.smartcare.hrms.common.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PayrollMathUtil {
    private PayrollMathUtil() {}

    public static BigDecimal roundAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(0, RoundingMode.HALF_UP).setScale(2);
    }

}
