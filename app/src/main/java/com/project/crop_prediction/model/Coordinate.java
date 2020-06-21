package com.project.crop_prediction.model;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.project.crop_prediction.R;

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

    public static ArrayList<InfoCell> getInfoList(Context context, Coordinate coordinate) {
        ArrayList<InfoCell> infos = new ArrayList<>();

        infos.add(new InfoCell((coordinate == null)? context.getString(R.string.not_available):
                coordinate.lat + "\u00B0 N", context.getString(R.string.info_subtitle_latitude)));
        infos.add(new InfoCell((coordinate == null)? context.getString(R.string.not_available):
                coordinate.lon + "\u00B0 E", context.getString(R.string.info_subtitle_longitude)));
        infos.add(new InfoCell((coordinate == null)? context.getString(R.string.not_available):
                coordinate.alt + " m above MSE", context.getString(R.string.info_subtitle_altitude)));

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

    public ArrayList<InfoCell> getInfoList(Context context) {
        return Coordinate.getInfoList(context, this);
    }

    @NonNull
    @Override
    public String toString() {
        return "Coordinate(lat: " + lat + ", lon: " + lon + ", alt: " + alt + ")";
    }

    @SuppressWarnings("HardCodedStringLiteral")
    enum CodingKeys {
        lat("lat"), lon("long"), alt("altitude");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }
}
