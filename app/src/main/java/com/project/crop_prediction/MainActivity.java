package com.project.crop_prediction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1008;

    private MaterialToolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;
    private NavController navController;
    private ImageView navHeaderDP;
    private TextView navHeaderUname;
    private TextView navHeaderEmail;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setNavigationItemListener();
        setupUI();
        setupFirebase();
    }

    private void setNavigationItemListener() {
        navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStop() {
        firebaseAuth.removeAuthStateListener(this);

        super.onStop();
    }

    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        fab = findViewById(R.id.fab);
        bottomNav = findViewById(R.id.bottom_navigation);

        View navigationHeaderView = ((NavigationView) findViewById(R.id.nav_drawer)).getHeaderView(0);
        navHeaderDP = navigationHeaderView.findViewById(R.id.nav_header_dp);
        navHeaderUname = navigationHeaderView.findViewById(R.id.nav_header_uname);
        navHeaderEmail = navigationHeaderView.findViewById(R.id.nav_header_email);

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_crop, R.id.navigation_disease, R.id.navigation_forum, R.id.navigation_prices)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                switch(destination.getId()) {
                    case R.id.navigation_crop:
                    case R.id.navigation_disease:
                        fab.setImageResource(R.drawable.ic_camera_24dp);
                        break;
                    case R.id.navigation_forum:
                        fab.setImageResource(R.drawable.ic_create_24dp);
                        break;
                    case R.id.navigation_prices:
                        fab.setImageResource(R.drawable.ic_add_24dp);
                        break;
                }
            }
        });
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showUserLogin() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        user = firebaseAuth.getCurrentUser();

        if (user != null) {
            navHeaderUname.setText(user.getDisplayName());
            navHeaderEmail.setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(navHeaderDP);
        } else {
            navHeaderUname.setText(R.string.default_uname);
            navHeaderEmail.setText(R.string.default_email);
            navHeaderDP.setImageResource(R.drawable.ic_profile_24px);

            showUserLogin();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Snackbar snackbar = null;

            if (resultCode == RESULT_OK) {
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Sign In Successful", Snackbar.LENGTH_LONG);
            } else {
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Unable to Sign In", Snackbar.LENGTH_LONG);
                snackbar.setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUserLogin();
                    }
                });
            }

            if(snackbar != null)
                snackbar.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_invite:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey,\n" +
                        "\n" +
                        "Crop Prediction App is an AI-powered, intuitive app that I use to identify my crops, crop diseases and get solutions.\n" +
                        "\n" +
                        "Get it for free at Play Store");
                sendIntent.setType("text/*");

                Intent shareIntent = Intent.createChooser(sendIntent, "Invite Friends Via");
                startActivity(shareIntent);

        }
        return true;
    }
}

