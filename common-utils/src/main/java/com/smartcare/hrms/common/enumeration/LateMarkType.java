package com.smartcare.hrms.common.enumeration;

public enum LateMarkType {
    LATE_IN("Late In"),
	EARLY_OUT("Early Out"),
	BOTH("Both"),;

	private final String displayName;
	
	private LateMarkType(String displayName) {
		this.displayName = displayName;
	}
	
	LateMarkType() {
		this.displayName = null;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
