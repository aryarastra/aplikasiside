package com.bumptech.glide.load.resource;

import android.graphics.ColorSpace;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.PreferredColorSpace;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.HardwareConfigState;
import java.io.IOException;

/* loaded from: classes.dex */
public abstract class ImageDecoderResourceDecoder<T> implements ResourceDecoder<ImageDecoder.Source, T> {
    private static final String TAG = "ImageDecoder";
    final HardwareConfigState hardwareConfigState = HardwareConfigState.getInstance();

    protected abstract Resource<T> decode(ImageDecoder.Source source, int i, int i2, ImageDecoder.OnHeaderDecodedListener onHeaderDecodedListener) throws IOException;

    @Override // com.bumptech.glide.load.ResourceDecoder
    public final boolean handles(ImageDecoder.Source source, Options options) {
        return true;
    }

    @Override // com.bumptech.glide.load.ResourceDecoder
    public final Resource<T> decode(ImageDecoder.Source source, final int requestedWidth, final int requestedHeight, Options options) throws IOException {
        final DecodeFormat decodeFormat = (DecodeFormat) options.get(Downsampler.DECODE_FORMAT);
        final DownsampleStrategy strategy = (DownsampleStrategy) options.get(DownsampleStrategy.OPTION);
        final boolean isHardwareConfigAllowed = options.get(Downsampler.ALLOW_HARDWARE_CONFIG) != null && ((Boolean) options.get(Downsampler.ALLOW_HARDWARE_CONFIG)).booleanValue();
        final PreferredColorSpace preferredColorSpace = (PreferredColorSpace) options.get(Downsampler.PREFERRED_COLOR_SPACE);
        return decode(source, requestedWidth, requestedHeight, new ImageDecoder.OnHeaderDecodedListener() { // from class: com.bumptech.glide.load.resource.ImageDecoderResourceDecoder.1
            @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
            public void onHeaderDecoded(ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source source2) {
                boolean z = false;
                if (ImageDecoderResourceDecoder.this.hardwareConfigState.isHardwareConfigAllowed(requestedWidth, requestedHeight, isHardwareConfigAllowed, false)) {
                    decoder.setAllocator(3);
                } else {
                    decoder.setAllocator(1);
                }
                if (decodeFormat == DecodeFormat.PREFER_RGB_565) {
                    decoder.setMemorySizePolicy(0);
                }
                decoder.setOnPartialImageListener(new ImageDecoder.OnPartialImageListener() { // from class: com.bumptech.glide.load.resource.ImageDecoderResourceDecoder.1.1
                    @Override // android.graphics.ImageDecoder.OnPartialImageListener
                    public boolean onPartialImage(ImageDecoder.DecodeException e) {
                        return false;
                    }
                });
                Size size = info.getSize();
                int targetWidth = requestedWidth;
                if (requestedWidth == Integer.MIN_VALUE) {
                    targetWidth = size.getWidth();
                }
                int targetHeight = requestedHeight;
                if (requestedHeight == Integer.MIN_VALUE) {
                    targetHeight = size.getHeight();
                }
                float scaleFactor = strategy.getScaleFactor(size.getWidth(), size.getHeight(), targetWidth, targetHeight);
                int resizeWidth = Math.round(size.getWidth() * scaleFactor);
                int resizeHeight = Math.round(size.getHeight() * scaleFactor);
                if (Log.isLoggable(ImageDecoderResourceDecoder.TAG, 2)) {
                    Log.v(ImageDecoderResourceDecoder.TAG, "Resizing from [" + size.getWidth() + "x" + size.getHeight() + "] to [" + resizeWidth + "x" + resizeHeight + "] scaleFactor: " + scaleFactor);
                }
                decoder.setTargetSize(resizeWidth, resizeHeight);
                if (Build.VERSION.SDK_INT >= 28) {
                    if (preferredColorSpace == PreferredColorSpace.DISPLAY_P3 && info.getColorSpace() != null && info.getColorSpace().isWideGamut()) {
                        z = true;
                    }
                    boolean isP3Eligible = z;
                    decoder.setTargetColorSpace(ColorSpace.get(isP3Eligible ? ColorSpace.Named.DISPLAY_P3 : ColorSpace.Named.SRGB));
                } else if (Build.VERSION.SDK_INT >= 26) {
                    decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB));
                }
            }
        });
    }
}
