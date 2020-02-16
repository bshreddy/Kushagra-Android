package com.project.crop_prediction;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.project.crop_prediction.model.Coordinate;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;
import com.project.crop_prediction.ui.recents.RecentsFragment;

import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    public static final String KIND_PARAM = "kind";
    public static final String RECENT_PARAM = "recent";
    public static final String PREDICTION_PARAM = "prediction";
    public static final String ISNEW_PARAM = "recent";
    private static final String TAG = "DetailActivity";
    private MaterialToolbar toolbar;

    private FusedLocationProviderClient fusedLocationClient;

    private Prediction.Kind kind;
    private Recent recent;
    private boolean isNew;

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

        if(!isNew)
            menu.removeItem(R.id.menu_save_prediction);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(isNew) {
                    confirmCancel();
                    return true;
                }
                break;

            case R.id.menu_save_prediction:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isNew) {
            confirmCancel();
            return;
        }

        super.onBackPressed();
    }

    private void initVariables() {
        Intent intent = getIntent();
        kind = (Prediction.Kind) intent.getSerializableExtra(KIND_PARAM);
        isNew = intent.getBooleanExtra(ISNEW_PARAM, false);

        if(isNew) {
            Prediction prediction = intent.getParcelableExtra(PREDICTION_PARAM);
            recent = new Recent(prediction, new Date());

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            recent.coordinate = new Coordinate(location);
                        }
                    });
        } else {
            recent = intent.getParcelableExtra(RECENT_PARAM);
        }
    }

    private void setupUI() {
        toolbar = findViewById(R.id.detail_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(isNew) {
            getSupportActionBar().setTitle("New " + kind.capitalized() + " Detection");
            toolbar.setNavigationIcon(R.drawable.ic_close_24dp);
        } else
            getSupportActionBar().setTitle(kind.capitalized() + " Details");
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure?\nDo you want to save?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DetailActivity.this.save();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DetailActivity.this.close();
                    }
                })
                .create()
                .show();
    }

    private void save() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RECENT_PARAM, recent);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void close() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
