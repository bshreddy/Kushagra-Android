package com.project.crop_prediction.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Coordinate implements Parcelable {

    public static final Creator<Coordinate> CREATOR = new Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(Parcel in) {
            return new Coordinate(in);
        }

        @Override
        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };
    public double lat;
    public double lon;
    public double alt;

    public Coordinate(double lat, double lon, double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public Coordinate(Location location) {
        this(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    protected Coordinate(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        alt = in.readDouble();
    }

    public static ArrayList<InfoCell> getInfoList(Coordinate coordinate) {
        ArrayList<InfoCell> infos = new ArrayList<>();

        if (coordinate == null) {
            infos.add(new InfoCell("N/A", "Latitude"));
            infos.add(new InfoCell("N/A", "Longitude"));
            infos.add(new InfoCell("N/A", "Altitude"));
        } else {
            infos.add(new InfoCell(coordinate.lat + "\u00B0 N", "Latitude"));
            infos.add(new InfoCell(coordinate.lon + "\u00B0 E", "Longitude"));
            infos.add(new InfoCell(coordinate.alt + " m above MSE", "Altitude"));
        }

        return infos;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeDouble(alt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<InfoCell> getInfoList() {
        return Coordinate.getInfoList(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "Coordinate(lat: " + lat + ", lon: " + lon + ", alt: " + alt + ")";
    }

    enum CodingKeys {
        lat("lat"), lon("long"), alt("altitude");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }
}
