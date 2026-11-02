package com.mathwhiz.app.data;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.concurrent.atomic.AtomicBoolean;

/** One-shot query read with a timeout, so a dead connection shows an error instead of loading forever. */
final class FirebaseReads {
    private static final long TIMEOUT_MS = 12_000;

    private FirebaseReads() {}

    static void readOnce(Query query, final DataCallback<DataSnapshot> cb) {
        final AtomicBoolean done = new AtomicBoolean(false);
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable timeout = () -> {
            if (done.compareAndSet(false, true)) {
                cb.onError("Could not load your data. Check the internet connection and try again.");
            }
        };
        handler.postDelayed(timeout, TIMEOUT_MS);

        query.get().addOnSuccessListener(snapshot -> {
            if (done.compareAndSet(false, true)) {
                handler.removeCallbacks(timeout);
                cb.onSuccess(snapshot);
            }
        }).addOnFailureListener(error -> {
            if (done.compareAndSet(false, true)) {
                handler.removeCallbacks(timeout);
                cb.onError(error.getMessage() != null
                        ? "Could not load your data: " + error.getMessage()
                        : "Could not load your data. Check the internet connection and try again.");
            }
        });
    }
}
