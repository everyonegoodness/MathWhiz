package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.SessionRepository;
import com.mathwhiz.app.logic.DemoData;

import java.util.List;
import java.util.Random;

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
        findViewById(R.id.btnDemo).setOnClickListener(v -> confirmDemoData());
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

    private void confirmDemoData() {
        new AlertDialog.Builder(this)
                .setTitle("Load demo data?")
                .setMessage("This adds 9 sample sessions with FAKE results (Kinder and Grade 1) to your account "
                        + "so the dashboard has something to show. Use it for demos and testing only.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add demo data", (d, w) -> loadDemoData())
                .show();
    }

    private void loadDemoData() {
        String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            goToLogin();
            return;
        }
        final android.content.Context appCtx = getApplicationContext();
        DataCallback<Void> onRejected = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void value) { }

            @Override
            public void onError(String message) { UiUtils.toast(appCtx, message); }
        };
        List<DemoData.Pack> packs = DemoData.build(uid, new Random(), System.currentTimeMillis());
        for (DemoData.Pack p : packs) {
            SessionRepository.get().saveSession(p.session, p.responses, onRejected);
        }
        UiUtils.toast(this, "Demo data added. Open the class dashboard to see it.");
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
