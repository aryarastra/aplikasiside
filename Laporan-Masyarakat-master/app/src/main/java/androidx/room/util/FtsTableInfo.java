package androidx.room.util;

import android.database.Cursor;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* loaded from: classes.dex */
public final class FtsTableInfo {
    private static final String[] FTS_OPTIONS = {"tokenize=", "compress=", "content=", "languageid=", "matchinfo=", "notindexed=", "order=", "prefix=", "uncompress="};
    public final Set<String> columns;
    public final String name;
    public final Set<String> options;

    public FtsTableInfo(String name, Set<String> columns, Set<String> options) {
        this.name = name;
        this.columns = columns;
        this.options = options;
    }

    public FtsTableInfo(String name, Set<String> columns, String createSql) {
        this.name = name;
        this.columns = columns;
        this.options = parseOptions(createSql);
    }

    public static FtsTableInfo read(SupportSQLiteDatabase database, String tableName) {
        Set<String> columns = readColumns(database, tableName);
        Set<String> options = readOptions(database, tableName);
        return new FtsTableInfo(tableName, columns, options);
    }

    private static Set<String> readColumns(SupportSQLiteDatabase database, String tableName) {
        Cursor cursor = database.query("PRAGMA table_info(`" + tableName + "`)");
        Set<String> columns = new HashSet<>();
        try {
            if (cursor.getColumnCount() > 0) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(nameIndex));
                }
            }
            return columns;
        } finally {
            cursor.close();
        }
    }

    private static Set<String> readOptions(SupportSQLiteDatabase database, String tableName) {
        String sql = "";
        Cursor cursor = database.query("SELECT * FROM sqlite_master WHERE `name` = '" + tableName + "'");
        try {
            if (cursor.moveToFirst()) {
                sql = cursor.getString(cursor.getColumnIndexOrThrow("sql"));
            }
            cursor.close();
            return parseOptions(sql);
        } catch (Throwable th) {
            cursor.close();
            throw th;
        }
    }

    static Set<String> parseOptions(String createStatement) {
        String[] strArr;
        if (createStatement.isEmpty()) {
            return new HashSet();
        }
        String argsString = createStatement.substring(createStatement.indexOf(40) + 1, createStatement.lastIndexOf(41));
        List<String> args = new ArrayList<>();
        ArrayDeque<Character> quoteStack = new ArrayDeque<>();
        int lastDelimiterIndex = -1;
        for (int i = 0; i < argsString.length(); i++) {
            char c = argsString.charAt(i);
            switch (c) {
                case '\"':
                case '\'':
                case '`':
                    if (quoteStack.isEmpty()) {
                        quoteStack.push(Character.valueOf(c));
                        break;
                    } else if (quoteStack.peek().charValue() == c) {
                        quoteStack.pop();
                        break;
                    } else {
                        break;
                    }
                case ',':
                    if (quoteStack.isEmpty()) {
                        args.add(argsString.substring(lastDelimiterIndex + 1, i).trim());
                        lastDelimiterIndex = i;
                        break;
                    } else {
                        break;
                    }
                case '[':
                    if (quoteStack.isEmpty()) {
                        quoteStack.push(Character.valueOf(c));
                        break;
                    } else {
                        break;
                    }
                case ']':
                    if (!quoteStack.isEmpty() && quoteStack.peek().charValue() == '[') {
                        quoteStack.pop();
                        break;
                    }
                    break;
            }
        }
        int i2 = lastDelimiterIndex + 1;
        args.add(argsString.substring(i2).trim());
        HashSet<String> options = new HashSet<>();
        for (String arg : args) {
            for (String validOption : FTS_OPTIONS) {
                if (arg.startsWith(validOption)) {
                    options.add(arg);
                }
            }
        }
        return options;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FtsTableInfo) {
            FtsTableInfo that = (FtsTableInfo) o;
            String str = this.name;
            if (str == null ? that.name == null : str.equals(that.name)) {
                Set<String> set = this.columns;
                if (set == null ? that.columns == null : set.equals(that.columns)) {
                    Set<String> set2 = this.options;
                    return set2 != null ? set2.equals(that.options) : that.options == null;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        String str = this.name;
        int result = str != null ? str.hashCode() : 0;
        int i = result * 31;
        Set<String> set = this.columns;
        int result2 = i + (set != null ? set.hashCode() : 0);
        int result3 = result2 * 31;
        Set<String> set2 = this.options;
        return result3 + (set2 != null ? set2.hashCode() : 0);
    }

    public String toString() {
        return "FtsTableInfo{name='" + this.name + "', columns=" + this.columns + ", options=" + this.options + '}';
    }
}
