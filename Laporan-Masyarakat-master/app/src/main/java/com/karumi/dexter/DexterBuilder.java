package com.karumi.dexter;

import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import java.util.Collection;

/* loaded from: classes.dex */
public interface DexterBuilder {

    /* loaded from: classes.dex */
    public interface MultiPermissionListener {
        DexterBuilder withListener(MultiplePermissionsListener multiplePermissionsListener);
    }

    /* loaded from: classes.dex */
    public interface Permission {
        SinglePermissionListener withPermission(String str);

        MultiPermissionListener withPermissions(Collection<String> collection);

        MultiPermissionListener withPermissions(String... strArr);
    }

    /* loaded from: classes.dex */
    public interface SinglePermissionListener {
        DexterBuilder withListener(PermissionListener permissionListener);
    }

    void check();

    DexterBuilder onSameThread();

    DexterBuilder withErrorListener(PermissionRequestErrorListener permissionRequestErrorListener);
}
