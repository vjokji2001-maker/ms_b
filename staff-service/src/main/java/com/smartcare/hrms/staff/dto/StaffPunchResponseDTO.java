package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPunchResponseDTO {

    private String status; // SUCCESS / FAILED
    private String message;
    private String punchDate;
    private String punchTime;
    private String direction; // IN / OUT

    // Geo Validation Details
    private Boolean locationVerified;
    private Double distanceFromOfficeMeters;
    private Boolean withinGeofence;

    // Face Authentication Details
    private Boolean faceVerified;
    private String selfieUrl;

    // Working info
    private String shiftName;
    private String totalWorkingHoursToday;
}
