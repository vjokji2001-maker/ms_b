package com.smartcare.hrms.common.enumeration;

public enum ApplicationType {
    LEAVE_RQST("Leave Request"),
    OD_RQST ("Od Request"),
    LEAVE_CANCEL("Leave cancel"),
    OD_CANCEL("Od cancel");

    private String value;

    ApplicationType(String value){this.value= value;}

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;

    }


    public static ApplicationType fromValue(String value) {
        for (ApplicationType type : ApplicationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ApplicationType: " + value);
    }
}
