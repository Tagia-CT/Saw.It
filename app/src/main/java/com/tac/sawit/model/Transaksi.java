package com.tac.sawit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaksi")
public class Transaksi {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String tanggal;

    @NonNull
    public String tipe;

    @NonNull
    public String kategori;

    public long jumlah;

    public String keterangan;

    public int bulan;
    public int tahun;

    public double beratKg;
    public long hargaPerKg;
}
