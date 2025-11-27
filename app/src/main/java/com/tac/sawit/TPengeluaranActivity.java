package com.tac.sawit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tac.sawit.data.AppDatabase;
import com.tac.sawit.data.KategoriDao;
import com.tac.sawit.data.TransaksiDao;
import com.tac.sawit.model.Kategori;
import com.tac.sawit.model.Transaksi;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TPengeluaranActivity extends AppCompatActivity {

    private TextView textTanggal;
    private Spinner spinnerKategori;
    private EditText editKeterangan, editJumlah;

    private AppDatabase db;
    private KategoriDao kategoriDao;
    private TransaksiDao transaksiDao;

    private String tanggal;
    private int tahun;
    private int bulan;
    private String tipe;

    private List<Kategori> kategoriList = new ArrayList<>();
    private ArrayAdapter<String> kategoriAdapter;
    private int lastSelectedIndex = -1;
    private int transaksiId = -1;
    private boolean isEdit = false;
    private Transaksi existingTransaksi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpengeluaran);

        db = AppDatabase.getInstance(this);
        kategoriDao = db.kategoriDao();
        transaksiDao = db.transaksiDao();

        db.seedDefaultKategoriPengeluaranIfNeeded();

        ambilIntent();
        initViews();
        setupKategoriSpinner();
        setupButtonSimpan();
        setupButtonKembali();
    }

    private void ambilIntent() {
        transaksiId = getIntent().getIntExtra("id", -1);

        if (transaksiId != -1) {
            isEdit = true;
            existingTransaksi = transaksiDao.getById(transaksiId);
            if (existingTransaksi == null) {
                finish();
                return;
            }
            tanggal = existingTransaksi.tanggal;
            tahun = existingTransaksi.tahun;
            bulan = existingTransaksi.bulan;
            tipe = existingTransaksi.tipe; // "PENGELUARAN"
        } else {
            isEdit = false;
            tanggal = getIntent().getStringExtra("tanggal");
            tahun = getIntent().getIntExtra("tahun", 0);
            bulan = getIntent().getIntExtra("bulan", 0);
            tipe = getIntent().getStringExtra("tipe");
        }
    }

    private void initViews() {
        textTanggal = findViewById(R.id.textTanggalPengeluaran);
        spinnerKategori = findViewById(R.id.spinnerKategoriOut);
        editKeterangan = findViewById(R.id.editKeteranganOut);
        editJumlah = findViewById(R.id.editJumlahOut);

        textTanggal.setText("Tanggal: " + tanggal);

        if (isEdit && existingTransaksi != null) {
            editKeterangan.setText(existingTransaksi.keterangan != null ? existingTransaksi.keterangan : "");
            if (existingTransaksi.jumlah != 0) {
                editJumlah.setText(String.valueOf(existingTransaksi.jumlah));
            }
        }
        editJumlah.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editJumlah.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[Rp,.\\s]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = NumberFormat.getNumberInstance(new Locale("id", "ID")).format(parsed);

                            current = formatted;
                            editJumlah.setText(formatted);
                            editJumlah.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        current = "";
                        editJumlah.setText("");
                    }

                    editJumlah.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupKategoriSpinner() {
        loadKategori();

        kategoriAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getNamaKategoriListForSpinner()
        );
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        spinnerKategori.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       android.view.View view,
                                       int position,
                                       long id) {
                if (position == kategoriList.size()) {
                    showTambahKategoriDialogFromSpinner();
                } else {
                    lastSelectedIndex = position;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        if (!kategoriList.isEmpty()) {
            if (isEdit && existingTransaksi != null) {
                int index = 0;
                for (int i = 0; i < kategoriList.size(); i++) {
                    if (kategoriList.get(i).nama.equals(existingTransaksi.kategori)) {
                        index = i;
                        break;
                    }
                }
                lastSelectedIndex = index;
                spinnerKategori.setSelection(index);
            } else {
                lastSelectedIndex = 0;
                spinnerKategori.setSelection(0);
            }
        }
    }

    private void loadKategori() {
        kategoriList = kategoriDao.getByTipe("PENGELUARAN");
    }

    private List<String> getNamaKategoriListForSpinner() {
        List<String> list = new ArrayList<>();
        for (Kategori k : kategoriList) {
            list.add(k.nama);
        }
        list.add("Tambah Kategori Baru…");
        return list;
    }

    private void refreshKategoriSpinnerAndSelectLast() {
        loadKategori();
        kategoriAdapter.clear();
        kategoriAdapter.addAll(getNamaKategoriListForSpinner());
        kategoriAdapter.notifyDataSetChanged();

        if (!kategoriList.isEmpty()) {
            lastSelectedIndex = kategoriList.size() - 1;
            spinnerKategori.setSelection(lastSelectedIndex);
        }
    }

    private void showTambahKategoriDialogFromSpinner() {
        final EditText input = new EditText(this);
        input.setHint("Nama kategori pengeluaran");

        new AlertDialog.Builder(this)
                .setTitle("Kategori Baru")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String nama = input.getText().toString().trim();
                    if (nama.isEmpty()) {
                        Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        if (lastSelectedIndex >= 0 && lastSelectedIndex < kategoriList.size()) {
                            spinnerKategori.setSelection(lastSelectedIndex);
                        }
                        return;
                    }
                    Kategori k = new Kategori();
                    k.nama = nama;
                    k.tipe = "PENGELUARAN";
                    kategoriDao.insert(k);
                    refreshKategoriSpinnerAndSelectLast();
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    if (lastSelectedIndex >= 0 && lastSelectedIndex < kategoriList.size()) {
                        spinnerKategori.setSelection(lastSelectedIndex);
                    }
                })
                .show();
    }

    private void setupButtonSimpan() {
        findViewById(R.id.buttonSimpanOut).setOnClickListener(v -> simpanTransaksi());
    }

    private void simpanTransaksi() {
        if (tanggal == null || tanggal.isEmpty()) {
            Toast.makeText(this, "Tanggal tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (kategoriList == null || kategoriList.isEmpty()) {
            Toast.makeText(this, "Kategori belum ada, tambah dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        int posisi = spinnerKategori.getSelectedItemPosition();
        if (posisi < 0 || posisi >= kategoriList.size()) {
            Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        long jumlah = parseLong(editJumlah.getText().toString());
        if (jumlah <= 0) {
            Toast.makeText(this, "Jumlah pengeluaran harus > 0", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaksi t;
        if (isEdit && existingTransaksi != null) {
            t = existingTransaksi;
        } else {
            t = new Transaksi();
            t.tipe = "PENGELUARAN";
            t.bulan = bulan;
            t.tahun = tahun;
        }

        t.tanggal = tanggal;
        t.kategori = kategoriList.get(posisi).nama;
        t.jumlah = jumlah;
        t.keterangan = editKeterangan.getText().toString().trim();
        t.beratKg = 0.0;
        t.hargaPerKg = 0L;

        if (isEdit) {
            transaksiDao.update(t);
            Toast.makeText(this, "Transaksi pengeluaran diperbarui", Toast.LENGTH_SHORT).show();
        } else {
            transaksiDao.insert(t);
            Toast.makeText(this, "Transaksi pengeluaran tersimpan", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private long parseLong(String s) {
        try {
            return Long.parseLong(s.replace(".", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private void setupButtonKembali() {
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            finish();
        });
    }
}
