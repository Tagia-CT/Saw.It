package com.tac.sawit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TransaksiActivity extends AppCompatActivity {

    private TextView textTanggal;
    private Button buttonPemasukan, buttonPengeluaran, buttonBack;

    private String tanggalStr;
    private int tahun;
    private int bulan;
    private int hari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi);

        textTanggal = findViewById(R.id.textTanggal);
        buttonPemasukan = findViewById(R.id.buttonPemasukan);
        buttonPengeluaran = findViewById(R.id.buttonPengeluaran);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(TransaksiActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        initDefaultDate();
        setupDatePicker();
        setupButtons();
    }

    private void initDefaultDate() {
        Calendar cal = Calendar.getInstance();
        tahun = cal.get(Calendar.YEAR);
        bulan = cal.get(Calendar.MONTH) + 1;
        hari = cal.get(Calendar.DAY_OF_MONTH);
        updateTanggalText();
    }

    private void setupDatePicker() {
        textTanggal.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        tahun = year;
                        bulan = monthOfYear + 1;
                        hari = dayOfMonth;
                        updateTanggalText();
                    },
                    tahun,
                    bulan - 1,
                    hari
            );
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void updateTanggalText() {
        tanggalStr = String.format("%04d-%02d-%02d", tahun, bulan, hari);
        textTanggal.setText(tanggalStr);
        textTanggal.setTextColor(getResources().getColor(R.color.colorTextPrimary));
    }

    private void setupButtons() {
        buttonPemasukan.setOnClickListener(v -> {
            if (!validTanggal()) return;
            Intent intent = new Intent(this, TPemasukanActivity.class);
            intent.putExtra("tanggal", tanggalStr);
            intent.putExtra("tahun", tahun);
            intent.putExtra("bulan", bulan);
            intent.putExtra("tipe", "PEMASUKAN");
            startActivity(intent);
//            finish();
        });

        buttonPengeluaran.setOnClickListener(v -> {
            if (!validTanggal()) return;
            Intent intent = new Intent(this, TPengeluaranActivity.class);
            intent.putExtra("tanggal", tanggalStr);
            intent.putExtra("tahun", tahun);
            intent.putExtra("bulan", bulan);
            intent.putExtra("tipe", "PENGELUARAN");
            startActivity(intent);
//            finish();
        });
    }

    private boolean validTanggal() {
        if (tanggalStr == null || tanggalStr.isEmpty()) {
            Toast.makeText(this, "Silakan pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show();
            return false;
        }

        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        if (tahun > tahunSekarang) {
            Toast.makeText(this, "Tahun tidak boleh melebihi tahun saat ini", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
