package androidx.room;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.util.Log;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/* loaded from: classes.dex */
public class InvalidationTracker {
    private static final String CREATE_TRACKING_TABLE_SQL = "CREATE TEMP TABLE room_table_modification_log(table_id INTEGER PRIMARY KEY, invalidated INTEGER NOT NULL DEFAULT 0)";
    private static final String INVALIDATED_COLUMN_NAME = "invalidated";
    static final String RESET_UPDATED_TABLES_SQL = "UPDATE room_table_modification_log SET invalidated = 0 WHERE invalidated = 1 ";
    static final String SELECT_UPDATED_TABLES_SQL = "SELECT * FROM room_table_modification_log WHERE invalidated = 1;";
    private static final String TABLE_ID_COLUMN_NAME = "table_id";
    private static final String[] TRIGGERS = {"UPDATE", "DELETE", "INSERT"};
    private static final String UPDATE_TABLE_NAME = "room_table_modification_log";
    AutoCloser mAutoCloser;
    volatile SupportSQLiteStatement mCleanupStatement;
    final RoomDatabase mDatabase;
    private volatile boolean mInitialized;
    private final InvalidationLiveDataContainer mInvalidationLiveDataContainer;
    private MultiInstanceInvalidationClient mMultiInstanceInvalidationClient;
    private ObservedTableTracker mObservedTableTracker;
    final SafeIterableMap<Observer, ObserverWrapper> mObserverMap;
    AtomicBoolean mPendingRefresh;
    Runnable mRefreshRunnable;
    final HashMap<String, Integer> mTableIdLookup;
    final String[] mTableNames;
    private Map<String, Set<String>> mViewTables;

    public InvalidationTracker(RoomDatabase database, String... tableNames) {
        this(database, new HashMap(), Collections.emptyMap(), tableNames);
    }

    public InvalidationTracker(RoomDatabase database, Map<String, String> shadowTablesMap, Map<String, Set<String>> viewTables, String... tableNames) {
        this.mAutoCloser = null;
        this.mPendingRefresh = new AtomicBoolean(false);
        this.mInitialized = false;
        this.mObserverMap = new SafeIterableMap<>();
        this.mRefreshRunnable = new Runnable() { // from class: androidx.room.InvalidationTracker.1
            /* JADX WARN: Code restructure failed: missing block: B:178:0x0093, code lost:
                if (androidx.room.InvalidationTracker.this.mAutoCloser != null) goto L33;
             */
            /* JADX WARN: Code restructure failed: missing block: B:179:0x0095, code lost:
                androidx.room.InvalidationTracker.this.mAutoCloser.decrementCountAndScheduleClose();
             */
            /* JADX WARN: Code restructure failed: missing block: B:187:0x00b1, code lost:
                if (androidx.room.InvalidationTracker.this.mAutoCloser == null) goto L34;
             */
            /* JADX WARN: Code restructure failed: missing block: B:189:0x00b4, code lost:
                if (r1 == null) goto L53;
             */
            /* JADX WARN: Code restructure failed: missing block: B:191:0x00ba, code lost:
                if (r1.isEmpty() != false) goto L52;
             */
            /* JADX WARN: Code restructure failed: missing block: B:192:0x00bc, code lost:
                r2 = androidx.room.InvalidationTracker.this.mObserverMap;
             */
            /* JADX WARN: Code restructure failed: missing block: B:193:0x00c0, code lost:
                monitor-enter(r2);
             */
            /* JADX WARN: Code restructure failed: missing block: B:194:0x00c1, code lost:
                r3 = androidx.room.InvalidationTracker.this.mObserverMap.iterator();
             */
            /* JADX WARN: Code restructure failed: missing block: B:196:0x00cd, code lost:
                if (r3.hasNext() == false) goto L44;
             */
            /* JADX WARN: Code restructure failed: missing block: B:197:0x00cf, code lost:
                r4 = r3.next();
                r4.getValue().notifyByTableInvalidStatus(r1);
             */
            /* JADX WARN: Code restructure failed: missing block: B:198:0x00df, code lost:
                monitor-exit(r2);
             */
            /* JADX WARN: Code restructure failed: missing block: B:203:0x00e4, code lost:
                return;
             */
            /* JADX WARN: Code restructure failed: missing block: B:217:?, code lost:
                return;
             */
            /* JADX WARN: Code restructure failed: missing block: B:218:?, code lost:
                return;
             */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public void run() {
                /*
                    Method dump skipped, instructions count: 246
                    To view this dump change 'Code comments level' option to 'DEBUG'
                */
                throw new UnsupportedOperationException("Method not decompiled: androidx.room.InvalidationTracker.AnonymousClass1.run():void");
            }

            private Set<Integer> checkUpdatedTable() {
                HashSet<Integer> invalidatedTableIds = new HashSet<>();
                Cursor cursor = InvalidationTracker.this.mDatabase.query(new SimpleSQLiteQuery(InvalidationTracker.SELECT_UPDATED_TABLES_SQL));
                while (cursor.moveToNext()) {
                    try {
                        int tableId = cursor.getInt(0);
                        invalidatedTableIds.add(Integer.valueOf(tableId));
                    } catch (Throwable th) {
                        cursor.close();
                        throw th;
                    }
                }
                cursor.close();
                if (!invalidatedTableIds.isEmpty()) {
                    InvalidationTracker.this.mCleanupStatement.executeUpdateDelete();
                }
                return invalidatedTableIds;
            }
        };
        this.mDatabase = database;
        this.mObservedTableTracker = new ObservedTableTracker(tableNames.length);
        this.mTableIdLookup = new HashMap<>();
        this.mViewTables = viewTables;
        this.mInvalidationLiveDataContainer = new InvalidationLiveDataContainer(database);
        int size = tableNames.length;
        this.mTableNames = new String[size];
        for (int id = 0; id < size; id++) {
            String tableName = tableNames[id].toLowerCase(Locale.US);
            this.mTableIdLookup.put(tableName, Integer.valueOf(id));
            String shadowTableName = shadowTablesMap.get(tableNames[id]);
            if (shadowTableName != null) {
                this.mTableNames[id] = shadowTableName.toLowerCase(Locale.US);
            } else {
                this.mTableNames[id] = tableName;
            }
        }
        for (Map.Entry<String, String> shadowTableEntry : shadowTablesMap.entrySet()) {
            String shadowTableName2 = shadowTableEntry.getValue().toLowerCase(Locale.US);
            if (this.mTableIdLookup.containsKey(shadowTableName2)) {
                String tableName2 = shadowTableEntry.getKey().toLowerCase(Locale.US);
                HashMap<String, Integer> hashMap = this.mTableIdLookup;
                hashMap.put(tableName2, hashMap.get(shadowTableName2));
            }
        }
    }

