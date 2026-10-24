package com.smartcare.hrms.common.enumeration;

public enum ApplicationStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    APPROVED("Approved"),
    REJECTED ("Rejected"),
    LAPSED ("Lapsed"),
    CANCELED("Canceled");


    private final String value;

    ApplicationStatus(String value) {
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
