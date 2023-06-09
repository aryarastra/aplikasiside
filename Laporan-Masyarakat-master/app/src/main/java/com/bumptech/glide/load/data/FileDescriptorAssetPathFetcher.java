package com.bumptech.glide.load.data;

import android.content.res.AssetManager;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

/* loaded from: classes.dex */
public class FileDescriptorAssetPathFetcher extends AssetPathFetcher<ParcelFileDescriptor> {
    public FileDescriptorAssetPathFetcher(AssetManager assetManager, String assetPath) {
        super(assetManager, assetPath);
    }

    @Override // com.bumptech.glide.load.data.AssetPathFetcher
    public ParcelFileDescriptor loadResource(AssetManager assetManager, String path) throws IOException {
        return assetManager.openFd(path).getParcelFileDescriptor();
    }

    @Override // com.bumptech.glide.load.data.AssetPathFetcher
    public void close(ParcelFileDescriptor data) throws IOException {
        data.close();
    }

    @Override // com.bumptech.glide.load.data.DataFetcher
    public Class<ParcelFileDescriptor> getDataClass() {
        return ParcelFileDescriptor.class;
    }
}
