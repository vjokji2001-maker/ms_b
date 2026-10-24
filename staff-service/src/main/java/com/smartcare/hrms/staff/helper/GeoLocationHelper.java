package com.smartcare.hrms.staff.helper;

import org.springframework.stereotype.Component;

@Component
public class GeoLocationHelper {

    // Earth's radius in meters
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * Calculates the great-circle distance between two points on the Earth's surface
     * using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in meters
     */
    public double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(rLat1) * Math.cos(rLat2)
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Checks if coordinates fall within the allowed radius of the target location.
     */
    public boolean isWithinGeofence(double userLat, double userLon, double targetLat, double targetLon, double allowedRadiusMeters) {
        double distance = calculateDistanceMeters(userLat, userLon, targetLat, targetLon);
        return distance <= allowedRadiusMeters;
    }
}
