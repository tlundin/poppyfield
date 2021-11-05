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


    @Query("SELECT * FROM variabler WHERE L1== NULL AND L2 == NULL AND L3 == NULL AND L4 == NULL AND L5 == NULL AND L6 == NULL AND L7 == NULL AND L8 == NULL AND L9 == NULL AND L10 == NULL")
    LiveData<List<VariableTable>> getAllGlobals();

    @RawQuery
    int deleteSomeHistorical(SimpleSQLiteQuery query);

    @RawQuery(observedEntities = VariableTable.class)
    LiveData<List<VariableTable>> rawVarQuery(SimpleSQLiteQuery query);


}
