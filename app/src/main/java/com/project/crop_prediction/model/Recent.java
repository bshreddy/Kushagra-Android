package com.project.crop_prediction.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Recent implements Parcelable {

    public String id;
    public Prediction prediction;
    public Boolean bookmarked;
    public Date createdAt;
    public Coordinate coordinate;

    public Recent(String id, Prediction prediction, Boolean bookmarked, Date createdAt, Coordinate coordinate) {
        this.id = id;
        this.prediction = prediction;
        this.bookmarked = bookmarked;
        this.createdAt = createdAt;
        this.coordinate = coordinate;
    }

    public Recent(Prediction prediction, Boolean bookmarked, Date createdAt, Coordinate coordinate) {
        this(null, prediction, bookmarked, createdAt, coordinate);
    }

    protected Recent(Parcel in) {
        id = in.readString();
        byte tmpBookmarked = in.readByte();
        bookmarked = tmpBookmarked == 0 ? null : tmpBookmarked == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (bookmarked == null ? 0 : bookmarked ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Recent> CREATOR = new Creator<Recent>() {
        @Override
        public Recent createFromParcel(Parcel in) {
            return new Recent(in);
        }

        @Override
        public Recent[] newArray(int size) {
            return new Recent[size];
        }
    };

    public String toString() {
        return "Recent(id: " + id + ", prediction: " + prediction + ", bookmarked: " + bookmarked +
                ", createdAt: " + createdAt + ", coordinate: " + coordinate + ")";
    }
}
