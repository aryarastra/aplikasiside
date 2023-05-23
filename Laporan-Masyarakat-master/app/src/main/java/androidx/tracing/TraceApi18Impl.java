package androidx.tracing;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class TraceApi18Impl {
    private TraceApi18Impl() {
    }

    public static void beginSection(String label) {
        android.os.Trace.beginSection(label);
    }

    public static void endSection() {
        android.os.Trace.endSection();
    }
}
