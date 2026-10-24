package com.smartcare.hrms.common.enumeration;

public enum BloodGroup {
    A_POSITIVE("A+ve"),
    A_NEGATIVE("A-ve"),
    B_POSITIVE("B+ve"),
    B_NEGATIVE("B-ve"),
    AB_POSITIVE("AB+ve"),
    AB_NEGATIVE("AB-ve"),
    O_POSITIVE("O+ve"),
    O_NEGATIVE("O-ve");
	

	
    private final String displayName;

    BloodGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
