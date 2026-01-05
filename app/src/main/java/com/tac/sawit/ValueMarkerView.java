package com.tac.sawit;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.NumberFormat;
import java.util.Locale;

public class ValueMarkerView extends MarkerView {

    private final TextView tvValue;
    private final LineChart chart;
    private final NumberFormat rupiahFormat;

    // constructor khusus untuk LineChart
    public ValueMarkerView(Context context, int layoutResource, LineChart chart) {
        super(context, layoutResource);
        this.chart = chart;
        this.tvValue = findViewById(R.id.textValue);

        rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        rupiahFormat.setMaximumFractionDigits(0);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // format nilai ke Rupiah
        long val = (long) e.getY();
        tvValue.setText(rupiahFormat.format(val));

        // cari dataset (Pemasukan / Pengeluaran / Laba) yang sedang di-highlight
        if (chart != null && chart.getData() != null) {
            com.github.mikephil.charting.interfaces.datasets.ILineDataSet dataSet =
                    (com.github.mikephil.charting.interfaces.datasets.ILineDataSet) chart.getData().getDataSetByIndex(highlight.getDataSetIndex());

            if (dataSet != null) {
                int color = dataSet.getColor();
                tvValue.setTextColor(color);
            }
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // posisi marker di tengah titik dan agak di atas
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 10f);
    }
}
