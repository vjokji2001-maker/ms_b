package com.smartcare.hrms.common.enumeration;

public enum DayType {
    FIRST_HALF("First Half"),
    SECOND_HALF("Second Half"),
    FULL_DAY("Full Day");


    private final String value;

    DayType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;

    }


    public static DayType fromValue(String value) {
        for (DayType type : DayType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DayType: " + value);
    }
}
