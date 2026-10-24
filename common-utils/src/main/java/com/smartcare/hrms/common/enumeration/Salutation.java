package com.smartcare.hrms.common.enumeration;

public enum Salutation {
    MR("Mr."),
    MS("Ms."),
    MRS("Mrs."),
    DR("Dr."),
    PROF("Prof.");

    private final String displayName;

    Salutation(String displayName) {
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
