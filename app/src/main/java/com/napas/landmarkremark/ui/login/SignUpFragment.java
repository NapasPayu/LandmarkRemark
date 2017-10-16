package com.napas.landmarkremark.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.ContentValues.TAG;

public class SignUpFragment extends BaseFragment {

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.btn_sign_up)
    Button btnSignUp;
    @BindView(R.id.progress)
    ProgressBar progress;
    private ILoginListener mListener;
    private FirebaseAuth mAuth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ILoginListener) {
            mListener = (ILoginListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement ILoginListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        setUnBinder(ButterKnife.bind(this, view));
        return view;
    }

    @OnClick(R.id.btn_sign_up)
    public void onBtnSignUpClicked() {
        hideKeyboard();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        // input validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), R.string.please_enter_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), R.string.please_enter_password, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), R.string.please_enter_name, Toast.LENGTH_SHORT).show();
            return;
        }

        createUserCall(email, password, name);
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
    }

    private void createUserCall(final String email, final String password, final String name) {
        showLoading(true);
        // create an an account by firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            updateUserCall(name);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    }
                });
    }

    private void updateUserCall(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showLoading(false);
            return;
        }

        // set user name after registering
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            mListener.onLoginCompleted();
                        }
                        showLoading(false);
                    }
                });
    }
}
