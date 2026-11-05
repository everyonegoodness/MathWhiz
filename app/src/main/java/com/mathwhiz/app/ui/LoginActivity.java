package com.mathwhiz.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.logic.Validators;

import java.util.Locale;

/** Launcher screen: teacher login. Already-signed-in teachers go straight to the home screen. */
public class LoginActivity extends AppCompatActivity {
    private static final String LOGIN_PREFS = "login_greeting";
    private static final String LAST_EMAIL = "last_email";
    private static final String LAST_NAME = "last_name";

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private ProgressBar progress;
    private TextView tvError;
    private TextView tvForgotPassword;
    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AuthRepository.get().isLoggedIn()) {
            goHome();
            return;
        }
        setContentView(R.layout.activity_login);
        adjustResponsiveLayout();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
                updateWelcomeName(value.toString());
            }

            @Override
            public void afterTextChanged(Editable value) {}
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvForgotPassword.setOnClickListener(v -> sendPasswordResetEmail());
        findViewById(R.id.tvPasswordToggle).setOnClickListener(v -> togglePasswordVisibility((TextView) v));
        findViewById(R.id.btnGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void updateWelcomeName(String email) {
        SharedPreferences preferences = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        String savedEmail = preferences.getString(LAST_EMAIL, "");
        String savedName = preferences.getString(LAST_NAME, "");
        boolean matchesAccount = !savedName.isEmpty()
                && email.trim().equalsIgnoreCase(savedEmail);
        TextView welcome = findViewById(R.id.tvWelcome);
        welcome.setText(matchesAccount
                ? getString(R.string.login_welcome_named, savedName)
                : getString(R.string.login_welcome));
    }

    private void rememberWelcomeName(String email) {
        String name = AuthRepository.get().currentDisplayName();
        if (name == null || name.trim().isEmpty()) return;

        getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE).edit()
                .putString(LAST_EMAIL, email.trim().toLowerCase(Locale.ROOT))
                .putString(LAST_NAME, name.trim())
                .apply();
    }

    private void adjustResponsiveLayout() {
        View root = findViewById(R.id.loginRoot);
        root.post(() -> {
            View formContainer = findViewById(R.id.loginFormContainer);
            View brandImage = findViewById(R.id.loginBranding);
            View brandContainer = (View) brandImage.getParent();
            int cardSpacing = formContainer.getPaddingLeft() + formContainer.getPaddingRight();
            if (findViewById(R.id.loginBrandingContainer) == null) {
                cardSpacing += dp(32);
            }
            int cardWidth = Math.min(formContainer.getWidth() - cardSpacing, dp(520));
            int brandWidth = Math.min(brandContainer.getWidth(), dp(390));

            ViewGroup.LayoutParams cardParams = findViewById(R.id.loginCard).getLayoutParams();
            cardParams.width = cardWidth;
            if (cardParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) cardParams).gravity = Gravity.CENTER;
            } else if (cardParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) cardParams).gravity = Gravity.CENTER_HORIZONTAL;
            }
            findViewById(R.id.loginCard).setLayoutParams(cardParams);

            ViewGroup.LayoutParams brandParams = brandImage.getLayoutParams();
            brandParams.width = brandWidth;
            brandParams.height = brandWidth * 296 / 390;
            if (brandParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) brandParams).gravity = Gravity.CENTER;
            } else if (brandParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) brandParams).gravity = Gravity.CENTER_HORIZONTAL;
            }
            brandImage.setLayoutParams(brandParams);
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void togglePasswordVisibility(TextView toggle) {
        int selection = etPassword.getSelectionStart();
        passwordVisible = !passwordVisible;
        etPassword.setTransformationMethod(passwordVisible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        etPassword.setSelection(Math.max(0, Math.min(selection, etPassword.length())));
        toggle.setText(passwordVisible ? R.string.password_hide : R.string.password_show);
        toggle.setContentDescription(passwordVisible
                ? getString(R.string.password_hide_accessibility)
                : getString(R.string.password_show_accessibility));
    }

    private void sendPasswordResetEmail() {
        String email = String.valueOf(etEmail.getText());
        String error = Validators.email(email);
        UiUtils.showError(tvError, error);
        if (error != null) {
            etEmail.requestFocus();
            return;
        }

        setLoading(true);
        AuthRepository.get().sendPasswordResetEmail(email, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, R.string.password_reset_sent, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                UiUtils.showError(tvError, message);
            }
        });
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
                rememberWelcomeName(email);
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
        tvForgotPassword.setEnabled(!loading);
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
