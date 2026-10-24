package com.smartcare.hrms.common.enumeration;

public enum AddressType {
    CURRENT("Current"),
    PAST("Past");

    private final String value;

    AddressType(String value) {
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
