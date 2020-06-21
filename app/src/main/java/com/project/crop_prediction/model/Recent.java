package com.project.crop_prediction.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.project.crop_prediction.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Recent implements Parcelable {

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

    public static ArrayList<InfoCell> getInfoList(Context context, Recent recent) {
        ArrayList<InfoCell> infos = new ArrayList<>();

        if (recent == null) {
            infos.add(new InfoCell(context.getString(R.string.not_available),
                    context.getString(R.string.info_subtitle_name)));
            infos.addAll(Coordinate.getInfoList(context, null));
        } else {
            infos.add(new InfoCell(recent.prediction.getPredictedName(context),
                    context.getString(R.string.info_subtitle_name)));
            infos.addAll(recent.coordinate.getInfoList(context));
        }

        return infos;
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

    public ArrayList<InfoCell> getInfoList(Context context) {
        return Recent.getInfoList(context, this);
    }

    public String toString() {
        return "Recent(id: " + id + ", prediction: " + prediction + ", bookmarked: " + bookmarked +
                ", createdAt: " + createdAt + ", coordinate: " + coordinate + ")";
    }

    public void getImage(FirebaseUser user, StorageReference recentImagesRef, File picsDir, final OnImageLoadListener onImageLoadListener) {
        if (prediction.image != null) {
            onImageLoadListener.onImageLoad(prediction.image);
        } else if (user != null && recentImagesRef != null) {
            final String imgName = prediction.getPredictedClass() + "/" + user.getUid() + '-' + id + ".png";
            File dir = new File(picsDir, prediction.getPredictedClass());
            final File imageFile = new File(picsDir, imgName);

            if (imageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                prediction.image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                onImageLoadListener.onImageLoad(prediction.image);
            } else {
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
                                    onImageLoadListener.onImageLoad(prediction.image);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
            }

        }
    }

    enum CodingKeys {
        prediction("pred"), createdAt("crtdAt"), bookmarked("bkmrkd"), location("loc");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    public interface OnImageLoadListener {
        void onImageLoad(Bitmap image);
    }

    public interface OnBookmarkUpdatedListener {
        void onComplete(Recent recent, Exception ex);
    }
}
