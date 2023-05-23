package com.azhar.reportapps.database;

import android.content.Context;
import androidx.room.Room;

/* loaded from: classes6.dex */
public class DatabaseClient {
    private static DatabaseClient dcInstance;
    AppDatabase appDatabase;

    private DatabaseClient(Context context) {
        this.appDatabase = (AppDatabase) Room.databaseBuilder(context, AppDatabase.class, "laporan_db").fallbackToDestructiveMigration().build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        DatabaseClient databaseClient;
        synchronized (DatabaseClient.class) {
            if (dcInstance == null) {
                dcInstance = new DatabaseClient(context);
            }
            databaseClient = dcInstance;
        }
        return databaseClient;
    }

    public AppDatabase getAppDatabase() {
        return this.appDatabase;
    }
}
