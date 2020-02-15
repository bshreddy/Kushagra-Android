package com.project.crop_prediction.ui.recents;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project.crop_prediction.DetailActivity;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Coordinate;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;
import com.project.crop_prediction.model.RecentDeserializer;

import java.util.ArrayList;
import java.util.Date;

public class RecentsFragment extends Fragment implements FirebaseAuth.AuthStateListener, View.OnClickListener {

    private static final String TAG = "RecentsFragment";
    private static final String KIND_PARAM = "kind";
    private static final int RC_CAPTURE = 1;
    private static final int RC_DETAIL = 2;
    private static final int RC_PERMISSIONS = 100;

    private RecyclerView recyclerView;
    private RecentsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton fab;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private CollectionReference recentsRef;
    private FusedLocationProviderClient fusedLocationClient;

    private ArrayList<Recent> recents;
    private Prediction.Kind kind;

    public static RecentsFragment newInstance(String kind) {
        RecentsFragment fragment = new RecentsFragment();
        Bundle args = new Bundle();
        args.putString(KIND_PARAM, kind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString(KIND_PARAM).equalsIgnoreCase(Prediction.Kind.crop.rawValue) ? Prediction.Kind.crop : Prediction.Kind.disease;
            Log.d(TAG, "onCreate: " + kind);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recents, container, false);

        recents = new ArrayList<>();

        recyclerView = root.findViewById(R.id.recents_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecentsAdapter(recents);
        recyclerView.setAdapter(mAdapter);

        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroy() {
        firebaseAuth.removeAuthStateListener(this);

        super.onDestroy();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        user = firebaseAuth.getCurrentUser();

        if(user != null) {
            recentsRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("recents");
        } else {
            recentsRef = null;
        }

        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CAPTURE && resultCode == getActivity().RESULT_OK) {
            getPrediction((Bitmap) data.getExtras().get("data"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSIONS) {
            boolean granted = true;
            for(int res: grantResults)
                granted = granted && (res == PackageManager.PERMISSION_GRANTED);

            if (granted)
                startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), RC_CAPTURE);
            else
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private boolean arePermissionsGranted() {
        return (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }


    @Override
    public void onClick(View view) {
        if (arePermissionsGranted()) {
            startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), RC_CAPTURE);
        }
        else
            requestPermissions(new String[]{ android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSIONS);
    }

    private void loadData() {
        if(recentsRef == null) {
            recents.clear();
            mAdapter.reloadData();
            return;
        }

        String kindFieldPath[] = {"pred", "kind"};
        recentsRef.whereEqualTo(FieldPath.of(kindFieldPath), kind.rawValue)
                .orderBy("crtdAt", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            recents.clear();
                            Gson gson = new GsonBuilder().registerTypeAdapter(Recent.class, new RecentDeserializer()).create();

                            for(DocumentSnapshot doc: task.getResult().getDocuments()) {
                                recents.add(gson.fromJson(gson.toJsonTree(doc.getData()), Recent.class));
                            }

                            mAdapter.reloadData();
                        } else {
                            Snackbar.make(getActivity().findViewById(android.R.id.content),
                                    "Umm! Unable to read data", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }



    private void getPrediction(Bitmap img) {
        Prediction.predict(getContext(), kind, img, new Prediction.PredictionListener() {

            @Override
            public void onCropPrediction(final Prediction prediction) {
                if(prediction == null) {
                    Log.d(TAG, "onComplete: Error");
                    return;
                }

                fusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Coordinate coordinate = null;

                                if(task.isSuccessful())
                                    coordinate = new Coordinate(task.getResult());

                                Recent recent = new Recent(prediction, false, new Date(), coordinate);

                                Intent intent = new Intent(getContext(), DetailActivity.class);
                                intent.putExtra(DetailActivity.KIND_PARAM, kind);
                                intent.putExtra(DetailActivity.RECENT_PARAM, recent);
                                startActivityForResult(intent, RC_DETAIL);
                                Log.d(TAG, "onComplete: " + recent);
                            }
                        });
            }
        });
    }

}