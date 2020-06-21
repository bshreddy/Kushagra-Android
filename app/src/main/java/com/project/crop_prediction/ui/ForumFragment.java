package com.project.crop_prediction.ui;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.crop_prediction.R;

public class ForumFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ForumFragment";

    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);

        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: FloatingActionButton Clicked");
    }
}
