package com.example.passwordmanager.data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Account.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static volatile AppDatabase INSTANCE;

    public abstract AccountDao accountDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        Log.d(TAG, "Creating database instance");
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                        AppDatabase.class, "password-manager-db")
                                .allowMainThreadQueries() // Temporary for debugging
                                .fallbackToDestructiveMigration()
                                .build();
                        Log.d(TAG, "Database instance created successfully");
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating database", e);
                        throw new RuntimeException("Error creating database: " + e.getMessage(), e);
                    }
                }
            }
        }
        return INSTANCE;
    }
}