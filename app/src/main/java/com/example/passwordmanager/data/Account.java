package com.example.passwordmanager.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String website;
    public String username;
    public String password;

    // Constructor kosong yang diperlukan Room
    public Account() {}

    // Constructor dengan parameter - ditandai @Ignore agar Room tidak bingung
    @Ignore
    public Account(String title, String website, String username, String password) {
        this.title = title;
        this.website = website;
        this.username = username;
        this.password = password;
    }
}