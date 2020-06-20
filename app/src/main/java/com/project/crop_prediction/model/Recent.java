package com.project.crop_prediction.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Recent implements Parcelable {

    public interface OnSuccessListener {
        void onSuccess(Bitmap image);
    }

    enum CodingKeys {
        prediction("pred"), createdAt("crtdAt"), bookmarked("bkmrkd"), location("loc");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

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

    public Recent(Prediction prediction, Date createdAt) {
        this(null, prediction, false, createdAt, null);
    }

    protected Recent(Parcel in) {
        id = in.readString();
        prediction = in.readParcelable(Prediction.class.getClassLoader());
        byte tmpBookmarked = in.readByte();
        bookmarked = tmpBookmarked == 0 ? null : tmpBookmarked == 1;
        createdAt = new Date(in.readLong());
        coordinate = in.readParcelable(Coordinate.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(prediction, flags);
        dest.writeByte((byte) (bookmarked == null ? 0 : bookmarked ? 1 : 2));
        dest.writeLong(createdAt.getTime());
        dest.writeParcelable(coordinate, flags);
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

    public ArrayList<InfoCell> getInfoList() {
        return Recent.getInfoList(this);
    }

    public static ArrayList<InfoCell> getInfoList(Recent recent) {
        ArrayList<InfoCell> infos = new ArrayList<>();

        if(recent == null) {
            infos.add(new InfoCell("N/A", "Name"));
            infos.addAll(Coordinate.getInfoList(null));
        } else {
            infos.add(new InfoCell(recent.prediction.getPredictedName(), "Name"));
            infos.addAll(recent.coordinate.getInfoList());
        }

        return infos;
    }

    public String toString() {
        return "Recent(id: " + id + ", prediction: " + prediction + ", bookmarked: " + bookmarked +
                ", createdAt: " + createdAt + ", coordinate: " + coordinate + ")";
    }

    public void getImage(FirebaseUser user, StorageReference recentImagesRef, File picsDir, final OnSuccessListener onSuccessListener) {
        if(prediction.image != null) {
            onSuccessListener.onSuccess(prediction.image);
        } else if(user != null && recentImagesRef != null) {
            final String imgName = prediction.getPredictedClass() + "/" + user.getUid() + '-' + id + ".png";
            File dir = new File(picsDir, prediction.getPredictedClass());
            final File imageFile = new File(picsDir, imgName);

            if(imageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                prediction.image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                onSuccessListener.onSuccess(prediction.image);
            }
            else {
                if (!dir.exists())
                    dir.mkdirs();

                recentImagesRef.child(imgName).getFile(imageFile)
                    .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            try {
                                imageFile.createNewFile();

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                prediction.image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                                onSuccessListener.onSuccess(prediction.image);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
            }

        }
    }
}
