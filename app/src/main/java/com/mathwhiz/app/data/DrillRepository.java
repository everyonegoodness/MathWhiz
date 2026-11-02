package com.mathwhiz.app.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mathwhiz.app.model.Drill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Saves a drill and only reports success after Firebase confirms the write. */
    public void saveDrill(Drill drill, DataCallback<Entry> cb) {
        DatabaseReference ref = root().child("drills").push();
        final String key = ref.getKey();
        ref.setValue(toMap(drill))
                .addOnSuccessListener(unused -> cb.onSuccess(new Entry(key, drill)))
                .addOnFailureListener(e ->
                        cb.onError("The drill could not be saved to the server: " + e.getMessage()));
    }

    public void updateDrill(String id, Drill drill, DataCallback<Void> cb) {
        root().child("drills").child(id).setValue(toMap(drill))
                .addOnSuccessListener(unused -> cb.onSuccess(null))
                .addOnFailureListener(e ->
                        cb.onError("The drill could not be updated: " + e.getMessage()));
    }

    public void deleteDrill(String id, DataCallback<Void> cb) {
        root().child("drills").child(id).removeValue()
                .addOnSuccessListener(unused -> cb.onSuccess(null))
                .addOnFailureListener(e ->
                        cb.onError("The drill could not be deleted: " + e.getMessage()));
    }

    private Map<String, Object> toMap(Drill drill) {
        Map<String, Object> values = new HashMap<>();
        values.put("teacherUid", drill.teacherUid);
        values.put("title", drill.title);
        values.put("grade", drill.grade);
        values.put("section", drill.section);
        values.put("skillId", drill.skillId);
        values.put("topic", drill.topic);
        values.put("minNumber", drill.minNumber);
        values.put("maxNumber", drill.maxNumber);
        values.put("questionCount", drill.questionCount);
        values.put("format", drill.format);
        values.put("mode", drill.mode);
        values.put("timeLimitSec", drill.timeLimitSec);
        values.put("createdAt", drill.createdAt);
        return values;
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
