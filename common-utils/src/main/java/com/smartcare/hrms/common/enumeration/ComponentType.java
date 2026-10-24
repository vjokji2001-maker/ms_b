package com.smartcare.hrms.common.enumeration;

public enum ComponentType {
    EARNINGS("Earnings"),
    DEDUCTIONS("Deductions"),
    STATUTORY_DEDUCTIONS("Statutory Deductions");

    private final String displayName;

    ComponentType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Human-friendly label for the enum constant.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return the display name when printing the enum.
     */
    @Override
    public String toString() {
        return displayName;
    }

}
