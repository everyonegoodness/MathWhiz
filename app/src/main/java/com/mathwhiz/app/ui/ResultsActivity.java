package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.FruitRepository;
import com.mathwhiz.app.logic.FruitCatalog;
import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.FactResult;

import java.util.ArrayList;
import java.util.Random;

/** Class score for the finished drill plus a Fruityvice fun fact (offline fallback if the API fails). */
public class ResultsActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        final Drill drill = (Drill) getIntent().getSerializableExtra(IntentKeys.DRILL);
        final String drillId = getIntent().getStringExtra(IntentKeys.DRILL_ID);
        int total = getIntent().getIntExtra(IntentKeys.TOTAL, 0);
        int correct = getIntent().getIntExtra(IntentKeys.CORRECT, 0);
        int best = getIntent().getIntExtra(IntentKeys.BEST_STREAK, 0);
        ArrayList<String> missed = getIntent().getStringArrayListExtra(IntentKeys.MISSED_FRUITS);
        ArrayList<String> used = getIntent().getStringArrayListExtra(IntentKeys.USED_FRUITS);

        int percent = total == 0 ? 0 : (int) Math.round(100.0 * correct / total);
        ((TextView) findViewById(R.id.tvResultTitle)).setText(drill == null ? "Results" : drill.title);
        ((TextView) findViewById(R.id.tvResultSection)).setText(drill == null ? "" : drill.section);
        ((TextView) findViewById(R.id.tvScore)).setText(correct + " / " + total);
        ((TextView) findViewById(R.id.tvPercent)).setText(percent + "% correct  \u2022  Best streak: " + best);
        ((TextView) findViewById(R.id.tvMessage)).setText(percent >= 80
                ? "Great job, class!" : percent >= 50 ? "Good effort! Let's keep practicing." : "Let's practice some more together!");

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
        findViewById(R.id.btnDashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        TextView btnAgain = findViewById(R.id.btnAgain);
        if (drill == null) {
            btnAgain.setVisibility(android.view.View.GONE);
        } else {
            btnAgain.setOnClickListener(v -> {
                Intent i = new Intent(this, ClassModeActivity.class);
                i.putExtra(IntentKeys.DRILL, drill);
                i.putExtra(IntentKeys.DRILL_ID, drillId);
                startActivity(i);
                finish();
            });
        }
        loadFact(missed, used);
    }

    private void loadFact(ArrayList<String> missed, ArrayList<String> used) {
        final ProgressBar progress = findViewById(R.id.factProgress);
        final TextView tvEmoji = findViewById(R.id.tvFactEmoji);
        final TextView tvFact = findViewById(R.id.tvFact);
        final TextView tvSource = findViewById(R.id.tvFactSource);

        Random rnd = new Random();
        String fruit = "";
        if (missed != null && !missed.isEmpty()) fruit = missed.get(rnd.nextInt(missed.size()));
        else if (used != null && !used.isEmpty()) fruit = used.get(rnd.nextInt(used.size()));

        progress.setVisibility(android.view.View.VISIBLE);
        FruitRepository.get().loadFact(fruit, new DataCallback<FactResult>() {
            @Override
            public void onSuccess(FactResult fact) {
                if (isFinishing() || isDestroyed()) return;
                progress.setVisibility(android.view.View.GONE);
                tvEmoji.setText(fact.emoji);
                tvFact.setText(fact.text);
                tvSource.setText(fact.fromApi ? "Data from Fruityvice" : "Offline fruit fact (Fruityvice not available)");
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(android.view.View.GONE);
                FactResult f = FruitCatalog.randomFallbackFact(new Random());
                tvEmoji.setText(f.emoji);
                tvFact.setText(f.text);
                tvSource.setText("Offline fruit fact");
            }
        });
    }
}