    public void setAutoCloser(AutoCloser autoCloser) {
        this.mAutoCloser = autoCloser;
        autoCloser.setAutoCloseCallback(new Runnable() { // from class: androidx.room.InvalidationTracker$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                InvalidationTracker.this.onAutoCloseCallback();
            }
        });
    }

    public void internalInit(SupportSQLiteDatabase database) {
        synchronized (this) {
            if (this.mInitialized) {
                Log.e("ROOM", "Invalidation tracker is initialized twice :/.");
                return;
            }
            database.execSQL("PRAGMA temp_store = MEMORY;");
            database.execSQL("PRAGMA recursive_triggers='ON';");
            database.execSQL(CREATE_TRACKING_TABLE_SQL);
            syncTriggers(database);
            this.mCleanupStatement = database.compileStatement(RESET_UPDATED_TABLES_SQL);
            this.mInitialized = true;
        }
    }

    public void onAutoCloseCallback() {
        synchronized (this) {
            this.mInitialized = false;
            this.mObservedTableTracker.resetTriggerState();
        }
    }

    public void startMultiInstanceInvalidation(Context context, String name) {
        this.mMultiInstanceInvalidationClient = new MultiInstanceInvalidationClient(context, name, this, this.mDatabase.getQueryExecutor());
    }

    public void stopMultiInstanceInvalidation() {
        MultiInstanceInvalidationClient multiInstanceInvalidationClient = this.mMultiInstanceInvalidationClient;
        if (multiInstanceInvalidationClient != null) {
            multiInstanceInvalidationClient.stop();
            this.mMultiInstanceInvalidationClient = null;
        }
    }

    private static void appendTriggerName(StringBuilder builder, String tableName, String triggerType) {
        builder.append("`");
        builder.append("room_table_modification_trigger_");
        builder.append(tableName);
        builder.append("_");
        builder.append(triggerType);
        builder.append("`");
    }

    private void stopTrackingTable(SupportSQLiteDatabase writableDb, int tableId) {
        String[] strArr;
        String tableName = this.mTableNames[tableId];
        StringBuilder stringBuilder = new StringBuilder();
        for (String trigger : TRIGGERS) {
            stringBuilder.setLength(0);
            stringBuilder.append("DROP TRIGGER IF EXISTS ");
            appendTriggerName(stringBuilder, tableName, trigger);
            writableDb.execSQL(stringBuilder.toString());
        }
    }

    private void startTrackingTable(SupportSQLiteDatabase writableDb, int tableId) {
        String[] strArr;
        writableDb.execSQL("INSERT OR IGNORE INTO room_table_modification_log VALUES(" + tableId + ", 0)");
        String tableName = this.mTableNames[tableId];
        StringBuilder stringBuilder = new StringBuilder();
        for (String trigger : TRIGGERS) {
            stringBuilder.setLength(0);
            stringBuilder.append("CREATE TEMP TRIGGER IF NOT EXISTS ");
            appendTriggerName(stringBuilder, tableName, trigger);
            stringBuilder.append(" AFTER ");
            stringBuilder.append(trigger);
            stringBuilder.append(" ON `");
            stringBuilder.append(tableName);
            stringBuilder.append("` BEGIN UPDATE ");
            stringBuilder.append(UPDATE_TABLE_NAME);
            stringBuilder.append(" SET ");
            stringBuilder.append(INVALIDATED_COLUMN_NAME);
            stringBuilder.append(" = 1");
            stringBuilder.append(" WHERE ");
            stringBuilder.append(TABLE_ID_COLUMN_NAME);
            stringBuilder.append(" = ");
            stringBuilder.append(tableId);
            stringBuilder.append(" AND ");
            stringBuilder.append(INVALIDATED_COLUMN_NAME);
            stringBuilder.append(" = 0");
            stringBuilder.append("; END");
            writableDb.execSQL(stringBuilder.toString());
        }
    }

    public void addObserver(Observer observer) {
        ObserverWrapper currentObserver;
        String[] tableNames = resolveViews(observer.mTables);
        int[] tableIds = new int[tableNames.length];
        int size = tableNames.length;
        for (int i = 0; i < size; i++) {
            Integer tableId = this.mTableIdLookup.get(tableNames[i].toLowerCase(Locale.US));
            if (tableId == null) {
                throw new IllegalArgumentException("There is no table with name " + tableNames[i]);
            }
            tableIds[i] = tableId.intValue();
        }
        ObserverWrapper wrapper = new ObserverWrapper(observer, tableIds, tableNames);
        synchronized (this.mObserverMap) {
            currentObserver = this.mObserverMap.putIfAbsent(observer, wrapper);
        }
        if (currentObserver == null && this.mObservedTableTracker.onAdded(tableIds)) {
            syncTriggers();
        }
    }

    private String[] validateAndResolveTableNames(String[] tableNames) {
        String[] resolved = resolveViews(tableNames);
        for (String tableName : resolved) {
            if (!this.mTableIdLookup.containsKey(tableName.toLowerCase(Locale.US))) {
                throw new IllegalArgumentException("There is no table with name " + tableName);
            }
        }
        return resolved;
    }

    private String[] resolveViews(String[] names) {
        Set<String> tables = new HashSet<>();
        for (String name : names) {
            String lowercase = name.toLowerCase(Locale.US);
            if (this.mViewTables.containsKey(lowercase)) {
                tables.addAll(this.mViewTables.get(lowercase));
            } else {
                tables.add(name);
            }
        }
        return (String[]) tables.toArray(new String[tables.size()]);
    }

    private static void beginTransactionInternal(SupportSQLiteDatabase database) {
        if (Build.VERSION.SDK_INT >= 16 && database.isWriteAheadLoggingEnabled()) {
            database.beginTransactionNonExclusive();
        } else {
            database.beginTransaction();
        }
    }

    public void addWeakObserver(Observer observer) {
        addObserver(new WeakObserver(this, observer));
    }

    public void removeObserver(final Observer observer) {
        ObserverWrapper wrapper;
        synchronized (this.mObserverMap) {
            wrapper = this.mObserverMap.remove(observer);
        }
        if (wrapper != null && this.mObservedTableTracker.onRemoved(wrapper.mTableIds)) {
            syncTriggers();
        }
    }

    boolean ensureInitialization() {
        if (this.mDatabase.isOpen()) {
            if (!this.mInitialized) {
                this.mDatabase.getOpenHelper().getWritableDatabase();
            }
            if (!this.mInitialized) {
                Log.e("ROOM", "database is not initialized even though it is open");
                return false;
            }
            return true;
        }
        return false;
    }

    public void refreshVersionsAsync() {
        if (this.mPendingRefresh.compareAndSet(false, true)) {
            AutoCloser autoCloser = this.mAutoCloser;
            if (autoCloser != null) {
                autoCloser.incrementCountAndEnsureDbIsOpen();
            }
            this.mDatabase.getQueryExecutor().execute(this.mRefreshRunnable);
        }
    }

    public void refreshVersionsSync() {
        AutoCloser autoCloser = this.mAutoCloser;
        if (autoCloser != null) {
            autoCloser.incrementCountAndEnsureDbIsOpen();
        }
        syncTriggers();
        this.mRefreshRunnable.run();
    }

    public void notifyObserversByTableNames(String... tables) {
        synchronized (this.mObserverMap) {
            Iterator<Map.Entry<Observer, ObserverWrapper>> it = this.mObserverMap.iterator();
            while (it.hasNext()) {
                Map.Entry<Observer, ObserverWrapper> entry = it.next();
                if (!entry.getKey().isRemote()) {
                    entry.getValue().notifyByTableNames(tables);
                }
            }
        }
    }

    public void syncTriggers(SupportSQLiteDatabase database) {
        if (database.inTransaction()) {
            return;
        }
        while (true) {
            try {
                Lock closeLock = this.mDatabase.getCloseLock();
                closeLock.lock();
                try {
                    int[] tablesToSync = this.mObservedTableTracker.getTablesToSync();
                    if (tablesToSync == null) {
                        return;
                    }
                    int limit = tablesToSync.length;
                    beginTransactionInternal(database);
                    for (int tableId = 0; tableId < limit; tableId++) {
                        switch (tablesToSync[tableId]) {
                            case 1:
                                startTrackingTable(database, tableId);
                                break;
                            case 2:
                                stopTrackingTable(database, tableId);
                                break;
                        }
                    }
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    this.mObservedTableTracker.onSyncCompleted();
                } finally {
                    closeLock.unlock();
                }
            } catch (SQLiteException | IllegalStateException exception) {
                Log.e("ROOM", "Cannot run invalidation tracker. Is the db closed?", exception);
                return;
            }
        }
    }

    void syncTriggers() {
        if (!this.mDatabase.isOpen()) {
            return;
        }
        syncTriggers(this.mDatabase.getOpenHelper().getWritableDatabase());
    }

    @Deprecated
    public <T> LiveData<T> createLiveData(String[] tableNames, Callable<T> computeFunction) {
        return createLiveData(tableNames, false, computeFunction);
    }

    public <T> LiveData<T> createLiveData(String[] tableNames, boolean inTransaction, Callable<T> computeFunction) {
        return this.mInvalidationLiveDataContainer.create(validateAndResolveTableNames(tableNames), inTransaction, computeFunction);
    }

    /* loaded from: classes.dex */
    public static class ObserverWrapper {
        final Observer mObserver;
        private final Set<String> mSingleTableSet;
        final int[] mTableIds;
        private final String[] mTableNames;

        ObserverWrapper(Observer observer, int[] tableIds, String[] tableNames) {
            this.mObserver = observer;
            this.mTableIds = tableIds;
            this.mTableNames = tableNames;
            if (tableIds.length == 1) {
                HashSet<String> set = new HashSet<>();
                set.add(tableNames[0]);
                this.mSingleTableSet = Collections.unmodifiableSet(set);
                return;
            }
            this.mSingleTableSet = null;
        }

        void notifyByTableInvalidStatus(Set<Integer> invalidatedTablesIds) {
            Set<String> invalidatedTables = null;
            int size = this.mTableIds.length;
            for (int index = 0; index < size; index++) {
                int tableId = this.mTableIds[index];
                if (invalidatedTablesIds.contains(Integer.valueOf(tableId))) {
                    if (size == 1) {
                        invalidatedTables = this.mSingleTableSet;
                    } else {
                        if (invalidatedTables == null) {
                            invalidatedTables = new HashSet(size);
                        }
                        invalidatedTables.add(this.mTableNames[index]);
                    }
                }
            }
            if (invalidatedTables != null) {
                this.mObserver.onInvalidated(invalidatedTables);
            }
        }

        void notifyByTableNames(String[] tables) {
            Set<String> invalidatedTables = null;
            if (this.mTableNames.length == 1) {
                int length = tables.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String table = tables[i];
                    if (!table.equalsIgnoreCase(this.mTableNames[0])) {
                        i++;
                    } else {
                        invalidatedTables = this.mSingleTableSet;
                        break;
                    }
                }
            } else {
                HashSet<String> set = new HashSet<>();
                for (String table2 : tables) {
                    String[] strArr = this.mTableNames;
                    int length2 = strArr.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 < length2) {
                            String ourTable = strArr[i2];
                            if (!ourTable.equalsIgnoreCase(table2)) {
                                i2++;
                            } else {
                                set.add(ourTable);
                                break;
                            }
                        }
                    }
                }
                if (set.size() > 0) {
                    invalidatedTables = set;
                }
            }
            if (invalidatedTables != null) {
                this.mObserver.onInvalidated(invalidatedTables);
            }
        }
    }

    /* loaded from: classes.dex */
    public static abstract class Observer {
        final String[] mTables;

        public abstract void onInvalidated(Set<String> tables);

        protected Observer(String firstTable, String... rest) {
            String[] strArr = (String[]) Arrays.copyOf(rest, rest.length + 1);
            this.mTables = strArr;
            strArr[rest.length] = firstTable;
        }

        public Observer(String[] tables) {
            this.mTables = (String[]) Arrays.copyOf(tables, tables.length);
        }

        boolean isRemote() {
            return false;
        }
    }

    /* loaded from: classes.dex */
    public static class ObservedTableTracker {
        static final int ADD = 1;
        static final int NO_OP = 0;
        static final int REMOVE = 2;
        boolean mNeedsSync;
        boolean mPendingSync;
        final long[] mTableObservers;
        final int[] mTriggerStateChanges;
        final boolean[] mTriggerStates;

        ObservedTableTracker(int tableCount) {
            long[] jArr = new long[tableCount];
            this.mTableObservers = jArr;
            boolean[] zArr = new boolean[tableCount];
            this.mTriggerStates = zArr;
            this.mTriggerStateChanges = new int[tableCount];
            Arrays.fill(jArr, 0L);
            Arrays.fill(zArr, false);
        }

        boolean onAdded(int... tableIds) {
            boolean needTriggerSync = false;
            synchronized (this) {
                for (int tableId : tableIds) {
                    long[] jArr = this.mTableObservers;
                    long prevObserverCount = jArr[tableId];
                    jArr[tableId] = 1 + prevObserverCount;
                    if (prevObserverCount == 0) {
                        this.mNeedsSync = true;
                        needTriggerSync = true;
                    }
                }
            }
            return needTriggerSync;
        }

        boolean onRemoved(int... tableIds) {
            boolean needTriggerSync = false;
            synchronized (this) {
                for (int tableId : tableIds) {
                    long[] jArr = this.mTableObservers;
                    long prevObserverCount = jArr[tableId];
                    jArr[tableId] = prevObserverCount - 1;
                    if (prevObserverCount == 1) {
                        this.mNeedsSync = true;
                        needTriggerSync = true;
                    }
                }
            }
            return needTriggerSync;
        }

        void resetTriggerState() {
            synchronized (this) {
                Arrays.fill(this.mTriggerStates, false);
                this.mNeedsSync = true;
            }
        }

        int[] getTablesToSync() {
            synchronized (this) {
                if (this.mNeedsSync && !this.mPendingSync) {
                    int tableCount = this.mTableObservers.length;
                    int i = 0;
                    while (true) {
                        int i2 = 1;
                        if (i < tableCount) {
                            boolean newState = this.mTableObservers[i] > 0;
                            boolean[] zArr = this.mTriggerStates;
                            if (newState != zArr[i]) {
                                int[] iArr = this.mTriggerStateChanges;
                                if (!newState) {
                                    i2 = 2;
                                }
                                iArr[i] = i2;
                            } else {
                                this.mTriggerStateChanges[i] = 0;
                            }
                            zArr[i] = newState;
                            i++;
                        } else {
                            this.mPendingSync = true;
                            this.mNeedsSync = false;
                            return this.mTriggerStateChanges;
                        }
                    }
                }
                return null;
            }
        }

        void onSyncCompleted() {
            synchronized (this) {
                this.mPendingSync = false;
            }
        }
    }

    /* loaded from: classes.dex */
    public static class WeakObserver extends Observer {
        final WeakReference<Observer> mDelegateRef;
        final InvalidationTracker mTracker;

        WeakObserver(InvalidationTracker tracker, Observer delegate) {
            super(delegate.mTables);
            this.mTracker = tracker;
            this.mDelegateRef = new WeakReference<>(delegate);
        }

        @Override // androidx.room.InvalidationTracker.Observer
        public void onInvalidated(Set<String> tables) {
            Observer observer = this.mDelegateRef.get();
            if (observer == null) {
                this.mTracker.removeObserver(this);
            } else {
                observer.onInvalidated(tables);
            }
        }
    }
}
