package com.tac.sawit.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tac.sawit.model.Kategori;
import com.tac.sawit.model.Transaksi;

@Database(
        entities = {Transaksi.class, Kategori.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TransaksiDao transaksiDao();
    public abstract KategoriDao kategoriDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "sawit_db"
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public void seedDefaultKategoriPengeluaranIfNeeded() {
        KategoriDao dao = kategoriDao();
        // cek apakah sudah ada kategori pengeluaran
        if (dao.getByTipe("PENGELUARAN") != null && !dao.getByTipe("PENGELUARAN").isEmpty()) {
            return;
        }

        String[] defaults = {
                "Pembelian Sawit Luar",
                "Upah Tenaga Kerja",
                "Perawatan Kebun",
                "Transportasi",
                "Konsumsi"
        };

        for (String nama : defaults) {
            Kategori k = new Kategori();
            k.nama = nama;
            k.tipe = "PENGELUARAN";
            dao.insert(k);
        }
    }
}
