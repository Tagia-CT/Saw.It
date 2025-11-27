package com.tac.sawit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.tac.sawit.data.AppDatabase;
import com.tac.sawit.data.CategorySum;
import com.tac.sawit.data.MonthlySummary;
import com.tac.sawit.data.TransaksiDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.view.LayoutInflater;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;

public class DashboardActivity extends AppCompatActivity {

    private Spinner spinnerYear;
    private View layoutTotalIn, layoutTotalOut, layoutLaba;
    private LinearLayout layoutMonths;
    private TextView textTotalIn, textTotalOut, textTotalLaba;
    private Button buttonAdd;

    private AppDatabase db;
    private TransaksiDao transaksiDao;

    private int tahunAktif = 0;
    private long totalInTahunan;
    private long totalOutTahunan;
    private List<MonthlySummary> monthlySummaries = new ArrayList<>();

    private int[] PIE_COLORS = {
            0xFFF44336, 0xFF9C27B0, 0xFFFF9800, 0xFF03A9F4,
            0xFF4CAF50, 0xFFCDDC39, 0xFFFFC107, 0xFFE91E63,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = AppDatabase.getInstance(this);
        transaksiDao = db.transaksiDao();

        spinnerYear    = findViewById(R.id.spinnerYear);
        layoutTotalIn  = findViewById(R.id.layoutTotalIn);
        layoutTotalOut = findViewById(R.id.layoutTotalOut);
        layoutLaba     = findViewById(R.id.layoutLaba);
        layoutMonths   = findViewById(R.id.layoutMonths);
        buttonAdd      = findViewById(R.id.buttonAdd);

        textTotalIn    = findViewById(R.id.textTotalIn);
        textTotalOut   = findViewById(R.id.textTotalOut);
        textTotalLaba  = findViewById(R.id.textTotalLaba);

        layoutTotalIn.setOnClickListener(v -> showPemasukanKategoriDialog());
        layoutTotalOut.setOnClickListener(v -> showPengeluaranKategoriDialog());
        layoutLaba.setOnClickListener(v -> showLabaTahunanDialog());

        initViews();
        setupSpinnerYear();
        setupClickListeners();
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (tahunAktif != 0) {
            loadDataForYear(tahunAktif);
        }
    }

    private void initViews() {
        spinnerYear = findViewById(R.id.spinnerYear);
        layoutTotalIn = findViewById(R.id.layoutTotalIn);
        layoutTotalOut = findViewById(R.id.layoutTotalOut);
        layoutLaba = findViewById(R.id.layoutLaba);
        layoutMonths = findViewById(R.id.layoutMonths);

        textTotalIn = findViewById(R.id.textTotalIn);
        textTotalOut = findViewById(R.id.textTotalOut);
        textTotalLaba = findViewById(R.id.textTotalLaba);

        buttonAdd = findViewById(R.id.buttonAdd);
    }

    private void setupSpinnerYear() {
        List<Integer> years = transaksiDao.getAvailableYears();
        if (years == null || years.isEmpty()) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            years = new ArrayList<>();
            years.add(currentYear);
        }

        ArrayAdapter<Integer> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        tahunAktif = years.get(0);
        spinnerYear.setSelection(0);

