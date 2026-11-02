package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;

/** Teacher Home (the "Main" activity). */
public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvWelcome = findViewById(R.id.tvWelcome);

        findViewById(R.id.btnNewDrill).setOnClickListener(v ->
                startActivity(new Intent(this, CreateDrillActivity.class)));
        findViewById(R.id.btnMyDrills).setOnClickListener(v ->
                startActivity(new Intent(this, DrillListActivity.class)));
        findViewById(R.id.btnDashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            AuthRepository.get().logout();
            goToLogin();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!AuthRepository.get().isLoggedIn()) {
            goToLogin();
            return;
        }
        tvWelcome.setText("Hello, " + AuthRepository.get().currentName() + "!");
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
