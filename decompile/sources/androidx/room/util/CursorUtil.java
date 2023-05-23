package androidx.room.util;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.util.Log;
import java.util.Arrays;

/* loaded from: classes.dex */
public class CursorUtil {
    public static Cursor copyAndClose(Cursor c) {
        try {
            MatrixCursor matrixCursor = new MatrixCursor(c.getColumnNames(), c.getCount());
            while (c.moveToNext()) {
                Object[] row = new Object[c.getColumnCount()];
                for (int i = 0; i < c.getColumnCount(); i++) {
                    switch (c.getType(i)) {
                        case 0:
                            row[i] = null;
                            break;
                        case 1:
                            row[i] = Long.valueOf(c.getLong(i));
                            break;
                        case 2:
                            row[i] = Double.valueOf(c.getDouble(i));
                            break;
                        case 3:
                            row[i] = c.getString(i);
                            break;
                        case 4:
                            row[i] = c.getBlob(i);
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }
                matrixCursor.addRow(row);
            }
            return matrixCursor;
        } finally {
            c.close();
        }
    }

    public static int getColumnIndex(Cursor c, String name) {
        int index = c.getColumnIndex(name);
        if (index >= 0) {
            return index;
        }
        int index2 = c.getColumnIndex("`" + name + "`");
        if (index2 >= 0) {
            return index2;
        }
        return findColumnIndexBySuffix(c, name);
    }

    public static int getColumnIndexOrThrow(Cursor c, String name) {
        int index = getColumnIndex(c, name);
        if (index >= 0) {
            return index;
        }
        String availableColumns = "";
        try {
            availableColumns = Arrays.toString(c.getColumnNames());
        } catch (Exception e) {
            Log.d("RoomCursorUtil", "Cannot collect column names for debug purposes", e);
        }
        throw new IllegalArgumentException("column '" + name + "' does not exist. Available columns: " + availableColumns);
    }

    private static int findColumnIndexBySuffix(Cursor cursor, String name) {
        if (Build.VERSION.SDK_INT <= 25 && name.length() != 0) {
            String[] columnNames = cursor.getColumnNames();
            return findColumnIndexBySuffix(columnNames, name);
        }
        return -1;
    }

    static int findColumnIndexBySuffix(String[] columnNames, String name) {
        String dotSuffix = "." + name;
        String backtickSuffix = "." + name + "`";
        for (int index = 0; index < columnNames.length; index++) {
            String columnName = columnNames[index];
            if (columnName.length() >= name.length() + 2) {
                if (columnName.endsWith(dotSuffix)) {
                    return index;
                }
                if (columnName.charAt(0) == '`' && columnName.endsWith(backtickSuffix)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private CursorUtil() {
    }
}
