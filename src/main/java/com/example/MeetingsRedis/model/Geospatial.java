package com.example.MeetingsRedis.model;

public class Geospatial {
    private final double latitude;
    private final double longitude;

    public Geospatial(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static Geospatial fromString(String coordinates) throws IllegalArgumentException {
        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected format: 'longitude,latitude'");
        }
        try {
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            return new Geospatial(latitude, longitude);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate values. Must be valid numbers.", e);
        }
    }

    @Override
    public String toString() {
        return longitude + "," + latitude;
    }
}
