package com.mathwhiz.app.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mathwhiz.app.model.Drill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Reads and writes drills/{drillId}. Queries are always limited to the signed-in teacher's own drills. */
public final class DrillRepository {
    private static final DrillRepository INSTANCE = new DrillRepository();

    public static DrillRepository get() { return INSTANCE; }

    /** A drill plus its database key (the key is not stored inside the drill itself). */
    public static final class Entry {
        public final String id;
        public final Drill drill;

        Entry(String id, Drill drill) {
            this.id = id;
            this.drill = drill;
        }
    }

    private DrillRepository() {}

    private DatabaseReference root() { return FirebaseDatabase.getInstance().getReference(); }

    /**
     * Saves a drill. The callback fires as soon as the write is queued locally (offline persistence makes
     * this safe on weak Wi-Fi); if the server later rejects it, {@code onRejected} is called.
     */
    public void saveDrill(Drill drill, DataCallback<Entry> cb, final DataCallback<Void> onRejected) {
        DatabaseReference ref = root().child("drills").push();
        final String key = ref.getKey();
        ref.setValue(drill).addOnFailureListener(e ->
                onRejected.onError("The drill could not be saved to the server: " + e.getMessage()));
        cb.onSuccess(new Entry(key, drill));
    }

    public void loadDrills(String teacherUid, final DataCallback<List<Entry>> cb) {
        FirebaseReads.readOnce(root().child("drills").orderByChild("teacherUid").equalTo(teacherUid),
                new DataCallback<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snap) {
                        List<Entry> out = new ArrayList<>();
                        for (DataSnapshot child : snap.getChildren()) {
                            Drill d = child.getValue(Drill.class);
                            if (d != null) out.add(new Entry(child.getKey(), d));
                        }
                        Collections.sort(out, new Comparator<Entry>() {
                            @Override
                            public int compare(Entry a, Entry b) {
                                return Long.compare(b.drill.createdAt, a.drill.createdAt);
                            }
                        });
                        cb.onSuccess(out);
                    }

                    @Override
                    public void onError(String message) { cb.onError(message); }
                });
    }
}
