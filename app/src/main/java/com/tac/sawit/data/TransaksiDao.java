package com.tac.sawit.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tac.sawit.model.Transaksi;

import java.util.List;

@Dao
public interface TransaksiDao {

    @Insert
    long insert(Transaksi transaksi);

    @Update
    void update(Transaksi transaksi);

    @Delete
    void delete(Transaksi transaksi);

    @Query("SELECT * FROM transaksi WHERE id = :id LIMIT 1")
    Transaksi getById(int id);

    // ===== TOTAL TAHUNAN (mungkin sudah ada di kamu) =====
    @Query("SELECT IFNULL(SUM(jumlah), 0) FROM transaksi " +
            "WHERE tahun = :tahun AND tipe = 'PEMASUKAN'")
    long getTotalPemasukanTahunan(int tahun);

    @Query("SELECT IFNULL(SUM(jumlah), 0) FROM transaksi " +
            "WHERE tahun = :tahun AND tipe = 'PENGELUARAN'")
    long getTotalPengeluaranTahunan(int tahun);

    // ===== TOTAL BULANAN =====
    @Query("SELECT IFNULL(SUM(jumlah), 0) FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PEMASUKAN'")
    long getTotalPemasukanBulanan(int tahun, int bulan);

    @Query("SELECT IFNULL(SUM(jumlah), 0) FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PENGELUARAN'")
    long getTotalPengeluaranBulanan(int tahun, int bulan);

    // ===== LIST TRANSAKSI BULANAN =====
    @Query("SELECT * FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PEMASUKAN' " +
            "ORDER BY tanggal ASC, id ASC")
    List<Transaksi> getPemasukanBulanan(int tahun, int bulan);

    @Query("SELECT * FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PENGELUARAN' " +
            "ORDER BY tanggal ASC, id ASC")
    List<Transaksi> getPengeluaranBulanan(int tahun, int bulan);

    // ===== DATA UNTUK DASHBOARD (TAHUNAN) =====

    @Query("SELECT DISTINCT tahun FROM transaksi ORDER BY tahun DESC")
    List<Integer> getAvailableYears();

    @Query("SELECT bulan, " +
            "SUM(CASE WHEN tipe = 'PEMASUKAN' THEN jumlah ELSE 0 END) AS totalIn, " +
            "SUM(CASE WHEN tipe = 'PENGELUARAN' THEN jumlah ELSE 0 END) AS totalOut " +
            "FROM transaksi " +
            "WHERE tahun = :tahun " +
            "GROUP BY bulan " +
            "ORDER BY bulan")
    List<MonthlySummary> getMonthlySummary(int tahun);

    @Query("SELECT kategori AS nama, SUM(jumlah) AS total " +
            "FROM transaksi " +
            "WHERE tahun = :tahun AND tipe = 'PEMASUKAN' " +
            "GROUP BY kategori " +
            "ORDER BY total DESC")
    List<CategorySum> getPemasukanPerKategoriTahunan(int tahun);

    @Query("SELECT kategori AS nama, SUM(jumlah) AS total " +
            "FROM transaksi " +
            "WHERE tahun = :tahun AND tipe = 'PENGELUARAN' " +
            "GROUP BY kategori " +
            "ORDER BY total DESC")
    List<CategorySum> getPengeluaranPerKategoriTahunan(int tahun);
    // ===== DATA UNTUK DETAIL BULANAN (BULANAN) =====

    @Query("SELECT kategori AS nama, SUM(jumlah) AS total " +
            "FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PEMASUKAN' " +
            "GROUP BY kategori " +
            "ORDER BY total DESC")
    List<CategorySum> getPemasukanPerKategoriBulanan(int tahun, int bulan);

    @Query("SELECT kategori AS nama, SUM(jumlah) AS total " +
            "FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan AND tipe = 'PENGELUARAN' " +
            "GROUP BY kategori " +
            "ORDER BY total DESC")
    List<CategorySum> getPengeluaranPerKategoriBulanan(int tahun, int bulan);

    // ===== DATA UNTUK DETAIL BULANAN (HARIAN) =====
    @Query("SELECT " +
            "CAST(strftime('%d', tanggal) AS INTEGER) AS hari, " +
            "SUM(CASE WHEN tipe = 'PEMASUKAN' THEN jumlah ELSE 0 END) AS totalIn, " +
            "SUM(CASE WHEN tipe = 'PENGELUARAN' THEN jumlah ELSE 0 END) AS totalOut " +
            "FROM transaksi " +
            "WHERE tahun = :tahun AND bulan = :bulan " +
            "GROUP BY tanggal " +
            "ORDER BY tanggal ASC")
    List<DailySummary> getDailySummary(int tahun, int bulan);
}
