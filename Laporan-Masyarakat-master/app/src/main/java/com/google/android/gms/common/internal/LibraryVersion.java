package com.google.android.gms.common.internal;

import java.util.concurrent.ConcurrentHashMap;

/* compiled from: com.google.android.gms:play-services-basement@@17.5.0 */
/* loaded from: classes.dex */
public class LibraryVersion {
    private static final GmsLogger zza = new GmsLogger("LibraryVersion", "");
    private static LibraryVersion zzb = new LibraryVersion();
    private ConcurrentHashMap<String, String> zzc = new ConcurrentHashMap<>();

    public static LibraryVersion getInstance() {
        return zzb;
    }

    protected LibraryVersion() {
    }

    /* JADX WARN: Removed duplicated region for block: B:120:0x00bb  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.String getVersion(java.lang.String r10) {
        /*
            r9 = this;
            java.lang.String r0 = "Failed to get app version for libraryName: "
            java.lang.String r1 = "LibraryVersion"
            java.lang.String r2 = "Please provide a valid libraryName"
            com.google.android.gms.common.internal.Preconditions.checkNotEmpty(r10, r2)
            java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> r2 = r9.zzc
            boolean r2 = r2.containsKey(r10)
            if (r2 == 0) goto L1a
            java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> r0 = r9.zzc
            java.lang.Object r10 = r0.get(r10)
            java.lang.String r10 = (java.lang.String) r10
            return r10
        L1a:
            java.util.Properties r2 = new java.util.Properties
            r2.<init>()
            r3 = 0
            java.lang.String r4 = "/%s.properties"
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch: java.lang.Throwable -> L94 java.io.IOException -> L96
            r6 = 0
            r5[r6] = r10     // Catch: java.lang.Throwable -> L94 java.io.IOException -> L96
            java.lang.String r4 = java.lang.String.format(r4, r5)     // Catch: java.lang.Throwable -> L94 java.io.IOException -> L96
            java.lang.Class<com.google.android.gms.common.internal.LibraryVersion> r5 = com.google.android.gms.common.internal.LibraryVersion.class
            java.io.InputStream r4 = r5.getResourceAsStream(r4)     // Catch: java.lang.Throwable -> L94 java.io.IOException -> L96
            if (r4 == 0) goto L6d
            r2.load(r4)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            java.lang.String r5 = "version"
            java.lang.String r3 = r2.getProperty(r5, r3)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            com.google.android.gms.common.internal.GmsLogger r2 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            java.lang.String r5 = java.lang.String.valueOf(r10)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            int r5 = r5.length()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            int r5 = r5 + 12
            java.lang.String r6 = java.lang.String.valueOf(r3)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            int r6 = r6.length()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            int r5 = r5 + r6
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            r6.<init>(r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            r6.append(r10)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            java.lang.String r5 = " version is "
            r6.append(r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            r6.append(r3)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            java.lang.String r5 = r6.toString()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            r2.v(r1, r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            goto L86
        L6d:
            com.google.android.gms.common.internal.GmsLogger r2 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            java.lang.String r5 = java.lang.String.valueOf(r10)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            int r6 = r5.length()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            if (r6 == 0) goto L7e
            java.lang.String r5 = r0.concat(r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            goto L83
        L7e:
            java.lang.String r5 = new java.lang.String     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
            r5.<init>(r0)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
        L83:
            r2.w(r1, r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8f
        L86:
            if (r4 == 0) goto Lb8
            com.google.android.gms.common.util.IOUtils.closeQuietly(r4)
            goto Lb8
        L8c:
            r10 = move-exception
            r3 = r4
            goto Lca
        L8f:
            r2 = move-exception
            r8 = r4
            r4 = r3
            r3 = r8
            goto L98
        L94:
            r10 = move-exception
            goto Lca
        L96:
            r2 = move-exception
            r4 = r3
        L98:
            com.google.android.gms.common.internal.GmsLogger r5 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> L94
            java.lang.String r6 = java.lang.String.valueOf(r10)     // Catch: java.lang.Throwable -> L94
            int r7 = r6.length()     // Catch: java.lang.Throwable -> L94
            if (r7 == 0) goto La9
            java.lang.String r0 = r0.concat(r6)     // Catch: java.lang.Throwable -> L94
            goto Laf
        La9:
            java.lang.String r6 = new java.lang.String     // Catch: java.lang.Throwable -> L94
            r6.<init>(r0)     // Catch: java.lang.Throwable -> L94
            r0 = r6
        Laf:
            r5.e(r1, r0, r2)     // Catch: java.lang.Throwable -> L94
            if (r3 == 0) goto Lb7
            com.google.android.gms.common.util.IOUtils.closeQuietly(r3)
        Lb7:
            r3 = r4
        Lb8:
            if (r3 != 0) goto Lc4
        Lbb:
            com.google.android.gms.common.internal.GmsLogger r0 = com.google.android.gms.common.internal.LibraryVersion.zza
            java.lang.String r2 = ".properties file is dropped during release process. Failure to read app version is expected during Google internal testing where locally-built libraries are used"
            r0.d(r1, r2)
            java.lang.String r3 = "UNKNOWN"
        Lc4:
            java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> r0 = r9.zzc
            r0.put(r10, r3)
            return r3
        Lca:
            if (r3 == 0) goto Lcf
            com.google.android.gms.common.util.IOUtils.closeQuietly(r3)
        Lcf:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.internal.LibraryVersion.getVersion(java.lang.String):java.lang.String");
    }
}
