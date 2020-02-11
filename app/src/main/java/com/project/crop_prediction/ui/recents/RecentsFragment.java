package com.project.crop_prediction.ui.recents;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Location;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;

import java.util.ArrayList;
import java.util.Date;

public class RecentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecentsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Recent> recents;

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
}