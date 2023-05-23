package com.google.android.gms.maps.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.RuntimeRemoteException;

/* compiled from: com.google.android.gms:play-services-maps@@18.0.0 */
/* loaded from: classes.dex */
public final class zzca {
    private static final String zza = zzca.class.getSimpleName();
    private static Context zzb = null;
    private static zzf zzc;

    public static zzf zza(Context context, MapsInitializer.Renderer renderer) throws GooglePlayServicesNotAvailableException {
        zzf zzeVar;
        Preconditions.checkNotNull(context);
        String str = zza;
        String valueOf = String.valueOf(renderer);
        String.valueOf(valueOf).length();
        Log.d(str, "preferredRenderer: ".concat(String.valueOf(valueOf)));
        zzf zzfVar = zzc;
        if (zzfVar == null) {
            int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context, 13400000);
            switch (isGooglePlayServicesAvailable) {
                case 0:
                    Log.i(str, "Making Creator dynamically");
                    try {
                        IBinder iBinder = (IBinder) zzd(((ClassLoader) Preconditions.checkNotNull(zzc(context, renderer).getClassLoader())).loadClass("com.google.android.gms.maps.internal.CreatorImpl"));
                        if (iBinder == null) {
                            zzeVar = null;
                        } else {
                            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.maps.internal.ICreator");
                            zzeVar = queryLocalInterface instanceof zzf ? (zzf) queryLocalInterface : new zze(iBinder);
                        }
                        zzc = zzeVar;
                        try {
                            Context zzc2 = zzc(context, renderer);
                            zzc2.getClass();
                            zzeVar.zzk(ObjectWrapper.wrap(zzc2.getResources()), GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);
                            return zzc;
                        } catch (RemoteException e) {
                            throw new RuntimeRemoteException(e);
                        }
                    } catch (ClassNotFoundException e2) {
                        throw new IllegalStateException("Unable to find dynamic class com.google.android.gms.maps.internal.CreatorImpl");
                    }
                default:
                    throw new GooglePlayServicesNotAvailableException(isGooglePlayServicesAvailable);
            }
        }
        return zzfVar;
    }

    private static Context zzb(Exception exc, Context context) {
        Log.e(zza, "Failed to load maps module, use pre-Chimera", exc);
        return GooglePlayServicesUtil.getRemoteContext(context);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(7:3|(6:5|6|8|9|10|11)|24|8|9|10|11) */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x0026, code lost:
        r1 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x002b, code lost:
        if (r3.equals("com.google.android.gms.maps_dynamite") == false) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x002d, code lost:
        android.util.Log.d(com.google.android.gms.maps.internal.zzca.zza, "Attempting to load maps_dynamite again.");
        r2 = com.google.android.gms.dynamite.DynamiteModule.load(r2, com.google.android.gms.dynamite.DynamiteModule.PREFER_REMOTE, "com.google.android.gms.maps_dynamite").getModuleContext();
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x003f, code lost:
        r3 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x0040, code lost:
        r2 = zzb(r3, r2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x0045, code lost:
        r2 = zzb(r1, r2);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static android.content.Context zzc(android.content.Context r2, com.google.android.gms.maps.MapsInitializer.Renderer r3) {
        /*
            android.content.Context r0 = com.google.android.gms.maps.internal.zzca.zzb
            if (r0 != 0) goto L4c
            r2.getApplicationContext()
            java.lang.String r0 = "com.google.android.gms.maps_dynamite"
            if (r3 == 0) goto L19
            int r3 = r3.ordinal()
            switch(r3) {
                case 0: goto L16;
                case 1: goto L13;
                default: goto L12;
            }
        L12:
            goto L1a
        L13:
            java.lang.String r3 = "com.google.android.gms.maps_core_dynamite"
            goto L1b
        L16:
            java.lang.String r3 = "com.google.android.gms.maps_legacy_dynamite"
            goto L1b
        L19:
        L1a:
            r3 = r0
        L1b:
            com.google.android.gms.dynamite.DynamiteModule$VersionPolicy r1 = com.google.android.gms.dynamite.DynamiteModule.PREFER_REMOTE     // Catch: java.lang.Exception -> L26
            com.google.android.gms.dynamite.DynamiteModule r1 = com.google.android.gms.dynamite.DynamiteModule.load(r2, r1, r3)     // Catch: java.lang.Exception -> L26
            android.content.Context r2 = r1.getModuleContext()     // Catch: java.lang.Exception -> L26
            goto L49
        L26:
            r1 = move-exception
            boolean r3 = r3.equals(r0)
            if (r3 != 0) goto L45
            java.lang.String r3 = com.google.android.gms.maps.internal.zzca.zza     // Catch: java.lang.Exception -> L3f
            java.lang.String r1 = "Attempting to load maps_dynamite again."
            android.util.Log.d(r3, r1)     // Catch: java.lang.Exception -> L3f
            com.google.android.gms.dynamite.DynamiteModule$VersionPolicy r3 = com.google.android.gms.dynamite.DynamiteModule.PREFER_REMOTE     // Catch: java.lang.Exception -> L3f
            com.google.android.gms.dynamite.DynamiteModule r3 = com.google.android.gms.dynamite.DynamiteModule.load(r2, r3, r0)     // Catch: java.lang.Exception -> L3f
            android.content.Context r2 = r3.getModuleContext()     // Catch: java.lang.Exception -> L3f
            goto L49
        L3f:
            r3 = move-exception
            android.content.Context r2 = zzb(r3, r2)
            goto L49
        L45:
            android.content.Context r2 = zzb(r1, r2)
        L49:
            com.google.android.gms.maps.internal.zzca.zzb = r2
            return r2
        L4c:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.maps.internal.zzca.zzc(android.content.Context, com.google.android.gms.maps.MapsInitializer$Renderer):android.content.Context");
    }

    private static <T> T zzd(Class cls) {
        try {
            return (T) cls.newInstance();
        } catch (IllegalAccessException e) {
            String valueOf = String.valueOf(cls.getName());
            throw new IllegalStateException(valueOf.length() != 0 ? "Unable to call the default constructor of ".concat(valueOf) : new String("Unable to call the default constructor of "));
        } catch (InstantiationException e2) {
            String valueOf2 = String.valueOf(cls.getName());
            throw new IllegalStateException(valueOf2.length() != 0 ? "Unable to instantiate the dynamic class ".concat(valueOf2) : new String("Unable to instantiate the dynamic class "));
        }
    }
}
