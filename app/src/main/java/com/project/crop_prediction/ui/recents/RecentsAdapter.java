package com.project.crop_prediction.ui.recents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Recent;

import java.util.ArrayList;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.RecentsViewHolder> {

    private Context context;
    private ArrayList<Recent> recents;
    private OnClickListener clickListener;

    public RecentsAdapter(Context context, ArrayList<Recent> recents, OnClickListener clickListener) {
        this.context = context;
        this.recents = recents;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentsViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.layout_recent_card, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentsViewHolder holder, int position) {
        Recent recent = recents.get(position);

        holder.imageView.setImageResource(context.getResources()
                .getIdentifier(recent.prediction.getPredictedClass(),
                        "drawable", context.getPackageName()));
        holder.title.setText(recent.prediction.getPredictedName());
        holder.subtitle.setText("Some Details");

        if(recent.prediction.image != null) {
            holder.imageView.setImageBitmap(recent.prediction.image);
        } else {
            //TODO: Read image from Local File System ot fetch from Firebase Storage
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
