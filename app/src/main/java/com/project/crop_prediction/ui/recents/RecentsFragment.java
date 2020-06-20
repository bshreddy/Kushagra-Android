package com.project.crop_prediction.ui.recents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.project.crop_prediction.detail.DetailActivity;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Coordinate;
import com.project.crop_prediction.model.CoordinateSerializer;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.PredictionSerializer;
import com.project.crop_prediction.model.Recent;
import com.project.crop_prediction.model.RecentDeserializer;
import com.project.crop_prediction.model.RecentSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecentsFragment extends Fragment implements FirebaseAuth.AuthStateListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, RecentsAdapter.OnClickListener {

    private static final String TAG = "RecentsFragment";
    private static final String KIND_PARAM = "kind";
    private static final String ONLYBKMK_PARAM = "bookmark";
    private static final int RC_CAPTURE = 1;
    private static final int RC_DETAIL = 2;
    private static final int RC_PERMISSIONS = 100;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecentsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton fab;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private CollectionReference recentsRef;
    private StorageReference recentImagesRef;

    private ArrayList<Recent> recents;
    private Prediction.Kind kind;
    private boolean onlyBookmark, openCam;
    private FusedLocationProviderClient fusedLocationClient;
    public  File picsDir;

    public static RecentsFragment newInstance(String kind, boolean onlyBookmark) {
        RecentsFragment fragment = new RecentsFragment();
        Bundle args = new Bundle();
        args.putString(KIND_PARAM, kind);
        args.putBoolean(ONLYBKMK_PARAM, onlyBookmark);
        fragment.setArguments(args);
        return fragment;
    }

    public static RecentsFragment newInstance(String kind) {
        return newInstance(kind, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + savedInstanceState);

        if (getArguments() != null) {
            kind = getArguments().getString(KIND_PARAM).equalsIgnoreCase(Prediction.Kind.crop.rawValue) ? Prediction.Kind.crop : Prediction.Kind.disease;
            onlyBookmark = getArguments().getBoolean(ONLYBKMK_PARAM);
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.addAuthStateListener(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if(!arePermissionsGranted()) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSIONS);
        }

        picsDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CropPrediction");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recents, container, false);

        recents = new ArrayList<>();

        swipeRefreshLayout = root.findViewById(R.id.recents_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = root.findViewById(R.id.recents_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecentsAdapter(getContext(), recents, picsDir, this);
        recyclerView.setAdapter(mAdapter);

        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroy() {
        firebaseAuth.removeAuthStateListener(this);
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        user = firebaseAuth.getCurrentUser();

        if (user != null) {
            recentsRef = FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).collection("recents");

            recentImagesRef = FirebaseStorage.getInstance().getReference().child("/images");

        } else {
            recentsRef = null;
            recentImagesRef = null;
        }

        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            getPrediction((Bitmap) data.getExtras().get("data"));
        } else if (requestCode == RC_DETAIL && resultCode == getActivity().RESULT_OK) {
            saveRecent((Recent) data.getExtras().getParcelable(DetailActivity.RECENT_PARAM));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSIONS) {
            boolean granted = true;
            for (int res : grantResults)
                granted = granted && (res == PackageManager.PERMISSION_GRANTED);

            if (granted) {
                if(openCam) {
                    openCam = false;
                    startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), RC_CAPTURE);
                }
            } else
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void onClick(View view) {
        if (arePermissionsGranted()) {
            Log.d(TAG, "onClick: Permission granted");
            startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), RC_CAPTURE);
        } else
            openCam = true;
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSIONS);
    }

    @Override
    public void onClick(int position) {
        Recent recent = recents.get(position);

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.KIND_PARAM, kind);
        intent.putExtra(DetailActivity.RECENT_PARAM, recent);

        startActivity(intent);
    }
    @Override
    public void onBookmarkClick(final int position) {
        final Recent recent = recents.get(position);

        if(recent.id == null) {
            Toast.makeText(getContext(),"Unknown Error",Toast.LENGTH_SHORT).show();
            return;
        }

        recent.bookmarked = !recent.bookmarked;
        ((RecentsAdapter.RecentsViewHolder)recyclerView.findViewHolderForAdapterPosition(position)).bookmark.setImageResource(
                (recent.bookmarked ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_outline_24dp));

        updateBookmark(recent, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        recent.bookmarked = !recent.bookmarked;
                        ((RecentsAdapter.RecentsViewHolder)recyclerView.findViewHolderForAdapterPosition(position)).bookmark.setImageResource(
                                (recent.bookmarked ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_outline_24dp));
                    }
                });

    }

    public void updateBookmark(Recent recent, OnFailureListener onFailureListener) {
        recentsRef.document(recent.id).update("bkmrkd", recent.bookmarked)
                .addOnFailureListener(onFailureListener);
    }

    private boolean arePermissionsGranted() {
        return (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void loadData() {
        if (recentsRef == null) {
            recents.clear();
            mAdapter.reloadData();
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        String kindFieldPath[] = {"pred", "kind"};
        Query recentQuery = null;
        if(onlyBookmark) {
            // TODO: Write query for bookmarked items
        } else {
            recentQuery = recentsRef.whereEqualTo(FieldPath.of(kindFieldPath), kind.rawValue);
        }

        recentQuery
                .orderBy("crtdAt", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            recents.clear();
                            Gson gson = new GsonBuilder()
                                    .registerTypeAdapter(Recent.class, new RecentDeserializer())
                                    .create();

                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                Recent recent = gson.fromJson(gson.toJsonTree(doc.getData()), Recent.class);
                                recent.id = doc.getId();
                                recents.add(recent);
                            }

                            mAdapter.reloadData();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            Snackbar.make(getActivity().findViewById(android.R.id.content),
                                    "Umm! Unable to read data", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveRecent(final Recent recent) {
        try {
            if (user == null && recentsRef == null)
                return;

            DocumentReference doc = recentsRef.document();
            if (doc == null)
                return;

            File dir = new File(picsDir, recent.prediction.getPredictedClass());
            if (!dir.exists())
                dir.mkdirs();

            String id = doc.getId();
            String imgName = recent.prediction.getPredictedClass() + "/" + user.getUid() + '-' + id + ".png";
            File imageFile = new File(picsDir, imgName);
            imageFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            recent.prediction.image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

            recentImagesRef.child(imgName).putFile(Uri.fromFile(imageFile));

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Recent.class, new RecentSerializer())
                    .registerTypeAdapter(Prediction.class, new PredictionSerializer())
                    .registerTypeAdapter(Coordinate.class, new CoordinateSerializer())
                    .create();
            gson.toJsonTree(recent);
            Map<String, Object> jsonMap = new Gson().fromJson(gson.toJson(recent), new TypeToken<HashMap<String, Object>>() {
            }.getType());
            Log.d(TAG, "saveRecent: " + jsonMap);

            doc.set(jsonMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Saved > " + recent);
                        loadData();
                    } else {
                        Log.d(TAG, "onComplete: Error > " + task.getException().getLocalizedMessage());
                    }
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getPrediction(Bitmap img) {
        Prediction.predict(getContext(), kind, img, new Prediction.PredictionListener() {

            @Override
            public void onCropPrediction(final Prediction prediction) {
                if (prediction == null) {
                    Log.d(TAG, "onComplete: Error");
                    return;
                }

                fusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Coordinate coordinate = null;

                                if (task.isSuccessful())
                                    coordinate = new Coordinate(task.getResult());

                                Recent recent = new Recent(prediction, false, new Date(), coordinate);

                                Intent intent = new Intent(getContext(), DetailActivity.class);
                                intent.putExtra(DetailActivity.KIND_PARAM, kind);
                                intent.putExtra(DetailActivity.ISNEW_PARAM, true);
                                intent.putExtra(DetailActivity.RECENT_PARAM, recent);
                                startActivityForResult(intent, RC_DETAIL);
                                Log.d(TAG, "onComplete: " + recent);
                            }
                        });
            }
        });
    }

}