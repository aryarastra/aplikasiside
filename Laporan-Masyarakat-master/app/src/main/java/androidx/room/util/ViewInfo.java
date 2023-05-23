package androidx.room.util;

import android.database.Cursor;
import androidx.sqlite.db.SupportSQLiteDatabase;

/* loaded from: classes.dex */
public final class ViewInfo {
    public final String name;
    public final String sql;

    public ViewInfo(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ViewInfo) {
            ViewInfo viewInfo = (ViewInfo) o;
            String str = this.name;
            if (str == null ? viewInfo.name == null : str.equals(viewInfo.name)) {
                String str2 = this.sql;
                if (str2 != null) {
                    if (str2.equals(viewInfo.sql)) {
                        return true;
                    }
                } else if (viewInfo.sql == null) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        String str = this.name;
        int result = str != null ? str.hashCode() : 0;
        int i = result * 31;
        String str2 = this.sql;
        int result2 = i + (str2 != null ? str2.hashCode() : 0);
        return result2;
    }

    public String toString() {
        return "ViewInfo{name='" + this.name + "', sql='" + this.sql + "'}";
    }

    public static ViewInfo read(SupportSQLiteDatabase database, String viewName) {
        Cursor cursor = database.query("SELECT name, sql FROM sqlite_master WHERE type = 'view' AND name = '" + viewName + "'");
        try {
            if (cursor.moveToFirst()) {
                return new ViewInfo(cursor.getString(0), cursor.getString(1));
            }
            return new ViewInfo(viewName, null);
        } finally {
            cursor.close();
        }
    }
}
