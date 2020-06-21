package com.project.crop_prediction.detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Recent;

public class ActionCardAdapter extends RecyclerView.Adapter<ActionCardAdapter.ActionViewHolder> {

    public static final int ACTION_BOOKMARK = 0;
    public static final int ACTION_SAVE_TO_PDF = 1;
    public static final int ACTION_SAVE_IMAGE = 2;
    public static final int ACTION_SAVE_MAP = 3;
    public static final int ACTION_DELETE = 4;
    public static final int[] actions = {ACTION_BOOKMARK, ACTION_SAVE_TO_PDF, ACTION_SAVE_IMAGE, ACTION_SAVE_MAP, ACTION_DELETE};

    private final int[] actionIcons = {R.drawable.ic_bookmark_outline_24dp,
            R.drawable.ic_save_docblack_24dp, R.drawable.ic_photo_24dp,
            R.drawable.ic_map_24dp, R.drawable.ic_delete_24dp};
    private final String[] actionTitles = {"Add to Bookmarks",
            "Save to PDF", "Save Image to Photos", "Save Map to Photos", "Delete"};
    private final int[] actionColors = {R.color.blue, R.color.blue, R.color.blue,
            R.color.blue, R.color.red};

    private Context context;
    private Recent recent;

    public ActionCardAdapter(Context context, Recent recent) {
        this.context = context;
        this.recent = recent;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActionCardAdapter.ActionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_action_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        holder.imageView.setImageResource(getIconForAction(actions[position]));
        holder.title.setText(getTitleForAction(actions[position]));

        holder.title.setTextColor(context.getResources().getColor(actionColors[actions[position]]));
        holder.imageView.setColorFilter(context.getResources().getColor(actionColors[actions[position]]));
    }

    @Override
    public int getItemCount() {
        return actions.length;
    }

    private int getIconForAction(int action) {
        int icon = actionIcons[action];

        if(action == ACTION_BOOKMARK && recent.bookmarked)
            icon = R.drawable.ic_bookmark_24dp;

        return icon;
    }

    private String getTitleForAction(int action) {
        String title = actionTitles[action];

        if(action == ACTION_BOOKMARK && recent.bookmarked)
            title = "Remove from Bookmarks";

        return title;
    }



    public class ActionViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView imageView;

        public ActionViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.action_cell_title);
            imageView = view.findViewById(R.id.action_cell_icon);
        }
    }
}
