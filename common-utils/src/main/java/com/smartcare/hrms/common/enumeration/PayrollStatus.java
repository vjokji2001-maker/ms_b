package com.smartcare.hrms.common.enumeration;

public enum PayrollStatus {
    PENDING("Pending"),
    ESTIMATED("Estimated"),
    DRAFT("Draft"),
    CALCULATED("Calculated"),
    APPROVED("Approved"),
    LOCKED("Locked"),
    PUBLISHED("Published"),
    FROZEN("Frozen");

    private final String value;

    PayrollStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
