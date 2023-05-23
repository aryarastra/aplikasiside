package com.azhar.reportapps.database;

import androidx.room.RoomDatabase;
import com.azhar.reportapps.dao.DatabaseDao;

/* loaded from: classes6.dex */
public abstract class AppDatabase extends RoomDatabase {
    public abstract DatabaseDao databaseDao();
}
