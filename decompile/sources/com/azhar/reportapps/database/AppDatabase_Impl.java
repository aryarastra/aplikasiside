package com.azhar.reportapps.database;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.azhar.reportapps.dao.DatabaseDao;
import com.azhar.reportapps.dao.DatabaseDao_Impl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes6.dex */
public final class AppDatabase_Impl extends AppDatabase {
    private volatile DatabaseDao _databaseDao;

    @Override // androidx.room.RoomDatabase
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
        SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) { // from class: com.azhar.reportapps.database.AppDatabase_Impl.1
            @Override // androidx.room.RoomOpenHelper.Delegate
            public void createAllTables(SupportSQLiteDatabase _db) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_laporan` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kategori` TEXT, `image` TEXT, `nama` TEXT, `lokasi` TEXT, `tanggal` TEXT, `isi_laporan` TEXT, `telepon` TEXT)");
                _db.execSQL(RoomMasterTable.CREATE_QUERY);
                _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9a48e83d14b62b9dfb642f0314c3b7d6')");
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void dropAllTables(SupportSQLiteDatabase _db) {
                _db.execSQL("DROP TABLE IF EXISTS `tbl_laporan`");
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int _size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(_i)).onDestructiveMigration(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected void onCreate(SupportSQLiteDatabase _db) {
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int _size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(_i)).onCreate(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onOpen(SupportSQLiteDatabase _db) {
                AppDatabase_Impl.this.mDatabase = _db;
                AppDatabase_Impl.this.internalInitInvalidationTracker(_db);
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int _size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(_i)).onOpen(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPreMigrate(SupportSQLiteDatabase _db) {
                DBUtil.dropFtsSyncTriggers(_db);
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPostMigrate(SupportSQLiteDatabase _db) {
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
                HashMap<String, TableInfo.Column> _columnsTblLaporan = new HashMap<>(8);
                _columnsTblLaporan.put("uid", new TableInfo.Column("uid", "INTEGER", true, 1, null, 1));
                _columnsTblLaporan.put("kategori", new TableInfo.Column("kategori", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("image", new TableInfo.Column("image", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("nama", new TableInfo.Column("nama", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("lokasi", new TableInfo.Column("lokasi", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("tanggal", new TableInfo.Column("tanggal", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("isi_laporan", new TableInfo.Column("isi_laporan", "TEXT", false, 0, null, 1));
                _columnsTblLaporan.put("telepon", new TableInfo.Column("telepon", "TEXT", false, 0, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysTblLaporan = new HashSet<>(0);
                HashSet<TableInfo.Index> _indicesTblLaporan = new HashSet<>(0);
                TableInfo _infoTblLaporan = new TableInfo("tbl_laporan", _columnsTblLaporan, _foreignKeysTblLaporan, _indicesTblLaporan);
                TableInfo _existingTblLaporan = TableInfo.read(_db, "tbl_laporan");
                if (!_infoTblLaporan.equals(_existingTblLaporan)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_laporan(com.azhar.reportapps.model.ModelDatabase).\n Expected:\n" + _infoTblLaporan + "\n Found:\n" + _existingTblLaporan);
                }
                return new RoomOpenHelper.ValidationResult(true, null);
            }
        }, "9a48e83d14b62b9dfb642f0314c3b7d6", "0cd7a62e21a9e09ee166696d88298914");
        SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context).name(configuration.name).callback(_openCallback).build();
        SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
        return _helper;
    }

    @Override // androidx.room.RoomDatabase
    protected InvalidationTracker createInvalidationTracker() {
        HashMap<String, String> _shadowTablesMap = new HashMap<>(0);
        HashMap<String, Set<String>> _viewTables = new HashMap<>(0);
        return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "tbl_laporan");
    }

    @Override // androidx.room.RoomDatabase
    public void clearAllTables() {
        super.assertNotMainThread();
        SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
        try {
            super.beginTransaction();
            _db.execSQL("DELETE FROM `tbl_laporan`");
            super.setTransactionSuccessful();
        } finally {
            super.endTransaction();
            _db.query("PRAGMA wal_checkpoint(FULL)").close();
            if (!_db.inTransaction()) {
                _db.execSQL("VACUUM");
            }
        }
    }

    @Override // androidx.room.RoomDatabase
    protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
        HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<>();
        _typeConvertersMap.put(DatabaseDao.class, DatabaseDao_Impl.getRequiredConverters());
        return _typeConvertersMap;
    }

    @Override // com.azhar.reportapps.database.AppDatabase
    public DatabaseDao databaseDao() {
        DatabaseDao databaseDao;
        if (this._databaseDao != null) {
            return this._databaseDao;
        }
        synchronized (this) {
            if (this._databaseDao == null) {
                this._databaseDao = new DatabaseDao_Impl(this);
            }
            databaseDao = this._databaseDao;
        }
        return databaseDao;
    }
}
