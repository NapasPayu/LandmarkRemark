package com.napas.landmarkremark.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.ui.base.BaseActivity;
import com.napas.landmarkremark.ui.landmark.LandmarkActivity;

import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements ILoginListener {

    private FirebaseAuth mAuth;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        setUnBinder(ButterKnife.bind(this));
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Check if user is not signed in (null)
        if (currentUser == null) {
            // go to login page
            replaceFragment(R.id.fl_container, new LoginFragment());
        } else {
            // go to main page
            onLoginCompleted();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onLoginCompleted() {
        startActivity(LandmarkActivity.getStartIntent(this));
    }

    @Override
    public void onSignUpClicked() {
        addFragment(R.id.fl_container, new SignUpFragment());
    }

}
