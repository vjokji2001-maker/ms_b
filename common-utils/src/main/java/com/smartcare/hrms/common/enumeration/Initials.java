package com.smartcare.hrms.common.enumeration;

public enum Initials {
    MR("Mr."),
    MRS("Mrs."),
    MS("Ms."),
    MX("Mx."),
    DR("Dr."),
    PROF("Prof.");

    private final String label;

    Initials(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
