package com.project.crop_prediction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;

public class DetailActivity extends AppCompatActivity {

    public static final String KIND_PARAM = "kind";
    public static final String RECENT_PARAM = "recent";
    private static final String TAG = "DetailActivity";
    private MaterialToolbar toolbar;

    private Prediction.Kind kind;
    private Recent recent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initVariables();
        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    private void initVariables() {
        Intent intent = getIntent();
        kind = (Prediction.Kind) intent.getSerializableExtra(KIND_PARAM);
        recent = intent.getParcelableExtra(RECENT_PARAM);
    }

    private void setupUI() {
        toolbar = findViewById(R.id.detail_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(kind.capitalized() + " Details");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_prediction:
                Bundle bundle = new Bundle();
                bundle.putParcelable(RECENT_PARAM, recent);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
