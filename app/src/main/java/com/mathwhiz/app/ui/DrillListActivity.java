package com.mathwhiz.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mathwhiz.app.R;
import com.mathwhiz.app.data.AuthRepository;
import com.mathwhiz.app.data.DataCallback;
import com.mathwhiz.app.data.DrillRepository;
import com.mathwhiz.app.model.Drill;

import java.util.List;

/** The teacher's saved drills. Tap one to run it in class mode again. */
public class DrillListActivity extends AppCompatActivity {
    private LinearLayout list;
    private ProgressBar progress;
    private TextView tvEmpty, tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_list);
        list = findViewById(R.id.list);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvError = findViewById(R.id.tvError);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = AuthRepository.get().currentUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        UiUtils.show(progress, true);
        UiUtils.show(tvEmpty, false);
        UiUtils.showError(tvError, null);
        DrillRepository.get().loadDrills(uid, new DataCallback<List<DrillRepository.Entry>>() {
            @Override
            public void onSuccess(List<DrillRepository.Entry> entries) {
                UiUtils.show(progress, false);
                render(entries);
            }

            @Override
            public void onError(String message) {
                UiUtils.show(progress, false);
                UiUtils.showError(tvError, message);
            }
        });
    }

    private void render(List<DrillRepository.Entry> entries) {
        list.removeAllViews();
        UiUtils.show(tvEmpty, entries.isEmpty());
        for (final DrillRepository.Entry e : entries) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_row, list, false);
            Drill d = e.drill;
            ((TextView) row.findViewById(R.id.tvRowTitle)).setText(d.title);
            String detail = d.section + "  \u2022  " + d.questionCount + " questions  \u2022  "
                    + (d.formatIsMultipleChoice() ? "Multiple choice" : "Flashcard")
                    + (d.modeIsTimed() ? "  \u2022  Timed " + d.timeLimitSec + "s" : "  \u2022  Untimed");
            ((TextView) row.findViewById(R.id.tvRowSub)).setText(detail);
            row.setOnClickListener(v -> {
                Intent i = new Intent(this, ClassModeActivity.class);
                i.putExtra(IntentKeys.DRILL, e.drill);
                i.putExtra(IntentKeys.DRILL_ID, e.id);
                startActivity(i);
            });
            list.addView(row);
        }
    }
}
