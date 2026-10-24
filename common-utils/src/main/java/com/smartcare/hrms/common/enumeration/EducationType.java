package com.smartcare.hrms.common.enumeration;

public enum EducationType {

	TENTH("10th"),
    TWELFTH("12th"),
    GRADUATION("Graduation"),
    POST_GRADUATION("Post Graduation"),
    PHD("PhD"),
    DIPLOMA("Diploma"),
    OTHERS("Others");

    private final String label;

    EducationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
