package com.mathwhiz.app.data;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.mathwhiz.app.ui.PresentationCoordinator;

public class MathWhizApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PresentationCoordinator.initialize(this);
        try {
            // Offline persistence: class mode keeps working if school Wi-Fi drops. Must run before any DB use.
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
            // Already enabled or Firebase not configured yet; the screens show their own errors.
        }
    }
}
