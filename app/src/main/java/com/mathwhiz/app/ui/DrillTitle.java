package com.mathwhiz.app.ui;

import com.mathwhiz.app.model.Drill;
import com.mathwhiz.app.model.Topic;

public final class DrillTitle {
    private DrillTitle() {}

    public static String display(Drill drill) {
        if (drill == null) return "";
        String title = drill.title == null ? "" : drill.title.trim();
        return title.isEmpty() ? Topic.label(drill.topic) : title;
    }
}
