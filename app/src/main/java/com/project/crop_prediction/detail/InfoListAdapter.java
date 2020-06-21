package com.project.crop_prediction.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.crop_prediction.R;
import com.project.crop_prediction.model.InfoCell;

import java.util.ArrayList;

public class InfoListAdapter extends RecyclerView.Adapter<InfoListAdapter.InfoViewHolder> {

    private ArrayList<InfoCell> infos;

    public InfoListAdapter(ArrayList<InfoCell> infos) {
        this.infos = infos;
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InfoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_info_list_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        InfoCell info = infos.get(position);

        holder.title.setText(info.title);
        holder.subtitle.setText(info.subtitle + ": ");
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subtitle;

        public InfoViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.info_list_cell_title);
            subtitle = view.findViewById(R.id.info_list_cell_subtitle);
        }

    }

}
