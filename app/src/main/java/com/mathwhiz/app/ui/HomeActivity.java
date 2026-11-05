package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.SessionRepository;
import com.mathwhiz.app.logic.StatsAggregator;
import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;

import java.util.List;

/** Teacher Home (the "Main" activity). */
public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private PieChart dashboardChart;
    private TextView dashboardSummary;
    private TextView dashboardError;
    private ProgressBar dashboardProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvWelcome = findViewById(R.id.tvWelcome);
        dashboardChart = findViewById(R.id.homeMostMissedChart);
        dashboardSummary = findViewById(R.id.tvDashboardPreviewSummary);
        dashboardError = findViewById(R.id.tvDashboardPreviewError);
        dashboardProgress = findViewById(R.id.dashboardPreviewProgress);

        findViewById(R.id.btnNewDrill).setOnClickListener(v ->
                startActivity(new Intent(this, CreateDrillActivity.class)));
        findViewById(R.id.btnMyDrills).setOnClickListener(v ->
                startActivity(new Intent(this, DrillListActivity.class)));
        findViewById(R.id.btnDashboard).setOnClickListener(v ->
                openDashboard());
        View viewMore = findViewById(R.id.btnDashboardMore);
        if (viewMore != null) viewMore.setOnClickListener(v -> openDashboard());
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
        loadDashboardPreview();
    }

    private void loadDashboardPreview() {
        if (dashboardChart == null) return;

        dashboardProgress.setVisibility(View.VISIBLE);
        dashboardError.setVisibility(View.GONE);
        dashboardSummary.setText(R.string.dashboard_preview_loading);
        String uid = AuthRepository.get().currentUid();
        if (uid == null) return;

        SessionRepository.get().loadAll(uid, new DataCallback<SessionRepository.Data>() {
            @Override
            public void onSuccess(SessionRepository.Data value) {
                if (isFinishing() || isDestroyed()) return;
                dashboardProgress.setVisibility(View.GONE);
                List<ResponseRecord> responses =
                        StatsAggregator.filterResponsesForSessions(value.responses, value.sessions);
                MostMissedChart.bind(dashboardChart, StatsAggregator.mostMissed(responses, 5));
                if (value.sessions.isEmpty()) {
                    dashboardSummary.setText(R.string.dashboard_preview_no_sessions);
                } else {
                    dashboardSummary.setText(
                            getString(R.string.dashboard_preview_sessions, value.sessions.size()));
                }
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                dashboardProgress.setVisibility(View.GONE);
                dashboardSummary.setText("");
                dashboardError.setText(message);
                dashboardError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
