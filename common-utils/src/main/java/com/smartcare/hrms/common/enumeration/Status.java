package com.smartcare.hrms.common.enumeration;

public enum Status {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String displayName;

    Status(String displayName) {
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
