package com.smartcare.hrms.common.utility;

public class PatternConstants {

    public static final String ALPHA_ONLY = "^[A-Za-z ]+$";
    public static final String COMPONENT_NAME_PATTERN = "^[A-Za-z0-9+ /%]+$";
    public static final String UPPERCASE_UNDERSCORE = "^[A-Z]+(_[A-Z]+)*$";
    public static final String ALPHA_NO_SPACE = "^[A-Za-z]+$";
    public static final String NUMERIC_ONLY = "^[0-9]+$";
    public static final String ALPHANUMERIC_ONLY = "^[A-Za-z0-9]+$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,}$";
    public static final String PHONE_NUMBER_PATTERN = "^[0-9]{10}$";
    public static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$"; // YYYY-MM-DD format
}
