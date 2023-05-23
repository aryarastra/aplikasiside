package com.karumi.dexter.listener.single;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import java.util.Arrays;
import java.util.Collection;

/* loaded from: classes.dex */
public class CompositePermissionListener implements PermissionListener {
    private final Collection<PermissionListener> listeners;

    public CompositePermissionListener(Collection<PermissionListener> collection) {
        this.listeners = collection;
    }

    public CompositePermissionListener(PermissionListener... permissionListenerArr) {
        this(Arrays.asList(permissionListenerArr));
    }

    @Override // com.karumi.dexter.listener.single.PermissionListener
    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
        for (PermissionListener permissionListener : this.listeners) {
            permissionListener.onPermissionDenied(permissionDeniedResponse);
        }
    }

    @Override // com.karumi.dexter.listener.single.PermissionListener
    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
        for (PermissionListener permissionListener : this.listeners) {
            permissionListener.onPermissionGranted(permissionGrantedResponse);
        }
    }

    @Override // com.karumi.dexter.listener.single.PermissionListener
    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
        for (PermissionListener permissionListener : this.listeners) {
            permissionListener.onPermissionRationaleShouldBeShown(permissionRequest, permissionToken);
        }
    }
}
