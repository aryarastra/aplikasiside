package com.azhar.reportapps.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.azhar.reportapps.model.ModelDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/* loaded from: classes8.dex */
public final class DatabaseDao_Impl implements DatabaseDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter<ModelDatabase> __insertionAdapterOfModelDatabase;
    private final SharedSQLiteStatement __preparedStmtOfDeleteAllReport;
    private final SharedSQLiteStatement __preparedStmtOfDeleteSingleReport;

    public DatabaseDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfModelDatabase = new EntityInsertionAdapter<ModelDatabase>(__db) { // from class: com.azhar.reportapps.dao.DatabaseDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_laporan` (`uid`,`kategori`,`image`,`nama`,`lokasi`,`tanggal`,`isi_laporan`,`telepon`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, ModelDatabase value) {
                stmt.bindLong(1, value.uid);
                if (value.kategori == null) {
                    stmt.bindNull(2);
                } else {
                    stmt.bindString(2, value.kategori);
                }
                if (value.image == null) {
                    stmt.bindNull(3);
                } else {
                    stmt.bindString(3, value.image);
                }
                if (value.nama == null) {
                    stmt.bindNull(4);
                } else {
                    stmt.bindString(4, value.nama);
                }
                if (value.lokasi == null) {
                    stmt.bindNull(5);
                } else {
                    stmt.bindString(5, value.lokasi);
                }
                if (value.tanggal == null) {
                    stmt.bindNull(6);
                } else {
                    stmt.bindString(6, value.tanggal);
                }
                if (value.isi_laporan == null) {
                    stmt.bindNull(7);
                } else {
                    stmt.bindString(7, value.isi_laporan);
                }
                if (value.telepon == null) {
                    stmt.bindNull(8);
                } else {
                    stmt.bindString(8, value.telepon);
                }
            }
        };
        this.__preparedStmtOfDeleteAllReport = new SharedSQLiteStatement(__db) { // from class: com.azhar.reportapps.dao.DatabaseDao_Impl.2
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM tbl_laporan";
            }
        };
        this.__preparedStmtOfDeleteSingleReport = new SharedSQLiteStatement(__db) { // from class: com.azhar.reportapps.dao.DatabaseDao_Impl.3
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM tbl_laporan WHERE uid= ?";
            }
        };
    }

    @Override // com.azhar.reportapps.dao.DatabaseDao
    public void insertData(final ModelDatabase... modelDatabases) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfModelDatabase.insert(modelDatabases);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.azhar.reportapps.dao.DatabaseDao
    public void deleteAllReport() {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfDeleteAllReport.acquire();
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteAllReport.release(_stmt);
        }
    }

    @Override // com.azhar.reportapps.dao.DatabaseDao
    public void deleteSingleReport(final int uid) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfDeleteSingleReport.acquire();
        _stmt.bindLong(1, uid);
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteSingleReport.release(_stmt);
        }
    }

    @Override // com.azhar.reportapps.dao.DatabaseDao
    public LiveData<List<ModelDatabase>> getAllReport() {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT * FROM tbl_laporan", 0);
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"tbl_laporan"}, false, new Callable<List<ModelDatabase>>() { // from class: com.azhar.reportapps.dao.DatabaseDao_Impl.4
            @Override // java.util.concurrent.Callable
            public List<ModelDatabase> call() throws Exception {
                Cursor _cursor = DBUtil.query(DatabaseDao_Impl.this.__db, _statement, false, null);
                try {
                    int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
                    int _cursorIndexOfKategori = CursorUtil.getColumnIndexOrThrow(_cursor, "kategori");
                    int _cursorIndexOfImage = CursorUtil.getColumnIndexOrThrow(_cursor, "image");
                    int _cursorIndexOfNama = CursorUtil.getColumnIndexOrThrow(_cursor, "nama");
                    int _cursorIndexOfLokasi = CursorUtil.getColumnIndexOrThrow(_cursor, "lokasi");
                    int _cursorIndexOfTanggal = CursorUtil.getColumnIndexOrThrow(_cursor, "tanggal");
                    int _cursorIndexOfIsiLaporan = CursorUtil.getColumnIndexOrThrow(_cursor, "isi_laporan");
                    int _cursorIndexOfTelepon = CursorUtil.getColumnIndexOrThrow(_cursor, "telepon");
                    List<ModelDatabase> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        ModelDatabase _item = new ModelDatabase();
                        _item.uid = _cursor.getInt(_cursorIndexOfUid);
                        if (_cursor.isNull(_cursorIndexOfKategori)) {
                            _item.kategori = null;
                        } else {
                            _item.kategori = _cursor.getString(_cursorIndexOfKategori);
                        }
                        if (_cursor.isNull(_cursorIndexOfImage)) {
                            _item.image = null;
                        } else {
                            _item.image = _cursor.getString(_cursorIndexOfImage);
                        }
                        if (_cursor.isNull(_cursorIndexOfNama)) {
                            _item.nama = null;
                        } else {
                            _item.nama = _cursor.getString(_cursorIndexOfNama);
                        }
                        if (_cursor.isNull(_cursorIndexOfLokasi)) {
                            _item.lokasi = null;
                        } else {
                            _item.lokasi = _cursor.getString(_cursorIndexOfLokasi);
                        }
                        if (_cursor.isNull(_cursorIndexOfTanggal)) {
                            _item.tanggal = null;
                        } else {
                            _item.tanggal = _cursor.getString(_cursorIndexOfTanggal);
                        }
                        if (_cursor.isNull(_cursorIndexOfIsiLaporan)) {
                            _item.isi_laporan = null;
                        } else {
                            _item.isi_laporan = _cursor.getString(_cursorIndexOfIsiLaporan);
                        }
                        if (_cursor.isNull(_cursorIndexOfTelepon)) {
                            _item.telepon = null;
                        } else {
                            _item.telepon = _cursor.getString(_cursorIndexOfTelepon);
                        }
                        _result.add(_item);
                    }
                    return _result;
                } finally {
                    _cursor.close();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
