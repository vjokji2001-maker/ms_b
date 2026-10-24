package com.smartcare.hrms.common.enumeration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DurationEnum {

    FIRST_HALF("First Half"),
    SECOND_HALF("Second Half"),
    FULL_DAY("Full Day");


    private final String value;

    DurationEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // O(1) lookup map
    private static final Map<String, DurationEnum> VALUE_MAP =
            Arrays.stream(DurationEnum.values())
                    .collect(Collectors.toMap(
                            e -> e.value.toLowerCase(),
                            e -> e
                    ));

    public static DurationEnum fromValue(String value) {
        DurationEnum result = VALUE_MAP.get(value.toLowerCase());
        if (result == null) {
            throw new IllegalArgumentException("Invalid DurationEnum value: " + value);
        }
        return result;
    }

    @Override
    public String toString() {
        return value;
    }
}