        spinnerYear.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                tahunAktif = (int) parent.getItemAtPosition(position);
                loadDataForYear(tahunAktif);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        loadDataForYear(tahunAktif);
    }

    private void loadDataForYear(int tahun) {
        totalInTahunan = transaksiDao.getTotalPemasukanTahunan(tahun);
        totalOutTahunan = transaksiDao.getTotalPengeluaranTahunan(tahun);
        long laba = totalInTahunan - totalOutTahunan;

        textTotalIn.setText(formatRupiah(totalInTahunan));
        textTotalOut.setText(formatRupiah(totalOutTahunan));
        textTotalLaba.setText(formatRupiah(laba));

        monthlySummaries = transaksiDao.getMonthlySummary(tahun);
        renderMonths();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

//    private void renderMonths() {
//        layoutMonths.removeAllViews();
//
//        if (monthlySummaries == null || monthlySummaries.isEmpty()) {
//            TextView tv = new TextView(this);
//            tv.setText("Belum ada data transaksi untuk tahun ini.");
//            tv.setTextSize(16);
//            tv.setTextColor(getColor(R.color.colorTextSecondary));
//            layoutMonths.addView(tv);
//            return;
//        }
//
//        for (MonthlySummary ms : monthlySummaries) {
//            long laba = ms.totalIn - ms.totalOut;
//
//            TextView tv = new TextView(this);
//
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            lp.topMargin = dp(8);
//            tv.setLayoutParams(lp);
//
//            tv.setBackgroundResource(R.drawable.bg_card);
//            tv.setPadding(dp(12), dp(12), dp(12), dp(12));
//
//            tv.setTextSize(16);
//            tv.setTextColor(getColor(R.color.colorTextPrimary));
//
//            String bulanText = getNamaBulan(ms.bulan);
//            String labaText = "Laba: " + formatRupiah(laba);
//            tv.setText(bulanText + " • " + labaText);
//
//            int colorLaba = laba >= 0
//                    ? getColor(R.color.colorPositive)
//                    : getColor(R.color.colorNegative);
//            tv.setTextColor(colorLaba);
//
//            tv.setOnClickListener(v -> {
//                Intent intent = new Intent(DashboardActivity.this, DetailBulananActivity.class);
//                intent.putExtra(DetailBulananActivity.EXTRA_TAHUN, tahunAktif);
//                intent.putExtra(DetailBulananActivity.EXTRA_BULAN, ms.bulan);
//                startActivity(intent);
//            });
//
//            layoutMonths.addView(tv);
//        }
//    }

    private void renderMonths() {
        layoutMonths.removeAllViews();

        if (monthlySummaries == null || monthlySummaries.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Belum ada data transaksi untuk tahun ini.");
            tv.setTextSize(16);
            tv.setTextColor(getColor(R.color.colorTextSecondary));
            tv.setPadding(dp(4), dp(16), dp(4), dp(16));
            layoutMonths.addView(tv);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MonthlySummary ms : monthlySummaries) {
            long laba = ms.totalIn - ms.totalOut;

            View cardView = inflater.inflate(R.layout.item_monthly_summary, layoutMonths, false);

            TextView textMonthYear = cardView.findViewById(R.id.textMonthYear);
            TextView textLabaRugi = cardView.findViewById(R.id.textLabaRugi);

            String bulanText = getNamaBulan(ms.bulan);
            int tahun = tahunAktif;

            textMonthYear.setText(bulanText + " " + tahun);
            textLabaRugi.setText(formatRupiah(laba));

            int colorLaba = laba >= 0
                    ? getColor(R.color.colorPositive)
                    : getColor(R.color.colorNegative);
            textLabaRugi.setTextColor(colorLaba);

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, DetailBulananActivity.class);
                intent.putExtra(DetailBulananActivity.EXTRA_TAHUN, tahunAktif);
                intent.putExtra(DetailBulananActivity.EXTRA_BULAN, ms.bulan);
                startActivity(intent);
            });

            layoutMonths.addView(cardView);
        }
    }


    private void setupClickListeners() {
        layoutTotalIn.setOnClickListener(v -> showPemasukanKategoriDialog());
        layoutTotalOut.setOnClickListener(v -> showPengeluaranKategoriDialog());
        layoutLaba.setOnClickListener(v -> showLabaTahunanDialog());
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransaksiActivity.class);
            startActivity(intent);
        });
    }

    private void showPemasukanDialog() {
        List<CategorySum> list = transaksiDao.getPemasukanPerKategoriTahunan(tahunAktif);
        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Rincian Pemasukan " + tahunAktif)
                    .setMessage("Belum ada data pemasukan.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (CategorySum cs : list) {
            sb.append(cs.nama)
                    .append(" : ")
                    .append(formatRupiah(cs.total))
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Rincian Pemasukan " + tahunAktif)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPengeluaranDialog() {
        List<CategorySum> list = transaksiDao.getPengeluaranPerKategoriTahunan(tahunAktif);
        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Rincian Pengeluaran " + tahunAktif)
                    .setMessage("Belum ada data pengeluaran.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (CategorySum cs : list) {
            sb.append(cs.nama)
                    .append(" : ")
                    .append(formatRupiah(cs.total))
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Rincian Pengeluaran " + tahunAktif)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLabaDialog() {
        if (monthlySummaries == null || monthlySummaries.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Analisis Laba " + tahunAktif)
                    .setMessage("Belum ada data.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        int jumlahBulanAdaData = monthlySummaries.size();
        long totalLaba = 0;
        for (MonthlySummary ms : monthlySummaries) {
            totalLaba += (ms.totalIn - ms.totalOut);
        }

        double rataRataLaba = (double) totalLaba / jumlahBulanAdaData;
        double margin = 0.0;
        if (totalInTahunan > 0) {
            margin = (double) totalLaba / (double) totalInTahunan * 100.0;
        }

        String pesan = "Rata-rata laba per bulan: " + formatRupiah(Math.round(rataRataLaba))
                + "\nTotal laba tahunan: " + formatRupiah(totalLaba)
                + "\nMargin laba: " + String.format(Locale.getDefault(), "%.2f", margin) + " %";

        new AlertDialog.Builder(this)
                .setTitle("Analisis Laba " + tahunAktif)
                .setMessage(pesan)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatRupiah(long amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return nf.format(amount);
    }

    private String getNamaBulan(int bulan) {
        switch (bulan) {
            case 1:  return "Januari";
            case 2:  return "Februari";
            case 3:  return "Maret";
            case 4:  return "April";
            case 5:  return "Mei";
            case 6:  return "Juni";
            case 7:  return "Juli";
            case 8:  return "Agustus";
            case 9:  return "September";
            case 10: return "Oktober";
            case 11: return "November";
            case 12: return "Desember";
            default: return "Bulan " + bulan;
        }
    }

    private void showPemasukanKategoriDialog() {
        List<CategorySum> list = transaksiDao.getPemasukanPerKategoriTahunan(tahunAktif);
        showRingkasanKategoriDialog(list,
                "Pemasukan per Kategori (" + tahunAktif + ")");
    }

    private void showPengeluaranKategoriDialog() {
        List<CategorySum> list = transaksiDao.getPengeluaranPerKategoriTahunan(tahunAktif);
        showRingkasanKategoriDialog(list,
                "Pengeluaran per Kategori (" + tahunAktif + ")");
    }

    private void showRingkasanKategoriDialog(List<CategorySum> list, String title) {
        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage("Belum ada data untuk tahun ini.")
                    .setPositiveButton("Tutup", null)
                    .show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_ringkasan_kategori, null);

        PieChart pieChart = view.findViewById(R.id.pieChartKategori);
        TextView textTotalKategori = view.findViewById(R.id.textTotalKategori);
        LinearLayout layoutDetail = view.findViewById(R.id.layoutDetailKategori);

        long totalAll = 0;
        for (CategorySum cs : list) {
            totalAll += cs.total;
        }
        textTotalKategori.setText("Total: " + formatRupiah(totalAll));

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (CategorySum cs : list) {
            if (cs.total > 0) {
                entries.add(new PieEntry(cs.total, cs.nama));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(10f);

        List<Integer> colors = new ArrayList<>();
        for (int color : PIE_COLORS) {
            colors.add(color);
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleColor(0x00000000);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(58f);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.invalidate();

        layoutDetail.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            CategorySum cs = list.get(i);
            TextView tv = new TextView(this);
            tv.setText("• " + cs.nama + " — " + formatRupiah(cs.total));
            int colorIndex = i % PIE_COLORS.length;
            tv.setTextColor(PIE_COLORS[colorIndex]);
            tv.setTextSize(14f);
            layoutDetail.addView(tv);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Tutup", null)
                .show();
    }

    public class MonthXAxisRenderer extends XAxisRenderer {

        public MonthXAxisRenderer(ViewPortHandler viewPortHandler,
                                  XAxis xAxis,
                                  Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void computeAxisValues(float min, float max) {
            int count = 12;

            if (mAxis.mEntries.length < count) {
                mAxis.mEntries = new float[count];
            }
            mAxis.mEntryCount = count;

            for (int i = 0; i < count; i++) {
                mAxis.mEntries[i] = i + 1; // 1..12
            }

            mAxis.mAxisMinimum = 1f;
            mAxis.mAxisMaximum = 12f;
            mAxis.mAxisRange   = 11f;
        }
    }

    private void showLabaTahunanDialog() {
        List<MonthlySummary> list = transaksiDao.getMonthlySummary(tahunAktif);
        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Analisis Laba Tahunan " + tahunAktif)
                    .setMessage("Belum ada data transaksi untuk tahun ini.")
                    .setPositiveButton("Tutup", null)
                    .show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_laba_tahunan, null);

        LineChart lineChart = view.findViewById(R.id.lineChartLaba);
        TextView textRataLaba = view.findViewById(R.id.textRataLaba);
        TextView textMarginLaba = view.findViewById(R.id.textMarginLaba);

        Map<Integer, MonthlySummary> map = new HashMap<>();
        for (MonthlySummary ms : list) {
            map.put(ms.bulan, ms);
        }

        ArrayList<Entry> inEntries = new ArrayList<>();
        ArrayList<Entry> outEntries = new ArrayList<>();
        ArrayList<Entry> labaEntries = new ArrayList<>();

        List<Integer> labaCircleColors = new ArrayList<>();

        long totalLabaTahun = 0;
        long totalInTahun = 0;

        for (int bulan = 1; bulan <= 12; bulan++) {
            MonthlySummary ms = map.get(bulan);

            float inVal = ms != null ? ms.totalIn : 0f;
            float outVal = ms != null ? ms.totalOut : 0f;
            float labaVal = inVal - outVal;

            float x = bulan;
            inEntries.add(new Entry(x, inVal));
            outEntries.add(new Entry(x, outVal));
            labaEntries.add(new Entry(x, labaVal));

            if (labaVal < 0) {
                labaCircleColors.add(getColor(R.color.colorNegative));
            } else {
                labaCircleColors.add(getColor(R.color.colorlaba));
            }

            if (ms != null) {
                totalLabaTahun += (long) labaVal;
                totalInTahun += (long) inVal;
            }
        }

        int nBulan = 12;
        long rataLaba = totalLabaTahun / nBulan;
        double margin = totalInTahun == 0
                ? 0.0
                : (double) totalLabaTahun / (double) totalInTahun * 100.0;

        // --- Dataset 1: Pemasukan ---
        LineDataSet setIn = new LineDataSet(inEntries, "Pemasukan");
        setIn.setColor(getColor(R.color.colorPositive));
        setIn.setLineWidth(2f);

        // GAYA LINGKARAN
        setIn.setDrawCircles(true);
        setIn.setCircleColor(getColor(R.color.colorPositive));
        setIn.setCircleRadius(4f);

        setIn.setDrawValues(false);
        setIn.setMode(LineDataSet.Mode.LINEAR);

        // --- Dataset 2: Pengeluaran ---
        LineDataSet setOut = new LineDataSet(outEntries, "Pengeluaran");
        setOut.setColor(getColor(R.color.colorNegative));
        setOut.setLineWidth(2f);

        // GAYA LINGKARAN (Sama seperti Bulanan)
        setOut.setDrawCircles(true);
        setOut.setCircleColor(getColor(R.color.colorNegative));
        setOut.setCircleRadius(4f);
        // HAPUS setDrawCircleHole(false)

        setOut.setDrawValues(false);
        setOut.setMode(LineDataSet.Mode.LINEAR);

        // --- Dataset 3: Laba Bersih ---
        LineDataSet setLaba = new LineDataSet(labaEntries, "Laba bersih");
        setLaba.setColor(getColor(R.color.colorlaba));
        setLaba.setLineWidth(3f);

        setLaba.setDrawCircles(true);
        setLaba.setCircleColors(labaCircleColors);
        setLaba.setCircleRadius(4f);

        setLaba.setDrawValues(false);
        setLaba.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(setIn, setOut, setLaba);
        lineChart.setData(lineData);

        // --- KONFIGURASI CHART ---
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Konfigurasi Sumbu X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(12.5f);

        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] bulan = {
                    "", "Jan", "Feb", "Mar", "Apr",
                    "Mei", "Jun", "Jul", "Agu",
                    "Sep", "Okt", "Nov", "Des"
            };

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int v = Math.round(value);
                if (v < 1 || v > 12) return "";
                return bulan[v];
            }
        });

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setZeroLineWidth(1.5f);

        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYOffset(4f);

        ValueMarkerView marker = new ValueMarkerView(this, R.layout.marker_value, lineChart);
        marker.setChartView(lineChart);
        lineChart.setMarker(marker);

        lineChart.invalidate();

        textRataLaba.setText("Rata-rata laba bersih/bulan: " + formatRupiah(rataLaba));
        textMarginLaba.setText(String.format(Locale.getDefault(),
                "Margin laba bersih: %.1f%%", margin));

        int colorRataLaba = rataLaba >= 0
                ? getColor(R.color.colorPositive)
                : getColor(R.color.colorNegative);
        textRataLaba.setTextColor(colorRataLaba);
        textMarginLaba.setTextColor(colorRataLaba);

        new AlertDialog.Builder(this)
                .setTitle("Analisis Laba Tahunan " + tahunAktif)
                .setView(view)
                .setPositiveButton("Tutup", null)
                .show();
    }
}
