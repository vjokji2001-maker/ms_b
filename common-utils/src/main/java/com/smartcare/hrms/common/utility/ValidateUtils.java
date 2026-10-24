package com.smartcare.hrms.common.utility;

public class ValidateUtils {

    private static final String acceptNamePattern = "^[A-Za-z0-9 .\\-_/()]+$";
    private static final String acceptCodePattern = "^[A-Za-z0-9]+$";;
    private static final String acceptEmailPattern = "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isNullObject(Object object) {
        return object == null;
    }

    public static boolean isStringEmpty(String val) {
        return val == null || val.isBlank();
    }

    public static boolean isZero(Integer val) {
        return val == null || val == 0;
    }

    public static boolean isLongZero(Long val) {
        return val == null || val == 0;
    }

    public static boolean isZero(Float val) {
        return val == null || val == 0.0f;
    }

    public static boolean isZero(Double val) {
        return val == null || val == 0.0;
    }

    public static boolean isFalse(Boolean val) {
        return val == null || !val;
    }

    public static boolean isTrue(Boolean val) {
        return val != null && val;
    }

    public static boolean isNegative(Integer val) {
        return val != null && val < 0;
    }

    public static boolean isNegative(Double val) {
        return val != null && val < 0;
    }

    public static boolean isNegative(Float val) {
        return val != null && val < 0;
    }

    /* Uppercase A–Z, Lowercase a–z, 0-9, dot ., white space, hyphen -, underscore _, forward slash /, round brackets ()  */
    public static boolean isValidNameFormat(String format){
        return format.matches(acceptNamePattern);
    }

    /* Uppercase A–Z, Lowercase a–z, digits 0 - 9 */
    public static boolean isValidCodeFormat(String format){
        return format.matches(acceptCodePattern);
    }

    /* lowercase letters / digits / dots / underscores + @ + lowercase letters / digits / dots / underscores */
    public static boolean isValidEmailFormat(String format){
        if (format == null || format.trim().isEmpty()){
            return true;
        }
        return format.matches(acceptEmailPattern);
    }
}
