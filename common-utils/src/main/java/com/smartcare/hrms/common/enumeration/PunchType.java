package com.smartcare.hrms.common.enumeration;

public enum PunchType {

    MACHINE ("Machine"),
    MANUAL ("Manual");

    private final String value;

    PunchType(String value) {
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
