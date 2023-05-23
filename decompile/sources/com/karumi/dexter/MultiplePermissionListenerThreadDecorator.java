package com.karumi.dexter;

import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.util.List;

/* loaded from: classes.dex */
final class MultiplePermissionListenerThreadDecorator implements MultiplePermissionsListener {
    private final MultiplePermissionsListener listener;
    private final Thread thread;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MultiplePermissionListenerThreadDecorator(MultiplePermissionsListener multiplePermissionsListener, Thread thread) {
        this.thread = thread;
        this.listener = multiplePermissionsListener;
    }

    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
    public void onPermissionRationaleShouldBeShown(final List<PermissionRequest> list, final PermissionToken permissionToken) {
        this.thread.execute(new Runnable() { // from class: com.karumi.dexter.MultiplePermissionListenerThreadDecorator.2
            @Override // java.lang.Runnable
            public void run() {
                MultiplePermissionListenerThreadDecorator.this.listener.onPermissionRationaleShouldBeShown(list, permissionToken);
            }
        });
    }

    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
    public void onPermissionsChecked(final MultiplePermissionsReport multiplePermissionsReport) {
        this.thread.execute(new Runnable() { // from class: com.karumi.dexter.MultiplePermissionListenerThreadDecorator.1
            @Override // java.lang.Runnable
            public void run() {
                MultiplePermissionListenerThreadDecorator.this.listener.onPermissionsChecked(multiplePermissionsReport);
            }
        });
    }
}
