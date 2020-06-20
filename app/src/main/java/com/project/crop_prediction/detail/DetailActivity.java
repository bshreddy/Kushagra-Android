package com.project.crop_prediction.detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.crop_prediction.R;
import com.project.crop_prediction.model.Prediction;
import com.project.crop_prediction.model.Recent;

import java.io.File;

public class DetailActivity extends AppCompatActivity {

    public static final String KIND_PARAM = "kind";
    public static final String RECENT_PARAM = "recent";
    public static final String CROP_DETAILS_PARAM = "crop_details";
    public static final String ISNEW_PARAM = "isnew";
    private static final String TAG = "DetailActivity";

    private RecyclerView recyclerView;
    private DetailAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private MaterialToolbar toolbar;

    private FirebaseUser user;
    private StorageReference recentImagesRef;
    private  File picsDir;

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

    private void initVariables() {
        Intent intent = getIntent();
        kind = (Prediction.Kind) intent.getSerializableExtra(KIND_PARAM);
        isNew = intent.getBooleanExtra(ISNEW_PARAM, false);
        recent = intent.getParcelableExtra(RECENT_PARAM);

        user = FirebaseAuth.getInstance().getCurrentUser();
        recentImagesRef = FirebaseStorage.getInstance().getReference().child("/images");
        picsDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CropPrediction");
    }

    private void setupUI() {
        toolbar = findViewById(R.id.detail_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (isNew) {
            getSupportActionBar().setTitle("New " + kind.capitalized() + " Detection");
            toolbar.setNavigationIcon(R.drawable.ic_close_24dp);
        } else
            getSupportActionBar().setTitle(kind.capitalized() + " Details");

        recyclerView = findViewById(R.id.detail_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new DetailAdapter(this, recent, user, recentImagesRef, picsDir);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        if (isNew)
            menu.removeItem(R.id.menu_delete);
        else
            menu.removeItem(R.id.menu_save_prediction);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_bookmark);
        item.setTitle((recent.bookmarked) ? "Remove from Bookmarks" : "Add to Bookmarks");
        item.setIcon((recent.bookmarked) ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_outline_24dp);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isNew) {
                    confirmCancel();
                    return true;
                }
                break;

            case R.id.menu_save_prediction:
                save();
                return true;

            case R.id.menu_bookmark:
                recent.bookmarked = !recent.bookmarked;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                
                if(user == null) {
                    Toast.makeText(getApplicationContext(),"Unknown Error Occurred",Toast.LENGTH_SHORT).show();
                } else {
                    CollectionReference recentsRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("recents");
                    recentsRef.document(recent.id).update("bkmrkd", recent.bookmarked)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    recent.bookmarked = !recent.bookmarked;
                                }
                            });
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isNew) {
            confirmCancel();
            return;
        }

        super.onBackPressed();
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
                .setNeutralButton("Cancel", null)
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
