package com.mathwhiz.app.ui;

import android.graphics.Color;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.mathwhiz.app.logic.StatsAggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Renders the distribution of wrong answers across the most-missed problem types. */
public final class MostMissedChart {
    private static final List<Integer> COLORS = Arrays.asList(
            Color.rgb(40, 115, 52),
            Color.rgb(239, 108, 0),
            Color.rgb(21, 101, 192),
            Color.rgb(198, 40, 40),
            Color.rgb(121, 85, 72));

    private MostMissedChart() {}

    public static void bind(PieChart chart, List<StatsAggregator.Stat> stats) {
        List<PieEntry> entries = new ArrayList<>();
        for (StatsAggregator.Stat stat : stats) {
            if (stat.wrong() > 0) {
                entries.add(new PieEntry(stat.wrong(), stat.label + " (" + stat.wrong() + ")"));
            }
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No missed answers yet");
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Wrong answers");
        dataSet.setColors(COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return Integer.toString(Math.round(value));
            }
        });

        chart.setData(data);
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(false);
        chart.setDrawHoleEnabled(false);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.rgb(38, 50, 56));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        chart.setTouchEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setEntryLabelColor(Color.rgb(38, 50, 56));
        chart.setExtraOffsets(12f, 12f, 12f, 12f);
        chart.animateY(600);
        chart.invalidate();
    }
}
