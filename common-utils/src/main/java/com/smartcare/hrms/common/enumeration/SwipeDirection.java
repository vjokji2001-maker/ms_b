package com.smartcare.hrms.common.enumeration;

public enum SwipeDirection {

    IN("In"),
    OUT("Out"),
    BOTH("Both");

    private final String value;

    SwipeDirection(String value) {
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
