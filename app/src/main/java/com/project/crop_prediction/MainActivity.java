package com.project.crop_prediction;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.project.crop_prediction.model.Prediction;

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

        setupUI();
        setupFirebase();
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(this);
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

        navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        if (!BuildConfig.DEBUG)
            navigationView.getMenu().removeItem(R.id.menu_server);

        View navigationHeaderView = navigationView.getHeaderView(0);
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
                switch (destination.getId()) {
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

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showUserLogin() {
        Log.d(TAG, "showUserLogin: " + user);
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
            if (user.getDisplayName().isEmpty()) {
                navHeaderUname.setText(user.getEmail());
                navHeaderEmail.setVisibility(View.GONE);
                Glide.with(this).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(navHeaderDP);
            } else {
                navHeaderUname.setText(user.getDisplayName());
                navHeaderEmail.setVisibility(View.VISIBLE);
                navHeaderEmail.setText(user.getEmail());
                Glide.with(this).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(navHeaderDP);
            }

            navigationView.getMenu().findItem(R.id.menu_signout).setTitle("Sign Out");
        } else {
            navHeaderUname.setText(R.string.default_uname);
            navHeaderEmail.setText(R.string.default_email);
            navHeaderDP.setImageResource(R.drawable.ic_profile_24px);

            navigationView.getMenu().findItem(R.id.menu_signout).setTitle("Sign In");
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

            if (snackbar != null)
                snackbar.show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START, true);

        switch (item.getItemId()) {
            case R.id.menu_bookmarks:
                Toast.makeText(this, "menu_bookmarks", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_server:
                final EditText serverAddrInp = new EditText(this);
                serverAddrInp.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                serverAddrInp.setText(Prediction.getServerURL(this));
                serverAddrInp.setPadding(16, serverAddrInp.getPaddingTop(), 16, serverAddrInp.getPaddingBottom());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Server Address")
                        .setMessage("Enter address of AI Inference Server")
                        .setView(serverAddrInp, 64, 8, 64, 8)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String serverAddress = serverAddrInp.getText().toString();
                                if (!serverAddress.isEmpty()) {
                                    Prediction.setServerURL(getApplicationContext(), serverAddress);
                                    Toast.makeText(getApplicationContext(), "Server Address Saved", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Enter a Valid Server Address", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
                break;

            case R.id.menu_settings:
                Toast.makeText(this, "menu_settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_help:
                startActivity(new Intent(getApplicationContext(), WebActivity.class));
                break;

            case R.id.menu_invite:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey,\n\nCrop Prediction App is an AI-powered, " +
                        "intuitive app that I use to identify my crops, crop diseases and get solutions.\n\n" +
                        "Get it for free at Play Store");
                sendIntent.setType("text/*");
                Intent shareIntent = Intent.createChooser(sendIntent, "Invite Friends Via");
                startActivity(shareIntent);
                break;
            case R.id.menu_signout:
                if(user == null)
                    showUserLogin();
                else
                    FirebaseAuth.getInstance().signOut();
        }

        return true;
    }
}

