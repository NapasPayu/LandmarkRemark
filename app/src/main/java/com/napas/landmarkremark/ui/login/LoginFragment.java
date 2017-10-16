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
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.ContentValues.TAG;

public class LoginFragment extends BaseFragment {

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        setUnBinder(ButterKnife.bind(this, view));
        return view;
    }

    @OnClick(R.id.btn_login)
    public void onBtnLoginClicked() {
        hideKeyboard();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        // input validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), R.string.please_enter_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), R.string.please_enter_password, Toast.LENGTH_SHORT).show();
            return;
        }

        loginCall(email, password);
    }

    @OnClick(R.id.btn_sign_up)
    public void onBtnSignUpClicked() {
        mListener.onSignUpClicked();
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnSignUp.setEnabled(!show);
    }

    private void loginCall(String email, String password) {
        showLoading(true);
        // sign in with firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            mListener.onLoginCompleted();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                        showLoading(false);
                    }
                });
    }
}
