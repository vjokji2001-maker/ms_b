package com.smartcare.hrms.payroll.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

public class ProfessionalTaxCalculator {
    public static BigDecimal calculateProfessionalTax(BigDecimal monthlyGross, String state, String gender) {
        String stateLower = state.toLowerCase().trim();
        String genderLower = gender.toLowerCase().trim();
        boolean isFebruary = LocalDate.now().getMonth() == Month.FEBRUARY;

        // Common comparison constants
        BigDecimal annualGross = monthlyGross.multiply(new BigDecimal("12"));

        switch (stateLower) {
            case "chhattisgarh":
                if (annualGross.compareTo(new BigDecimal("100000")) <= 0) return BigDecimal.ZERO;
                if (annualGross.compareTo(new BigDecimal("150000")) <= 0) return new BigDecimal("130");
                if (annualGross.compareTo(new BigDecimal("200000")) <= 0) return new BigDecimal("150");
                if (annualGross.compareTo(new BigDecimal("250000")) <= 0) return new BigDecimal("200");
                // For high earners, roughly 208/month to hit ~2500 annual limit
                return isFebruary ? new BigDecimal("212") : new BigDecimal("208");

            case "maharashtra":
                if (genderLower.equals("female")) {
                    if (monthlyGross.compareTo(new BigDecimal("25000")) <= 0) return BigDecimal.ZERO;
                    return isFebruary ? new BigDecimal("300") : new BigDecimal("200");
                } else {
                    if (monthlyGross.compareTo(new BigDecimal("7500")) <= 0) return BigDecimal.ZERO;
                    if (monthlyGross.compareTo(new BigDecimal("10000")) <= 0) return new BigDecimal("175");
                    return isFebruary ? new BigDecimal("300") : new BigDecimal("200");
                }

            case "karnataka":
                if (monthlyGross.compareTo(new BigDecimal("25000")) < 0) return BigDecimal.ZERO;
                return isFebruary ? new BigDecimal("300") : new BigDecimal("200");

            case "west bengal":
                if (monthlyGross.compareTo(new BigDecimal("10000")) <= 0) return BigDecimal.ZERO;
                if (monthlyGross.compareTo(new BigDecimal("15000")) <= 0) return new BigDecimal("110");
                if (monthlyGross.compareTo(new BigDecimal("25000")) <= 0) return new BigDecimal("130");
                if (monthlyGross.compareTo(new BigDecimal("40000")) <= 0) return new BigDecimal("150");
                return new BigDecimal("200");

            case "telangana":
            case "andhra pradesh":
                if (monthlyGross.compareTo(new BigDecimal("15000")) <= 0) return BigDecimal.ZERO;
                if (monthlyGross.compareTo(new BigDecimal("20000")) <= 0) return new BigDecimal("150");
                return new BigDecimal("200");

            case "gujarat":
                if (monthlyGross.compareTo(new BigDecimal("12000")) <= 0) return BigDecimal.ZERO;
                return new BigDecimal("200");

            default:
                // Delhi, Haryana, UP, etc.
                return BigDecimal.ZERO;
        }
    }
}
