package androidx.room.migration;

import androidx.sqlite.db.SupportSQLiteDatabase;

/* loaded from: classes.dex */
public interface AutoMigrationCallback {
    void onPostMigrate(SupportSQLiteDatabase db);

    /* renamed from: androidx.room.migration.AutoMigrationCallback$-CC  reason: invalid class name */
    /* loaded from: classes.dex */
    public final /* synthetic */ class CC {
        public static void $default$onPostMigrate(AutoMigrationCallback _this, SupportSQLiteDatabase db) {
        }
    }
}
