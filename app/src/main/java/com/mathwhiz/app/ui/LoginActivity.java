package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.logic.Validators;

/** Launcher screen: teacher login. Already-signed-in teachers go straight to the home screen. */
public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private ProgressBar progress;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AuthRepository.get().isLoggedIn()) {
            goHome();
            return;
        }
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = String.valueOf(etEmail.getText());
        String password = String.valueOf(etPassword.getText());
        String error = Validators.email(email);
        if (error == null) error = Validators.password(password);
        UiUtils.showError(tvError, error);
        if (error != null) return;

        setLoading(true);
        AuthRepository.get().login(email, password, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                goHome();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                UiUtils.showError(tvError, message);
            }
        });
    }

    private void setLoading(boolean loading) {
        UiUtils.show(progress, loading);
        btnLogin.setEnabled(!loading);
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
