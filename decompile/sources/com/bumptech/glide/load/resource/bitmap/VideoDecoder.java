package com.bumptech.glide.load.resource.bitmap;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/* loaded from: classes.dex */
public class VideoDecoder<T> implements ResourceDecoder<T, Bitmap> {
    public static final long DEFAULT_FRAME = -1;
    static final int DEFAULT_FRAME_OPTION = 2;
    private static final String TAG = "VideoDecoder";
    private final BitmapPool bitmapPool;
    private final MediaMetadataRetrieverFactory factory;
    private final MediaMetadataRetrieverInitializer<T> initializer;
    public static final Option<Long> TARGET_FRAME = Option.disk("com.bumptech.glide.load.resource.bitmap.VideoBitmapDecode.TargetFrame", -1L, new Option.CacheKeyUpdater<Long>() { // from class: com.bumptech.glide.load.resource.bitmap.VideoDecoder.1
        private final ByteBuffer buffer = ByteBuffer.allocate(8);

        @Override // com.bumptech.glide.load.Option.CacheKeyUpdater
        public void update(byte[] keyBytes, Long value, MessageDigest messageDigest) {
            messageDigest.update(keyBytes);
            synchronized (this.buffer) {
                this.buffer.position(0);
                messageDigest.update(this.buffer.putLong(value.longValue()).array());
            }
        }
    });
    public static final Option<Integer> FRAME_OPTION = Option.disk("com.bumptech.glide.load.resource.bitmap.VideoBitmapDecode.FrameOption", 2, new Option.CacheKeyUpdater<Integer>() { // from class: com.bumptech.glide.load.resource.bitmap.VideoDecoder.2
        private final ByteBuffer buffer = ByteBuffer.allocate(4);

        @Override // com.bumptech.glide.load.Option.CacheKeyUpdater
        public void update(byte[] keyBytes, Integer value, MessageDigest messageDigest) {
            if (value == null) {
                return;
            }
            messageDigest.update(keyBytes);
            synchronized (this.buffer) {
                this.buffer.position(0);
                messageDigest.update(this.buffer.putInt(value.intValue()).array());
            }
        }
    });
    private static final MediaMetadataRetrieverFactory DEFAULT_FACTORY = new MediaMetadataRetrieverFactory();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface MediaMetadataRetrieverInitializer<T> {
        void initialize(MediaMetadataRetriever mediaMetadataRetriever, T t);
    }

    public static ResourceDecoder<AssetFileDescriptor, Bitmap> asset(BitmapPool bitmapPool) {
        return new VideoDecoder(bitmapPool, new AssetFileDescriptorInitializer());
    }

    public static ResourceDecoder<ParcelFileDescriptor, Bitmap> parcel(BitmapPool bitmapPool) {
        return new VideoDecoder(bitmapPool, new ParcelFileDescriptorInitializer());
    }

