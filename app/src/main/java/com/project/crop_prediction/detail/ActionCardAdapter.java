package com.project.crop_prediction.detail;

import android.content.Context;
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

    public static final Action[] actions = {Action.bookmark,
            Action.saveToPDF, Action.saveImage, Action.saveMap, Action.delete};
    private final int[] actionIcons = {R.drawable.ic_bookmark_outline_24dp,
            R.drawable.ic_save_docblack_24dp, R.drawable.ic_photo_24dp,
            R.drawable.ic_map_24dp, R.drawable.ic_delete_24dp};
    private final String[] actionTitles = {"Add to Bookmarks",
            "Save to PDF", "Save Image to Photos", "Save Map to Photos", "Delete"};
    private final int[] actionColors = {R.color.blue, R.color.blue, R.color.blue,
            R.color.blue, R.color.red};

    private Context context;
    private Recent recent;
    private DetailAdapter.OnClickListener onClickListener;

    public ActionCardAdapter(Context context, Recent recent, DetailAdapter.OnClickListener onClickListener) {
        this.context = context;
        this.recent = recent;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActionCardAdapter.ActionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_action_cell, parent, false), onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        holder.imageView.setImageResource(getIconForAction(actions[position]));
        holder.title.setText(getTitleForAction(actions[position]));

        holder.title.setTextColor(context.getResources().getColor(actionColors[actions[position].rawValue]));
        holder.imageView.setColorFilter(context.getResources().getColor(actionColors[actions[position].rawValue]));
    }

    @Override
    public int getItemCount() {
        return actions.length;
    }

    private int getIconForAction(Action action) {
        int icon = actionIcons[action.rawValue];

        if (action == Action.bookmark && recent.bookmarked)
            icon = R.drawable.ic_bookmark_24dp;

        return icon;
    }

    private String getTitleForAction(Action action) {
        String title = actionTitles[action.rawValue];

        if (action == Action.bookmark && recent.bookmarked)
            title = "Remove from Bookmarks";

        return title;
    }

    public void notifyBookmarkChanged() {
        int position = -1;

        for (int i = 0; i < actions.length; i++)
            if (actions[i] == Action.bookmark)
                position = i;

        if (position == -1)
            return;

        notifyItemChanged(position);
    }

    public enum Action {
        bookmark(0), saveToPDF(1), saveImage(2),
        saveMap(3), delete(4);

        public int rawValue;

        Action(int rawValue) {
            this.rawValue = rawValue;
        }
    }

    public class ActionViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView imageView;

        public ActionViewHolder(View view, final DetailAdapter.OnClickListener onClickListener) {
            super(view);

            title = view.findViewById(R.id.action_cell_title);
            imageView = view.findViewById(R.id.action_cell_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onActionPerformed(actions[getAdapterPosition()]);
                }
            });
        }
    }
}
