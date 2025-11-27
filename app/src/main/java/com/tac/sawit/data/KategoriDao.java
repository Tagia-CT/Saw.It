package com.tac.sawit.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tac.sawit.model.Kategori;

import java.util.List;

@Dao
public interface KategoriDao {

    @Insert
    long insert(Kategori kategori);

    @Update
    int update(Kategori kategori);

    @Delete
    int delete(Kategori kategori);

    @Query("SELECT * FROM kategori WHERE tipe = :tipe ORDER BY nama ASC")
    List<Kategori> getByTipe(String tipe);

    @Query("SELECT COUNT(*) FROM kategori")
    int getCount();
}
