package com.project.crop_prediction.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.crop_prediction.R;
import com.project.crop_prediction.model.InfoCell;
import com.project.crop_prediction.model.Recent;

import java.util.ArrayList;

public class DetailAdapter extends RecyclerView.Adapter {

    private final int TYPE_IMAGE = 1;
    private final int TYPE_INFO_LIST = 2;
    private final int TYPE_MAP = 3;
    private final int TYPE_ACTION = 4;
    private final int[] viewTypes = {TYPE_IMAGE, TYPE_INFO_LIST};

    private Context context;
    private Recent recent;

    public DetailAdapter(Context context, Recent recent) {
        this.context = context;
        this.recent = recent;
    }

    @Override
    public int getItemViewType(int position) {
        return viewTypes[position];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case TYPE_IMAGE:
                viewHolder = new ImageViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_image_card, parent, false));
                break;

            case TYPE_INFO_LIST:
                viewHolder = new InfoListViewHolder(context, LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_info_list_card, parent, false), recent.getInfoList());
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (viewTypes[position]) {
            case TYPE_IMAGE:
                ImageViewHolder imgholder = (ImageViewHolder) holder;
                imgholder.imageView.setImageResource(context.getResources()
                        .getIdentifier(recent.prediction.getPredictedClass(),
                                "drawable", context.getPackageName()));


                if (recent.prediction.image != null)
                    imgholder.imageView.setImageBitmap(recent.prediction.image);
                else {
                    // TODO: Get Image from Local File System or Firebase storage
                    //  and set to recent.prediction.image
                }

                break;

            case TYPE_INFO_LIST:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return viewTypes.length;
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.image_card_image);
        }

    }

    private static class InfoListViewHolder extends RecyclerView.ViewHolder {

        public RecyclerView recyclerView;
        private InfoListAdapter mAdapter;
        private LinearLayoutManager layoutManager;

        public InfoListViewHolder(Context context, View view, ArrayList<InfoCell> infos) {
            super(view);

            recyclerView = view.findViewById(R.id.info_list_recycler);
            layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new InfoListAdapter(infos);
            recyclerView.setAdapter(mAdapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());

            recyclerView.addItemDecoration(dividerItemDecoration);
        }

    }

}
