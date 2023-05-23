package androidx.room;

import androidx.sqlite.db.SupportSQLiteOpenHelper;

/* loaded from: classes.dex */
interface DelegatingOpenHelper {
    SupportSQLiteOpenHelper getDelegate();
}
