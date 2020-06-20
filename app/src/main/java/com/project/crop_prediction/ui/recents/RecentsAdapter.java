package com.project.crop_prediction.ui.recents;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Recent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.RecentsViewHolder> {

    private static final String TAG = "RecentsAdapter";

    private Context context;
    private ArrayList<Recent> recents;
    private OnClickListener clickListener;
    private File picsDir;
    private FirebaseUser user;
    private StorageReference recentImagesRef;

    public RecentsAdapter(Context context, ArrayList<Recent> recents,
                          File picsDir, OnClickListener clickListener) {
        this.context = context;
        this.recents = recents;
        this.picsDir = picsDir;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.recentImagesRef = FirebaseStorage.getInstance().getReference().child("images/");
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentsViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.layout_recent_card, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecentsViewHolder holder, int position) {
        final Recent recent = recents.get(position);

        holder.imageView.setImageResource(context.getResources()
                .getIdentifier(recent.prediction.getPredictedClass(),
                        "drawable", context.getPackageName()));
        holder.title.setText(recent.prediction.getPredictedName());
        holder.subtitle.setText("Some Details");

        if(recent.prediction.image != null) {
            holder.imageView.setImageBitmap(recent.prediction.image);
        } else if(user != null && recentImagesRef != null) {
            final String imgName = recent.prediction.getPredictedClass() + "/" + user.getUid() + '-' + recent.id + ".png";
            File dir = new File(picsDir, recent.prediction.getPredictedClass());
            final File imageFile = new File(picsDir, imgName);

            if(imageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                recent.prediction.image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                holder.imageView.setImageBitmap(recent.prediction.image);
            }
            else {
                if (!dir.exists())
                    dir.mkdirs();

                Log.d(TAG, "onBindViewHolder: Starting");
                recentImagesRef.child(imgName).getFile(imageFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                try {
                                    imageFile.createNewFile();

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    recent.prediction.image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                                    holder.imageView.setImageBitmap(recent.prediction.image);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
            }

        }


    }

    @Override
    public int getItemCount() {
        return recents.size();
    }

    public void reloadData() {
        this.notifyDataSetChanged();
    }

    public interface OnClickListener{
        void onClick(int position);
        void onBookmarkClick(int position);
    }

    public static class RecentsViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView title, subtitle;
        public ImageButton bookmark;

        public RecentsViewHolder(View view, final OnClickListener clickListener) {
            super(view);

            imageView = view.findViewById(R.id.recent_image);
            title = view.findViewById(R.id.recent_title);
            subtitle = view.findViewById(R.id.recent_subtitle);
            bookmark = view.findViewById(R.id.bookmark_button);

            if(clickListener != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickListener.onClick(getAdapterPosition());
                    }
                });
            }

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onBookmarkClick(getAdapterPosition());
                }
            });
        }
    }

}
