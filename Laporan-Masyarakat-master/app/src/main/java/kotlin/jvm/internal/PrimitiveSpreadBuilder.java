package kotlin.jvm.internal;

import androidx.exifinterface.media.ExifInterface;
import kotlin.Metadata;

/* compiled from: PrimitiveSpreadBuilders.kt */
@Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u0011\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\t\b&\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0002B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0002\u0010\u0005J\u0013\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00028\u0000¢\u0006\u0002\u0010\u0012J\b\u0010\u0003\u001a\u00020\u0004H\u0004J\u001d\u0010\u0013\u001a\u00028\u00002\u0006\u0010\u0014\u001a\u00028\u00002\u0006\u0010\u0015\u001a\u00028\u0000H\u0004¢\u0006\u0002\u0010\u0016J\u0011\u0010\u0017\u001a\u00020\u0004*\u00028\u0000H$¢\u0006\u0002\u0010\u0018R\u001a\u0010\u0006\u001a\u00020\u0004X\u0084\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\u0005R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u001e\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00018\u00000\u000bX\u0082\u0004¢\u0006\n\n\u0002\u0010\u000e\u0012\u0004\b\f\u0010\r¨\u0006\u0019"}, d2 = {"Lkotlin/jvm/internal/PrimitiveSpreadBuilder;", ExifInterface.GPS_DIRECTION_TRUE, "", "size", "", "(I)V", "position", "getPosition", "()I", "setPosition", "spreads", "", "getSpreads$annotations", "()V", "[Ljava/lang/Object;", "addSpread", "", "spreadArgument", "(Ljava/lang/Object;)V", "toArray", "values", "result", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "getSize", "(Ljava/lang/Object;)I", "kotlin-stdlib"}, k = 1, mv = {1, 6, 0}, xi = 48)
/* loaded from: classes11.dex */
public abstract class PrimitiveSpreadBuilder<T> {
    private int position;
    private final int size;
    private final T[] spreads;

    private static /* synthetic */ void getSpreads$annotations() {
    }

    protected abstract int getSize(T t);

    public PrimitiveSpreadBuilder(int size) {
        this.size = size;
        this.spreads = (T[]) new Object[size];
    }

    public final int getPosition() {
        return this.position;
    }

    public final void setPosition(int i) {
        this.position = i;
    }

    public final void addSpread(T spreadArgument) {
        Intrinsics.checkNotNullParameter(spreadArgument, "spreadArgument");
        T[] tArr = this.spreads;
        int i = this.position;
        this.position = i + 1;
        tArr[i] = spreadArgument;
    }

    public final int size() {
        int i;
        int totalLength = 0;
        int i2 = this.size - 1;
        if (i2 >= 0) {
            int i3 = 0;
            do {
                i = i3;
                i3++;
                T t = this.spreads[i];
                totalLength += t == null ? 1 : getSize(t);
            } while (i != i2);
            return totalLength;
        }
        return totalLength;
    }

    /* JADX WARN: Removed duplicated region for block: B:49:0x0037  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final T toArray(T r9, T r10) {
        /*
            r8 = this;
            java.lang.String r0 = "values"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r9, r0)
            java.lang.String r0 = "result"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r10, r0)
            r0 = 0
            r1 = 0
            int r2 = r8.size
            int r2 = r2 + (-1)
            if (r2 < 0) goto L33
            r3 = 0
            r4 = 0
        L14:
            r5 = r4
            int r4 = r4 + 1
            T[] r6 = r8.spreads
            r6 = r6[r5]
            if (r6 == 0) goto L31
            if (r1 >= r5) goto L27
            int r7 = r5 - r1
            java.lang.System.arraycopy(r9, r1, r10, r0, r7)
            int r7 = r5 - r1
            int r0 = r0 + r7
        L27:
            int r7 = r8.getSize(r6)
            java.lang.System.arraycopy(r6, r3, r10, r0, r7)
            int r0 = r0 + r7
            int r1 = r5 + 1
        L31:
            if (r5 != r2) goto L14
        L33:
            int r2 = r8.size
            if (r1 >= r2) goto L3b
            int r2 = r2 - r1
            java.lang.System.arraycopy(r9, r1, r10, r0, r2)
        L3b:
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.jvm.internal.PrimitiveSpreadBuilder.toArray(java.lang.Object, java.lang.Object):java.lang.Object");
    }
}
