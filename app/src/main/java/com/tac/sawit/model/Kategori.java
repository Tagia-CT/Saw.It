package com.tac.sawit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "kategori")
public class Kategori {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nama;

    @NonNull
    public String tipe;
}
