package com.tac.sawit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tac.sawit.data.AppDatabase;
import com.tac.sawit.data.TransaksiDao;
import com.tac.sawit.model.Transaksi;

import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.tac.sawit.data.CategorySum;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.text.NumberFormat;
import java.util.Locale;

public class PemasukanFragment extends Fragment {

    private static final String ARG_TAHUN = "tahun";
    private static final String ARG_BULAN = "bulan";

    private int tahun;
    private int bulan;

    private RecyclerView recyclerView;
    private PemasukanAdapter adapter;

    private AppDatabase db;
    private TransaksiDao transaksiDao;
    private PieChart pieChart;
    private LinearLayout layoutKategori;

    private final int[] PIE_COLORS = {
            0xFFF44336, 0xFF9C27B0, 0xFFFF9800, 0xFF03A9F4,
            0xFF4CAF50, 0xFFCDDC39, 0xFFFFC107, 0xFFE91E63,
    };

    public static PemasukanFragment newInstance(int tahun, int bulan) {
        PemasukanFragment f = new PemasukanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAHUN, tahun);
        args.putInt(ARG_BULAN, bulan);
        f.setArguments(args);
        return f;
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            tahun = getArguments().getInt(ARG_TAHUN);
//            bulan = getArguments().getInt(ARG_BULAN);
//        }
//
//        if (getActivity() instanceof DetailBulananActivity) {
//            db = ((DetailBulananActivity) getActivity()).getDbInstance();
//            transaksiDao = db.getTransaksiDao();
//        } else {
//            db = AppDatabase.getInstance(requireContext());
//            transaksiDao = db.transaksiDao();
//        }
//    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tahun = getArguments().getInt(ARG_TAHUN);
            bulan = getArguments().getInt(ARG_BULAN);
        }

        if (getActivity() instanceof DetailBulananActivity) {
            db = ((DetailBulananActivity) getActivity()).getDbInstance();
            transaksiDao = db.transaksiDao();
        } else {
            db = AppDatabase.getInstance(requireContext());
            transaksiDao = db.transaksiDao();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pemasukan, container, false);

        recyclerView = v.findViewById(R.id.recyclerPemasukan);
        pieChart = v.findViewById(R.id.pieChartPemasukan);
        layoutKategori = v.findViewById(R.id.layoutKategoriPemasukan);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PemasukanAdapter(new ArrayList<>(), this::onItemClick);
        recyclerView.setAdapter(adapter);

        loadData();
        loadChartData();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadChartData();
    }

    private void loadData() {
        List<Transaksi> list = transaksiDao.getPemasukanBulanan(tahun, bulan);
        adapter.setData(list);
    }

    private void onItemClick(Transaksi t) {
        String[] options = {"Edit", "Hapus", "Batal"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Transaksi Pemasukan")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Edit
                        editTransaksi(t);
                    } else if (which == 1) {
                        deleteTransaksi(t);
                    }
                })
                .show();
    }

    private void editTransaksi(Transaksi t) {
        Intent intent = new Intent(requireContext(), TPemasukanActivity.class);
        intent.putExtra("id", t.id);
        startActivity(intent);
    }

    private void deleteTransaksi(Transaksi t) {
        transaksiDao.delete(t);
        loadData();
        loadChartData();
        if (getActivity() instanceof DetailBulananActivity) {
            ((DetailBulananActivity) getActivity()).refreshHeader();
        }
    }

    private void loadChartData() {
        long totalSemuaTransaksi = transaksiDao.getTotalPemasukanBulanan(tahun, bulan);
        List<CategorySum> list = transaksiDao.getPemasukanPerKategoriBulanan(tahun, bulan);

        layoutKategori.removeAllViews();
        pieChart.setVisibility(View.VISIBLE);

        if (list == null || list.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            TextView tv = new TextView(getContext());
            tv.setText("Tidak ada rincian kategori pemasukan untuk bulan ini.");
            tv.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            layoutKategori.addView(tv);
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            CategorySum cs = list.get(i);
            if (cs.total > 0) {
                entries.add(new PieEntry(cs.total, cs.nama));

                int color = PIE_COLORS[i % PIE_COLORS.length];
                colors.add(color);

                float persentase = (float) cs.total / totalSemuaTransaksi * 100f;
                String persentaseStr = String.format(Locale.getDefault(), "%.1f%%", persentase);

                TextView tv = new TextView(getContext());
                tv.setText("• " + cs.nama + ": " + persentaseStr);
                tv.setTextColor(color);
                tv.setTextSize(14f);
                layoutKategori.addView(tv);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(android.graphics.Color.TRANSPARENT);
        dataSet.setValueTextSize(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));

        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(getContext().getColor(R.color.colorTextPrimary));
        pieChart.setDrawEntryLabels(true);

        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(100f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineWidth(1f);
        dataSet.setValueLineColor(getContext().getColor(R.color.colorTextSecondary));

        pieChart.setDrawEntryLabels(false);

        data.setValueTextColor(getContext().getColor(R.color.colorTextPrimary));
        data.setValueTextSize(10f);

        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));

        pieChart.invalidate();
    }
}
