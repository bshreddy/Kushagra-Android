package com.project.crop_prediction.detail;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Coordinate;
import com.project.crop_prediction.model.CropDetails;
import com.project.crop_prediction.model.CropDetailsDeserializer;
import com.project.crop_prediction.model.InfoCell;
import com.project.crop_prediction.model.Recent;

import java.util.ArrayList;

public class DetailAdapter extends RecyclerView.Adapter {

    private static final String TAG = "DetailAdapter";

    private final int TYPE_IMAGE = 1;
    private final int TYPE_INFO_LIST = 2;
    private final int TYPE_MAP = 3;
    private final int TYPE_ACTION = 4;
    private final int[] viewTypes = {TYPE_IMAGE, TYPE_INFO_LIST, TYPE_MAP};

    private StorageReference storageReference;
    private FirebaseUser user;

    private Context context;
    private Recent recent;
    private CropDetails cropDetails;

    public DetailAdapter(Context context, Recent recent) {
        this.context = context;
        this.recent = recent;
        cropDetails = null;
    }

    @Override
    public int getItemViewType(int position) {
        return viewTypes[position];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        user= FirebaseAuth.getInstance().getCurrentUser();

        String fileURL="/images/"+recent.prediction.getPredictedClass()+"/"+user.getUid()+"-"+recent.id+".png";
        storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(fileURL);

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case TYPE_IMAGE:
                viewHolder = new ImageViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_image_card, parent, false));
                break;

            case TYPE_INFO_LIST:
                viewHolder = new InfoListViewHolder(context, LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_info_list_card, parent, false), getInfoList());
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                FirebaseFirestore.getInstance().collection("details")
                        .document(recent.prediction.getPredictedClass()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Gson gson = new GsonBuilder()
                                        .registerTypeAdapter(CropDetails.class, new CropDetailsDeserializer())
                                        .create();

                                cropDetails = gson.fromJson(gson.toJsonTree(documentSnapshot.getData()), CropDetails.class);
                                ((InfoListViewHolder) finalViewHolder).setInfos(getInfoList());
                            }
                        });
                break;

            case TYPE_MAP:
                viewHolder = new MapViewHolder(context, LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_map_card, parent, false), recent.coordinate);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (viewTypes[position]) {
            case TYPE_IMAGE:
                ImageViewHolder imgHolder = (ImageViewHolder) holder;
                imgHolder.imageView.setImageResource(context.getResources()
                        .getIdentifier(recent.prediction.getPredictedClass(),
                                "drawable", context.getPackageName()));

                if (recent.prediction.image != null)
                    imgHolder.imageView.setImageBitmap(recent.prediction.image);
                else {
                    Glide.with(context)
                            .load(storageReference)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgHolder.imageView);
                }

                break;

            case TYPE_INFO_LIST:
                final InfoListViewHolder listViewHolder = (InfoListViewHolder) holder;
                break;

            case TYPE_MAP:
                MapViewHolder mapViewHolder = (MapViewHolder) holder;
                mapViewHolder.initalizeMap();
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

    private ArrayList<InfoCell> getInfoList() {
        ArrayList<InfoCell> infos = new ArrayList<>();
        infos.addAll(recent.getInfoList());

        if(cropDetails != null)
            infos.addAll(cropDetails.getInfoList());
        return infos;
    }

    private static class InfoListViewHolder extends RecyclerView.ViewHolder {

        public RecyclerView recyclerView;
        private InfoListAdapter mAdapter;
        private LinearLayoutManager layoutManager;
        ArrayList<InfoCell> infos;

        public InfoListViewHolder(Context context, View view, ArrayList<InfoCell> infos) {
            super(view);

            this.infos = infos;

            recyclerView = view.findViewById(R.id.info_list_recycler);
            layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new InfoListAdapter(infos);
            recyclerView.setAdapter(mAdapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());

            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        public void setInfos(ArrayList<InfoCell> infos) {
            this.infos.clear();
            this.infos.addAll(infos);
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "setInfos: " + infos.size());
        }

    }

    private static class MapViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        private static final String TAG = "MapViewHolder";

        private Context context;
        private MapView mapView;
        private GoogleMap map;
        private Coordinate coordinate;

        public MapViewHolder(Context context, View view, Coordinate coordinate) {
            super(view);

            this.context = context;
            mapView = view.findViewById(R.id.map_card_map);
            this.coordinate = coordinate;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            map = googleMap;

            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(false);

            setLocation();
        }

        public void initalizeMap() {
            if (mapView != null) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            }
        }

        private void setLocation() {
            if (map == null) return;

            LatLng location = new LatLng(coordinate.lat, coordinate.lon);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
            map.addMarker(new MarkerOptions().position(location));

            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

}
