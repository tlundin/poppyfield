package com.teraime.poppyfield.room;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {VariableTable.class},
        version = 1,
        exportSchema = true)

public abstract class FieldPadRoomDatabase extends RoomDatabase {

    public abstract VariableDAO variableDao();

    private static volatile FieldPadRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static FieldPadRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FieldPadRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FieldPadRoomDatabase.class, "fieldpad_database")
                            .build(); //.allowMainThreadQueries()
                }
            }
        }
        return INSTANCE;
    }
}