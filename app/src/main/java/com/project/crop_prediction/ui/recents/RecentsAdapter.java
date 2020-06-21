package com.project.crop_prediction.ui.recents;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Recent;

import java.io.File;
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
        holder.bookmark.setImageResource(recent.bookmarked ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_outline_24dp);

        recent.getImage(user, recentImagesRef, picsDir, new Recent.OnSuccessListener() {
            @Override
            public void onSuccess(Bitmap image) {
                holder.imageView.setImageBitmap(recent.prediction.image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recents.size();
    }

    public void reloadData() {
        this.notifyDataSetChanged();
    }

    public interface OnClickListener {
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

            if (clickListener != null) {
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
