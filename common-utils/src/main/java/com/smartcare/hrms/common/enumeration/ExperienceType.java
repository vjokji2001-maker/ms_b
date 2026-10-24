package com.smartcare.hrms.common.enumeration;

public enum ExperienceType {

    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    INTERNSHIP("Internship"),
    CONTRACT("Contract"),
    FREELANCE("Freelance"),
    TEMPORARY("Temporary");


    private final String displayName;

    ExperienceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