    public static ResourceDecoder<ByteBuffer, Bitmap> byteBuffer(BitmapPool bitmapPool) {
        return new VideoDecoder(bitmapPool, new ByteBufferInitializer());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public VideoDecoder(BitmapPool bitmapPool, MediaMetadataRetrieverInitializer<T> initializer) {
        this(bitmapPool, initializer, DEFAULT_FACTORY);
    }

    VideoDecoder(BitmapPool bitmapPool, MediaMetadataRetrieverInitializer<T> initializer, MediaMetadataRetrieverFactory factory) {
        this.bitmapPool = bitmapPool;
        this.initializer = initializer;
        this.factory = factory;
    }

    @Override // com.bumptech.glide.load.ResourceDecoder
    public boolean handles(T data, Options options) {
        return true;
    }

    @Override // com.bumptech.glide.load.ResourceDecoder
    public Resource<Bitmap> decode(T resource, int outWidth, int outHeight, Options options) throws IOException {
        Integer frameOption;
        DownsampleStrategy downsampleStrategy;
        long frameTimeMicros = ((Long) options.get(TARGET_FRAME)).longValue();
        if (frameTimeMicros < 0 && frameTimeMicros != -1) {
            throw new IllegalArgumentException("Requested frame must be non-negative, or DEFAULT_FRAME, given: " + frameTimeMicros);
        }
        Integer frameOption2 = (Integer) options.get(FRAME_OPTION);
        if (frameOption2 != null) {
            frameOption = frameOption2;
        } else {
            frameOption = 2;
        }
        DownsampleStrategy downsampleStrategy2 = (DownsampleStrategy) options.get(DownsampleStrategy.OPTION);
        if (downsampleStrategy2 != null) {
            downsampleStrategy = downsampleStrategy2;
        } else {
            downsampleStrategy = DownsampleStrategy.DEFAULT;
        }
        MediaMetadataRetriever mediaMetadataRetriever = this.factory.build();
        try {
        } catch (Throwable th) {
            th = th;
        }
        try {
            this.initializer.initialize(mediaMetadataRetriever, resource);
            Bitmap result = decodeFrame(mediaMetadataRetriever, frameTimeMicros, frameOption.intValue(), outWidth, outHeight, downsampleStrategy);
            mediaMetadataRetriever.release();
            return BitmapResource.obtain(result, this.bitmapPool);
        } catch (Throwable th2) {
            th = th2;
            mediaMetadataRetriever.release();
            throw th;
        }
    }

    private static Bitmap decodeFrame(MediaMetadataRetriever mediaMetadataRetriever, long frameTimeMicros, int frameOption, int outWidth, int outHeight, DownsampleStrategy strategy) {
        Bitmap result = null;
        if (Build.VERSION.SDK_INT >= 27 && outWidth != Integer.MIN_VALUE && outHeight != Integer.MIN_VALUE && strategy != DownsampleStrategy.NONE) {
            result = decodeScaledFrame(mediaMetadataRetriever, frameTimeMicros, frameOption, outWidth, outHeight, strategy);
        }
        if (result == null) {
            result = decodeOriginalFrame(mediaMetadataRetriever, frameTimeMicros, frameOption);
        }
        if (result == null) {
            throw new VideoDecoderException();
        }
        return result;
    }

    private static Bitmap decodeScaledFrame(MediaMetadataRetriever mediaMetadataRetriever, long frameTimeMicros, int frameOption, int outWidth, int outHeight, DownsampleStrategy strategy) {
        int originalWidth;
        int originalHeight;
        try {
            originalWidth = Integer.parseInt(mediaMetadataRetriever.extractMetadata(18));
            int originalHeight2 = Integer.parseInt(mediaMetadataRetriever.extractMetadata(19));
            int orientation = Integer.parseInt(mediaMetadataRetriever.extractMetadata(24));
            if (orientation == 90 || orientation == 270) {
                originalWidth = originalHeight2;
                originalHeight = originalWidth;
            } else {
                originalHeight = originalHeight2;
            }
        } catch (Throwable th) {
            t = th;
        }
        try {
            float scaleFactor = strategy.getScaleFactor(originalWidth, originalHeight, outWidth, outHeight);
            int decodeWidth = Math.round(originalWidth * scaleFactor);
            int decodeHeight = Math.round(originalHeight * scaleFactor);
            return mediaMetadataRetriever.getScaledFrameAtTime(frameTimeMicros, frameOption, decodeWidth, decodeHeight);
        } catch (Throwable th2) {
            t = th2;
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Exception trying to decode a scaled frame on oreo+, falling back to a fullsize frame", t);
                return null;
            }
            return null;
        }
    }

    private static Bitmap decodeOriginalFrame(MediaMetadataRetriever mediaMetadataRetriever, long frameTimeMicros, int frameOption) {
        return mediaMetadataRetriever.getFrameAtTime(frameTimeMicros, frameOption);
    }

    /* loaded from: classes.dex */
    static class MediaMetadataRetrieverFactory {
        MediaMetadataRetrieverFactory() {
        }

        public MediaMetadataRetriever build() {
            return new MediaMetadataRetriever();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class AssetFileDescriptorInitializer implements MediaMetadataRetrieverInitializer<AssetFileDescriptor> {
        private AssetFileDescriptorInitializer() {
        }

        @Override // com.bumptech.glide.load.resource.bitmap.VideoDecoder.MediaMetadataRetrieverInitializer
        public void initialize(MediaMetadataRetriever retriever, AssetFileDescriptor data) {
            retriever.setDataSource(data.getFileDescriptor(), data.getStartOffset(), data.getLength());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ParcelFileDescriptorInitializer implements MediaMetadataRetrieverInitializer<ParcelFileDescriptor> {
        @Override // com.bumptech.glide.load.resource.bitmap.VideoDecoder.MediaMetadataRetrieverInitializer
        public void initialize(MediaMetadataRetriever retriever, ParcelFileDescriptor data) {
            retriever.setDataSource(data.getFileDescriptor());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ByteBufferInitializer implements MediaMetadataRetrieverInitializer<ByteBuffer> {
        ByteBufferInitializer() {
        }

        @Override // com.bumptech.glide.load.resource.bitmap.VideoDecoder.MediaMetadataRetrieverInitializer
        public void initialize(MediaMetadataRetriever retriever, final ByteBuffer data) {
            retriever.setDataSource(new MediaDataSource() { // from class: com.bumptech.glide.load.resource.bitmap.VideoDecoder.ByteBufferInitializer.1
                @Override // android.media.MediaDataSource
                public int readAt(long position, byte[] buffer, int offset, int size) {
                    if (position >= data.limit()) {
                        return -1;
                    }
                    data.position((int) position);
                    int numBytesRead = Math.min(size, data.remaining());
                    data.get(buffer, offset, numBytesRead);
                    return numBytesRead;
                }

                @Override // android.media.MediaDataSource
                public long getSize() {
                    return data.limit();
                }

                @Override // java.io.Closeable, java.lang.AutoCloseable
                public void close() {
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class VideoDecoderException extends RuntimeException {
        private static final long serialVersionUID = -2556382523004027815L;

        VideoDecoderException() {
            super("MediaMetadataRetriever failed to retrieve a frame without throwing, check the adb logs for .*MetadataRetriever.* prior to this exception for details");
        }
    }
}
