package com.project.crop_prediction.model;

import android.location.Location;

import androidx.annotation.NonNull;

public class Coordinate {

    double lat;
    double lon;
    double alt;

    public Coordinate(double lat, double lon, double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public Coordinate(Location location) {
        this(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    @NonNull
    @Override
    public String toString() {
        return "Coordinate(lat: " + lat + ", lon: " + lon + ", alt: " + alt + ")";
    }
}
