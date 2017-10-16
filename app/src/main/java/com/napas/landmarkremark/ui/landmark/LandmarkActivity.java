package com.napas.landmarkremark.ui.landmark;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.model.Landmark;
import com.napas.landmarkremark.ui.base.BaseActivity;
import com.napas.landmarkremark.ui.login.LoginActivity;
import com.napas.landmarkremark.util.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LandmarkActivity extends BaseActivity implements ILandmarkListener {

    @BindView(R.id.tv_error)
    TextView tvError;
    @BindView(R.id.btn_get_permission)
    Button btnGetPermission;
    @BindView(R.id.fl_container)
    FrameLayout flContainer;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Fragment mRetainedFragment;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, LandmarkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);
        setUnBinder(ButterKnife.bind(this));
        checkLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(LoginActivity.getStartIntent(this));
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            showLandmarkMapFragment();
            if (CommonUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showError(false);
            } else {
                // show error when permission is not granted
                showError(true);
            }
        }
    }

    @Override
    public void onAddClicked(Landmark currentPlace) {
        showAddLandmarkFragment(currentPlace);
    }

    @Override
    public void onLandmarkAdded() {
        Toast.makeText(this, R.string.landmark_added, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    @OnClick(R.id.btn_get_permission)
    public void onBtnGetPermissionClicked() {
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {   // permission granted
            // find the retained fragment on activity restarts
            FragmentManager fm = getSupportFragmentManager();
            mRetainedFragment = fm.findFragmentById(R.id.fl_container);

            // create a fragment for the first time
            if (mRetainedFragment == null) {
                showLandmarkMapFragment();
            }
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showLandmarkMapFragment() {
        mRetainedFragment = LandmarkMapFragment.newInstance();
        replaceFragment(R.id.fl_container, mRetainedFragment);
    }

    private void showAddLandmarkFragment(Landmark currentPlace) {
        mRetainedFragment = AddLandmarkFragment.newInstance(currentPlace);
        addFragment(R.id.fl_container, mRetainedFragment);
    }

    private void showError(boolean show) {
        tvError.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGetPermission.setVisibility(show ? View.VISIBLE : View.GONE);
        flContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
