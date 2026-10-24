package com.smartcare.hrms.common.enumeration;

public enum SwipeStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    APPROVED("Approved"),
    REJECTED ("Rejected"),
    LAPSED ("Lapsed");


    private final String value;

    SwipeStatus(String value) {
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
