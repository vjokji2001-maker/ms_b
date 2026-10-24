package com.smartcare.hrms.attendance.helper;

import java.util.Set;

public class AttendanceStatusResolver {


    private static final Set<String> KNOWN_TYPES = Set.of(
            "DP", "ABS", "WO", "WOP", "PH", "PHP", "OD"
    );

    private static boolean isLeave(String type) {
        if (type == null) return false;
        return !KNOWN_TYPES.contains(type.trim().toUpperCase());
    }

    public static String resolveStatus(String dayType, String dayType1) {

        if (dayType == null) return null;

        String d1 = dayType.trim().toUpperCase();
        String d2 = (dayType1 != null && !dayType1.trim().isEmpty())
                ? dayType1.trim().toUpperCase()
                : null;

        // ---- Full Day ----
        if (d1.equals("DP")  && d2 == null) return "DP";
        if (d1.equals("WO")  && d2 == null) return "WO";
        if (d1.equals("WOP") && d2 == null) return "WOP";
        if (d1.equals("PH")  && d2 == null) return "PH";
        if (d1.equals("PHP") && d2 == null) return "PHP";
        if (d1.equals("OD")  && d2 == null) return "OD";
        if (d1.equals("ABS") && d2 == null) return "ABS";

        // Any leave type full day (CL, SL, EL, ML, LWP etc.)
        if (isLeave(d1) && d2 == null) return d1;

        // ---- Half Day ----

        // DP + ABS
        if (d1.equals("DP")  && "ABS".equals(d2)) return "HD";
        if (d1.equals("ABS") && "DP".equals(d2))  return "HD";

        // DP + Any Leave (CL/SL/EL/ML/LWP...)
        if (d1.equals("DP") && isLeave(d2)) return "HD";
        if (isLeave(d1)     && "DP".equals(d2)) return "HD";

        // DP + OD
        if (d1.equals("DP") && "OD".equals(d2)) return "HD";
        if (d1.equals("OD") && "DP".equals(d2)) return "HD";

        // DP + WO
        if (d1.equals("DP") && "WO".equals(d2)) return "HD";
        if (d1.equals("WO") && "DP".equals(d2)) return "HD";

        // DP + PH
        if (d1.equals("DP") && "PH".equals(d2)) return "HD";
        if (d1.equals("PH") && "DP".equals(d2)) return "HD";

        // Leave + ABS
        if (isLeave(d1) && "ABS".equals(d2)) return "HD";
        if (d1.equals("ABS") && isLeave(d2)) return "HD";

        // Two different leave types (CL + SL etc.)
        if (isLeave(d1) && isLeave(d2)) return "HD";

        // OD + ABS
        if (d1.equals("OD") && "ABS".equals(d2)) return "HD";
        if (d1.equals("ABS") && "OD".equals(d2)) return "HD";

        // OD + Leave
        if (d1.equals("OD") && isLeave(d2)) return "HD";
        if (isLeave(d1) && "OD".equals(d2)) return "HD";

        // Fallback
        return d1;
    }
}
