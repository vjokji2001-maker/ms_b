package com.smartcare.hrms.common.enumeration;

public enum WeekNo {

    WEEK_1("Week 1"),
    WEEK_2("Week 2"),
    WEEK_3("Week 3"),
    WEEK_4("Week 4"),
    WEEK_5("Week 5"),
    WEEK_6("Week 6");

    private final String displayName;

    WeekNo(String displayName) {
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
