package com.teraime.poppyfield.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;

@Dao
public interface VariableDAO {

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert()
    void insert(VariableTable vTable);

    @Query("DELETE FROM variabler")
    void deleteAll();

    @Query("SELECT * FROM variabler ORDER BY timestamp ASC")
    LiveData<List<VariableTable>> getTimeOrderedList();

    @Query("DELETE FROM variabler where year=='H'")
    void deleteAllHistorical();

    @RawQuery
    int deleteSomeHistorical(SimpleSQLiteQuery query);


    @RawQuery
    List<VariableTable> latestMatch(SimpleSQLiteQuery query);

}
