package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPunchRequestDTO {

    // Employee UUID from login
    private String empId;

    // "IN" or "OUT"
    private String punchDirection;

    // Mobile GPS Coordinates
    private Double latitude;
    private Double longitude;
    private String locationAddress;

    // Base64 selfie / face captured via mobile camera
    private String faceImageBase64;

    // Target office coordinates (optional override if known, otherwise branch coordinates are used)
    private Double targetLatitude;
    private Double targetLongitude;
    private Double allowedRadiusMeters;

    // True if employee has permission for outdoor / remote / on-duty punch
    private Boolean isRemotePunch;

    // Mobile device metadata
    private String deviceInfo;
}
