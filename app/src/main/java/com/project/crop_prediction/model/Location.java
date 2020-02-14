package com.project.crop_prediction.model;

import androidx.annotation.NonNull;

public class Location {

    double lat;
    double lon;
    double alt;

    public Location(double lat, double lon, double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    @NonNull
    @Override
    public String toString() {
        return "Location(lat: " + lat + ", lon: " + lon + ", alt: " + alt + ")";
    }
}
