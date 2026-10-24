package com.smartcare.hrms.common.enumeration;

public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married"),
    DIVORCED("Divorced"),
    WIDOWED("Widowed"),
    SEPARATED("Separated");

    private final String displayName;

    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
