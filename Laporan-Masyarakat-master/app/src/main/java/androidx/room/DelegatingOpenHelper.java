package androidx.room;

import androidx.sqlite.db.SupportSQLiteOpenHelper;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public interface DelegatingOpenHelper {
    SupportSQLiteOpenHelper getDelegate();
}
