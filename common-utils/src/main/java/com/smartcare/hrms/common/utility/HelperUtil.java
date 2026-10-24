package com.smartcare.hrms.common.utility;

public class HelperUtil {

    public static Boolean toBoolean(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.TRUE.equals(value);
    }

}
