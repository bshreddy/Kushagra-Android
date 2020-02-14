package com.project.crop_prediction.model;

import java.util.Date;

public class Recent {

    public String id;
    public Prediction prediction;
    public Boolean bookmarked;
    public Date createdAt;
    public Location location;

    public Recent(String id, Prediction prediction, Boolean bookmarked, Date createdAt, Location location) {
        this.id = id;
        this.prediction = prediction;
        this.bookmarked = bookmarked;
        this.createdAt = createdAt;
        this.location = location;
    }

    public Recent(Prediction prediction, Boolean bookmarked, Date createdAt, Location location) {
        this(null, prediction, bookmarked, createdAt, location);
    }

    public String toString() {
        return "Recent(id: " + id + ", prediction: " + prediction + ", bookmarked: " + bookmarked +
                ", createdAt: " + createdAt + ", location: " + location + ")";
    }
}
