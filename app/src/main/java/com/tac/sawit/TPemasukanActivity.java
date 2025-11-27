package com.tac.sawit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class TPemasukanActivity extends AppCompatActivity {

    private TextView textTanggal, textTotalIn;
    private Spinner spinnerKategori;
    private EditText editKeterangan, editBeratKg, editHargaPerKg;

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
        setContentView(R.layout.activity_tpemasukan);

        db = AppDatabase.getInstance(this);
        kategoriDao = db.kategoriDao();
        transaksiDao = db.transaksiDao();

        ambilIntent();
        initViews();
        setupKategoriSpinner();
        setupTextWatchers();
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
            tipe = existingTransaksi.tipe;
        } else {
            isEdit = false;
            tanggal = getIntent().getStringExtra("tanggal");
            tahun = getIntent().getIntExtra("tahun", 0);
            bulan = getIntent().getIntExtra("bulan", 0);
            tipe = getIntent().getStringExtra("tipe");
        }
    }

    private void initViews() {
        textTanggal = findViewById(R.id.textTanggalPemasukan);
        textTotalIn = findViewById(R.id.textTotalIn);
        spinnerKategori = findViewById(R.id.spinnerKategoriIn);
        editKeterangan = findViewById(R.id.editKeteranganIn);
        editBeratKg = findViewById(R.id.editBeratKg);
        editHargaPerKg = findViewById(R.id.editHargaPerKg);

        textTanggal.setText("Tanggal: " + tanggal);
        textTotalIn.setText("Total: " + formatRupiah(0));

        if (isEdit && existingTransaksi != null) {
            editKeterangan.setText(existingTransaksi.keterangan != null ? existingTransaksi.keterangan : "");
            if (existingTransaksi.beratKg != 0) {
                editBeratKg.setText(String.valueOf(existingTransaksi.beratKg));
            }
            if (existingTransaksi.hargaPerKg != 0) {
                editHargaPerKg.setText(String.valueOf(existingTransaksi.hargaPerKg));
            }
        }
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
                // cari index kategori yang sama
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
        kategoriList = kategoriDao.getByTipe("PEMASUKAN");
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
        input.setHint("Nama kategori pemasukan");

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
                    k.tipe = "PEMASUKAN";
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

    private void setupTextWatchers() {
        editBeratKg.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                hitungTotal();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        editHargaPerKg.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editHargaPerKg.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[Rp,.\\s]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = NumberFormat.getNumberInstance(new Locale("id", "ID")).format(parsed);

                            current = formatted;
                            editHargaPerKg.setText(formatted);
                            editHargaPerKg.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        current = "";
                        editHargaPerKg.setText("");
                    }

                    editHargaPerKg.addTextChangedListener(this);
                }

                hitungTotal();
            }
        });
    }

    private void hitungTotal() {
        double berat = parseDouble(editBeratKg.getText().toString());
        long hargaPerKg = parseLong(editHargaPerKg.getText().toString());
        long total = (long) (berat * hargaPerKg);
        textTotalIn.setText("Total: " + formatRupiah(total));
    }

    private void setupButtonSimpan() {
        findViewById(R.id.buttonSimpanIn).setOnClickListener(v -> simpanTransaksi());
    }

    private void setupButtonKembali() {
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            finish();
        });
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

        double berat = parseDouble(editBeratKg.getText().toString());
        long hargaPerKg = parseLong(editHargaPerKg.getText().toString());
        if (berat <= 0 || hargaPerKg <= 0) {
            Toast.makeText(this, "Berat dan harga/kg harus > 0", Toast.LENGTH_SHORT).show();
            return;
        }

        long total = (long) (berat * hargaPerKg);

        Transaksi t;
        if (isEdit && existingTransaksi != null) {
            t = existingTransaksi;
        } else {
            t = new Transaksi();
            t.tipe = "PEMASUKAN";
            t.bulan = bulan;
            t.tahun = tahun;
        }

        t.tanggal = tanggal;
        t.kategori = kategoriList.get(posisi).nama;
        t.jumlah = total;
        t.keterangan = editKeterangan.getText().toString().trim();
        t.beratKg = berat;
        t.hargaPerKg = hargaPerKg;

        if (isEdit) {
            transaksiDao.update(t);
            Toast.makeText(this, "Transaksi pemasukan diperbarui", Toast.LENGTH_SHORT).show();
        } else {
            transaksiDao.insert(t);
            Toast.makeText(this, "Transaksi pemasukan tersimpan", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatRupiah(long amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return nf.format(amount);
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private long parseLong(String s) {
        try {
            return Long.parseLong(s.replace(".", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0L;
        }
    }
}
