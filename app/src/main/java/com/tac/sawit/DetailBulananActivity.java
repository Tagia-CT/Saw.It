package com.tac.sawit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tac.sawit.data.AppDatabase;
import com.tac.sawit.data.TransaksiDao;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;


import com.tac.sawit.data.DailySummary;

public class DetailBulananActivity extends AppCompatActivity {

    public static final String EXTRA_TAHUN = "tahun";
    public static final String EXTRA_BULAN = "bulan";

    private int tahun;
    private int bulan;
    private com.google.android.material.button.MaterialButton buttonTitleMonth;
    private TextView textTotalInMonth, textTotalOutMonth, textLabaMonth;
//    private TextView textTitleMonth, textTotalInMonth, textTotalOutMonth, textLabaMonth;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private AppDatabase db;
    private TransaksiDao transaksiDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_bulanan);

        Button buttonDiagramHarian = findViewById(R.id.buttonDiagramHarian);
        buttonDiagramHarian.setOnClickListener(v -> showDiagramHarianDialog());

        db = AppDatabase.getInstance(this);
        transaksiDao = db.transaksiDao();

        tahun = getIntent().getIntExtra(EXTRA_TAHUN, 0);
        bulan = getIntent().getIntExtra(EXTRA_BULAN, 0);

        initViews();
        setupViewPagerAndTabs();
        refreshHeader();
    }

    private void initViews() {
        buttonTitleMonth = findViewById(R.id.buttonTitleMonth);
        textTotalInMonth = findViewById(R.id.textTotalInMonth);
        textTotalOutMonth = findViewById(R.id.textTotalOutMonth);
        textLabaMonth = findViewById(R.id.textLabaMonth);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailBulananActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });


        buttonTitleMonth.setText(getNamaBulan(bulan) + " " + tahun);
    }

    private void setupViewPagerAndTabs() {
        DetailBulananPagerAdapter adapter =
                new DetailBulananPagerAdapter(this, tahun, bulan);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Pemasukan");
                    else tab.setText("Pengeluaran");
                }).attach();
    }

    public void refreshHeader() {
        long totalIn = transaksiDao.getTotalPemasukanBulanan(tahun, bulan);
        long totalOut = transaksiDao.getTotalPengeluaranBulanan(tahun, bulan);
        long laba = totalIn - totalOut;

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        int colorLaba = laba >= 0 ? getColor(R.color.colorPositive) : getColor(R.color.colorNegative);

        textTotalInMonth.setText(nf.format(totalIn));
        textTotalInMonth.setTextColor(getColor(R.color.colorPositive));

        textTotalOutMonth.setText(nf.format(totalOut));
        textTotalOutMonth.setTextColor(getColor(R.color.colorNegative));

        textLabaMonth.setText(nf.format(laba));

        com.google.android.material.card.MaterialCardView cardLaba = findViewById(R.id.cardLabaMonth);
        if (laba < 0) {
            cardLaba.setCardBackgroundColor(getColor(R.color.colorNegative));
        } else {
            cardLaba.setCardBackgroundColor(getColor(R.color.colorPrimaryLight));
        }
    }

        private String getNamaBulan(int b) {
        String[] nama = {
                "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        if (b < 1 || b > 12) return "-";
        return nama[b];
    }

    private String formatBulanTahun(int bulan, int tahun) {
        return getNamaBulan(bulan) + " " + tahun;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeader();
    }

    public TransaksiDao getTransaksiDao() {
        return transaksiDao;
    }

    public AppDatabase getDbInstance() {
        return db;
    }

    private String formatRupiah(long nilai) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        nf.setMaximumFractionDigits(0);
        return nf.format(nilai);
    }

    private void showDiagramHarianDialog() {
        int tahunDetail = tahun;
        int bulanDetail = bulan;

        // 1. Ambil Data
        List<DailySummary> list = transaksiDao.getDailySummary(tahunDetail, bulanDetail);

        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Diagram Harian")
                    .setMessage("Belum ada data transaksi untuk bulan ini.")
                    .setPositiveButton("Tutup", null)
                    .show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_laba_tahunan, null);

        LineChart lineChart = view.findViewById(R.id.lineChartLaba);
        TextView textPemasukan = view.findViewById(R.id.textRataLaba);
        TextView textPengeluaran = view.findViewById(R.id.textMarginLaba);

        ArrayList<Entry> inEntries = new ArrayList<>();
        ArrayList<Entry> outEntries = new ArrayList<>();

        final ArrayList<String> xLabels = new ArrayList<>();

        long totalInBulan = 0;
        long totalOutBulan = 0;

        for (int i = 0; i < list.size(); i++) {
            DailySummary ds = list.get(i);

            float x = (float) i;

            inEntries.add(new Entry(x, ds.totalIn));
            outEntries.add(new Entry(x, ds.totalOut));

            xLabels.add(String.valueOf(ds.hari));

            totalInBulan += (long) ds.totalIn;
            totalOutBulan += (long) ds.totalOut;
        }

        LineDataSet setIn = new LineDataSet(inEntries, "Pemasukan");
        setIn.setColor(getColor(R.color.colorPositive));
        setIn.setLineWidth(2f);
        setIn.setCircleColor(getColor(R.color.colorPositive));
        setIn.setCircleRadius(4f);
        setIn.setDrawValues(false);
        setIn.setMode(LineDataSet.Mode.LINEAR);

        LineDataSet setOut = new LineDataSet(outEntries, "Pengeluaran");
        setOut.setColor(getColor(R.color.colorNegative));
        setOut.setLineWidth(2f);
        setOut.setCircleColor(getColor(R.color.colorNegative));
        setOut.setCircleRadius(4f);
        setOut.setDrawValues(false);
        setOut.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(setIn, setOut);
        lineChart.setData(lineData);

        // 3. Konfigurasi Chart Umum
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true); // Izinkan zoom cubit
        lineChart.setDragEnabled(true);

        // 4. Konfigurasi Sumbu X (KUNCI PERBAIKAN)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                }
                return "";
            }
        });

        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(list.size() - 0.5f);


        // 5. Konfigurasi Sumbu Y
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setAxisMinimum(0f);

        // Legend & Marker
        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        ValueMarkerView marker = new ValueMarkerView(this, R.layout.marker_value, lineChart);
        marker.setChartView(lineChart);
        lineChart.setMarker(marker);

        // Refresh Chart
        lineChart.invalidate();

        // Update Text Summary
        textPemasukan.setText("Total pemasukan: " + formatRupiah(totalInBulan));
        textPengeluaran.setText("Total pengeluaran: " + formatRupiah(totalOutBulan));
        textPemasukan.setTextColor(getColor(R.color.colorPositive));
        textPengeluaran.setTextColor(getColor(R.color.colorNegative));

        new AlertDialog.Builder(this)
                .setTitle("Diagram Harian " + formatBulanTahun(bulanDetail, tahunDetail))
                .setView(view)
                .setPositiveButton("Tutup", null)
                .show();
    }
}
