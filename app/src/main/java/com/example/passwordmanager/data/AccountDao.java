package com.example.passwordmanager.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Query("SELECT * FROM Account ORDER BY id DESC")
    List<Account> getAll();

    @Query("SELECT COUNT(*) FROM Account")
    int getCount();

    @Delete
    void delete(Account account);

    @Update
    void update(Account account);

}