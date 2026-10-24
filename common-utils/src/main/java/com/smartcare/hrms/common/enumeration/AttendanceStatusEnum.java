package com.smartcare.hrms.common.enumeration;

public enum AttendanceStatusEnum {
    NA("Not Allocated"),
    PENDING("Pending"),
    IN_PROCESS("In Process"),
    RECALCULATE("Re Calculate"),
    PROCESED("Processed"),
    LOCKED("Locked"),
    FROZEN("Frozen");

    private final String value;

    AttendanceStatusEnum(String value) {
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



