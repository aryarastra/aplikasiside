package androidx.room;

import androidx.sqlite.db.SupportSQLiteProgram;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public final class QueryInterceptorProgram implements SupportSQLiteProgram {
    private List<Object> mBindArgsCache = new ArrayList();

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void bindNull(int index) {
        saveArgsToCache(index, null);
    }

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void bindLong(int index, long value) {
        saveArgsToCache(index, Long.valueOf(value));
    }

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void bindDouble(int index, double value) {
        saveArgsToCache(index, Double.valueOf(value));
    }

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void bindString(int index, String value) {
        saveArgsToCache(index, value);
    }

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void bindBlob(int index, byte[] value) {
        saveArgsToCache(index, value);
    }

    @Override // androidx.sqlite.db.SupportSQLiteProgram
    public void clearBindings() {
        this.mBindArgsCache.clear();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
    }

    private void saveArgsToCache(int bindIndex, Object value) {
        int index = bindIndex - 1;
        if (index >= this.mBindArgsCache.size()) {
            for (int i = this.mBindArgsCache.size(); i <= index; i++) {
                this.mBindArgsCache.add(null);
            }
        }
        this.mBindArgsCache.set(index, value);
    }

    public List<Object> getBindArgs() {
        return this.mBindArgsCache;
    }
}
