package com.project.crop_prediction.ui.recents;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
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
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Location;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;
import com.project.crop_prediction.model.RecentDeserializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecentsFragment extends Fragment implements FirebaseAuth.AuthStateListener {

    private static final String TAG = "RecentsFragment";
    private static final String KIND_PARAM = "kind";

    private RecyclerView recyclerView;
    private RecentsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    CollectionReference recentsRef;

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
}