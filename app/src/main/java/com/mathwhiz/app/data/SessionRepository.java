package com.mathwhiz.app.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Saves finished sessions (sessions/ + responses/) and loads them for the dashboard. */
public final class SessionRepository {
    private static final SessionRepository INSTANCE = new SessionRepository();

    public static SessionRepository get() { return INSTANCE; }

    /** Everything the dashboard needs for one teacher. */
    public static final class Data {
        public final List<SessionResult> sessions;
        public final List<ResponseRecord> responses;

        Data(List<SessionResult> sessions, List<ResponseRecord> responses) {
            this.sessions = sessions;
            this.responses = responses;
        }
    }

    private SessionRepository() {}

    private DatabaseReference root() { return FirebaseDatabase.getInstance().getReference(); }

    /**
     * Writes the session and all its responses in ONE multi-path update (all or nothing).
     * Works offline: Firebase queues it and syncs later. {@code onRejected} fires if the server refuses it.
     */
    public void saveSession(SessionResult session, List<ResponseRecord> responses, final DataCallback<Void> onRejected) {
        DatabaseReference root = root();
        String sessionId = root.child("sessions").push().getKey();
        Map<String, Object> updates = new HashMap<>();
        updates.put("sessions/" + sessionId, session);
        for (ResponseRecord r : responses) {
            r.sessionId = sessionId;
            String key = root.child("responses").push().getKey();
            updates.put("responses/" + key, r);
        }
        root.updateChildren(updates).addOnFailureListener(e ->
                onRejected.onError("Results could not be saved to the server: " + e.getMessage()));
    }

    public void loadAll(final String teacherUid, final DataCallback<Data> cb) {
        FirebaseReads.readOnce(root().child("sessions").orderByChild("teacherUid").equalTo(teacherUid),
                new DataCallback<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot sSnap) {
                        final List<SessionResult> sessions = new ArrayList<>();
                        for (DataSnapshot c : sSnap.getChildren()) {
                            SessionResult s = c.getValue(SessionResult.class);
                            if (s != null) sessions.add(s);
                        }
                        FirebaseReads.readOnce(root().child("responses").orderByChild("teacherUid").equalTo(teacherUid),
                                new DataCallback<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot rSnap) {
                                        List<ResponseRecord> responses = new ArrayList<>();
                                        for (DataSnapshot c : rSnap.getChildren()) {
                                            ResponseRecord r = c.getValue(ResponseRecord.class);
                                            if (r != null) responses.add(r);
                                        }
                                        cb.onSuccess(new Data(sessions, responses));
                                    }

                                    @Override
                                    public void onError(String message) { cb.onError(message); }
                                });
                    }

                    @Override
                    public void onError(String message) { cb.onError(message); }
                });
    }
}
