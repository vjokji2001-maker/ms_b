package com.smartcare.hrms.common.utility;

public class EnumConverter {


   /**
    *  Convert String to ENUM (for POST - DTO to Entity)
    * */
    public static <E extends Enum<E>> E toEnum(String value, Class<E> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmedValue = value.trim();

        // First, try to match by display name (toString)
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.toString().equalsIgnoreCase(trimmedValue)) {
                return enumConstant;
            }
        }

        // If not found, try by enum constant name with underscores
        String normalizedValue = trimmedValue.toUpperCase().replace(" ", "_");
        return Enum.valueOf(enumClass, normalizedValue);
    }

    /**
     * Convert ENUM to String (for GET - Entity to DTO/Projection)
     */
    public static <E extends Enum<E>> String toDisplayName(E enumValue) {
        if (enumValue == null) {
            return null;
        }

        // Try to get displayName via toString() method
        String displayValue = enumValue.toString();

        // If toString() returns null or same as name(), return name()
        if (displayValue == null || displayValue.equals(enumValue.name())) {
            return enumValue.name();
        }
        return displayValue;
    }

    /**
     * Convert String (ENUM name from DB) to Display String (for Bean/Projection to DTO)
     */
    public static <E extends Enum<E>> String toDisplayString(String enumName, Class<E> enumClass) {
        if (enumName == null || enumName.trim().isEmpty()) {
            return null;
        }
        E enumValue = toEnum(enumName, enumClass);
        return toDisplayName(enumValue);
    }
}
