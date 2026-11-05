package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.logic.Validators;

/** Teacher registration (email + password). There are no student accounts. */
public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private Button btnRegister;
    private ProgressBar progress;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        adjustResponsiveLayout();
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);

        btnRegister.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.btnGoLogin).setOnClickListener(v -> finish());
    }

    private void adjustResponsiveLayout() {
        View container = findViewById(R.id.registerFormContainer);
        container.post(() -> {
            int horizontalPadding = container.getPaddingLeft() + container.getPaddingRight();
            int cardWidth = Math.min(container.getWidth() - horizontalPadding, dp(520));
            View card = findViewById(R.id.registerCard);
            ViewGroup.LayoutParams params = card.getLayoutParams();
            params.width = cardWidth;
            if (params instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) params).gravity = Gravity.CENTER;
            }
            card.setLayoutParams(params);
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void attemptRegister() {
        final String name = String.valueOf(etName.getText());
        final String email = String.valueOf(etEmail.getText());
        final String password = String.valueOf(etPassword.getText());
        String error = Validators.name(name);
        if (error == null) error = Validators.email(email);
        if (error == null) error = Validators.password(password);
        if (error == null && !password.equals(String.valueOf(etConfirm.getText()))) {
            error = "The two passwords do not match.";
        }
        UiUtils.showError(tvError, error);
        if (error != null) return;

        setLoading(true);
        AuthRepository.get().register(name, email, password, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
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
        btnRegister.setEnabled(!loading);
    }
}
