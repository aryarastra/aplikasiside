package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.PreferredColorSpace;
import com.bumptech.glide.load.data.ParcelFileDescriptorRewinder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.ImageReader;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/* loaded from: classes.dex */
public final class Downsampler {
    static final String TAG = "Downsampler";
    private final BitmapPool bitmapPool;
    private final ArrayPool byteArrayPool;
    private final DisplayMetrics displayMetrics;
    private final HardwareConfigState hardwareConfigState = HardwareConfigState.getInstance();
    private final List<ImageHeaderParser> parsers;
    public static final Option<DecodeFormat> DECODE_FORMAT = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeFormat", DecodeFormat.DEFAULT);
    public static final Option<PreferredColorSpace> PREFERRED_COLOR_SPACE = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.PreferredColorSpace", PreferredColorSpace.SRGB);
    @Deprecated
    public static final Option<DownsampleStrategy> DOWNSAMPLE_STRATEGY = DownsampleStrategy.OPTION;
    public static final Option<Boolean> FIX_BITMAP_SIZE_TO_REQUESTED_DIMENSIONS = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.FixBitmapSize", false);
    public static final Option<Boolean> ALLOW_HARDWARE_CONFIG = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.AllowHardwareDecode", false);
    private static final String WBMP_MIME_TYPE = "image/vnd.wap.wbmp";
    private static final String ICO_MIME_TYPE = "image/x-ico";
    private static final Set<String> NO_DOWNSAMPLE_PRE_N_MIME_TYPES = Collections.unmodifiableSet(new HashSet(Arrays.asList(WBMP_MIME_TYPE, ICO_MIME_TYPE)));
    private static final DecodeCallbacks EMPTY_CALLBACKS = new DecodeCallbacks() { // from class: com.bumptech.glide.load.resource.bitmap.Downsampler.1
        @Override // com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeCallbacks
        public void onObtainBounds() {
        }

        @Override // com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeCallbacks
        public void onDecodeComplete(BitmapPool bitmapPool, Bitmap downsampled) {
        }
    };
    private static final Set<ImageHeaderParser.ImageType> TYPES_THAT_USE_POOL_PRE_KITKAT = Collections.unmodifiableSet(EnumSet.of(ImageHeaderParser.ImageType.JPEG, ImageHeaderParser.ImageType.PNG_A, ImageHeaderParser.ImageType.PNG));
    private static final Queue<BitmapFactory.Options> OPTIONS_QUEUE = Util.createQueue(0);

    /* loaded from: classes.dex */
    public interface DecodeCallbacks {
        void onDecodeComplete(BitmapPool bitmapPool, Bitmap bitmap) throws IOException;

        void onObtainBounds();
    }

    public Downsampler(List<ImageHeaderParser> parsers, DisplayMetrics displayMetrics, BitmapPool bitmapPool, ArrayPool byteArrayPool) {
        this.parsers = parsers;
        this.displayMetrics = (DisplayMetrics) Preconditions.checkNotNull(displayMetrics);
        this.bitmapPool = (BitmapPool) Preconditions.checkNotNull(bitmapPool);
        this.byteArrayPool = (ArrayPool) Preconditions.checkNotNull(byteArrayPool);
    }

    public boolean handles(InputStream is) {
        return true;
    }

    public boolean handles(ByteBuffer byteBuffer) {
        return true;
    }

    public boolean handles(ParcelFileDescriptor source) {
        return ParcelFileDescriptorRewinder.isSupported();
    }

    public Resource<Bitmap> decode(InputStream is, int outWidth, int outHeight, Options options) throws IOException {
        return decode(is, outWidth, outHeight, options, EMPTY_CALLBACKS);
    }

    public Resource<Bitmap> decode(InputStream is, int requestedWidth, int requestedHeight, Options options, DecodeCallbacks callbacks) throws IOException {
        return decode(new ImageReader.InputStreamImageReader(is, this.parsers, this.byteArrayPool), requestedWidth, requestedHeight, options, callbacks);
    }

    public Resource<Bitmap> decode(ParcelFileDescriptor parcelFileDescriptor, int outWidth, int outHeight, Options options) throws IOException {
        return decode(new ImageReader.ParcelFileDescriptorImageReader(parcelFileDescriptor, this.parsers, this.byteArrayPool), outWidth, outHeight, options, EMPTY_CALLBACKS);
    }

    private Resource<Bitmap> decode(ImageReader imageReader, int requestedWidth, int requestedHeight, Options options, DecodeCallbacks callbacks) throws IOException {
        byte[] bytesForOptions = (byte[]) this.byteArrayPool.get(65536, byte[].class);
        BitmapFactory.Options bitmapFactoryOptions = getDefaultOptions();
        bitmapFactoryOptions.inTempStorage = bytesForOptions;
        DecodeFormat decodeFormat = (DecodeFormat) options.get(DECODE_FORMAT);
        PreferredColorSpace preferredColorSpace = (PreferredColorSpace) options.get(PREFERRED_COLOR_SPACE);
        DownsampleStrategy downsampleStrategy = (DownsampleStrategy) options.get(DownsampleStrategy.OPTION);
        boolean fixBitmapToRequestedDimensions = ((Boolean) options.get(FIX_BITMAP_SIZE_TO_REQUESTED_DIMENSIONS)).booleanValue();
        Option<Boolean> option = ALLOW_HARDWARE_CONFIG;
        boolean isHardwareConfigAllowed = options.get(option) != null && ((Boolean) options.get(option)).booleanValue();
        try {
            Bitmap result = decodeFromWrappedStreams(imageReader, bitmapFactoryOptions, downsampleStrategy, decodeFormat, preferredColorSpace, isHardwareConfigAllowed, requestedWidth, requestedHeight, fixBitmapToRequestedDimensions, callbacks);
            return BitmapResource.obtain(result, this.bitmapPool);
        } finally {
            releaseOptions(bitmapFactoryOptions);
            this.byteArrayPool.put(bytesForOptions);
        }
    }

    private Bitmap decodeFromWrappedStreams(ImageReader imageReader, BitmapFactory.Options options, DownsampleStrategy downsampleStrategy, DecodeFormat decodeFormat, PreferredColorSpace preferredColorSpace, boolean isHardwareConfigAllowed, int requestedWidth, int requestedHeight, boolean fixBitmapToRequestedDimensions, DecodeCallbacks callbacks) throws IOException {
        int targetWidth;
        int targetHeight;
        Downsampler downsampler;
        String str;
        int expectedWidth;
        int expectedHeight;
        int expectedWidth2;
        long startTime = LogTime.getLogTime();
        int[] sourceDimensions = getDimensions(imageReader, options, callbacks, this.bitmapPool);
        boolean z = false;
        int sourceWidth = sourceDimensions[0];
        int sourceHeight = sourceDimensions[1];
        String sourceMimeType = options.outMimeType;
        boolean isHardwareConfigAllowed2 = (sourceWidth == -1 || sourceHeight == -1) ? false : isHardwareConfigAllowed;
        int orientation = imageReader.getImageOrientation();
        int degreesToRotate = TransformationUtils.getExifOrientationDegrees(orientation);
        boolean isExifOrientationRequired = TransformationUtils.isExifOrientationRequired(orientation);
        if (requestedWidth == Integer.MIN_VALUE) {
            targetWidth = isRotationRequired(degreesToRotate) ? sourceHeight : sourceWidth;
        } else {
            targetWidth = requestedWidth;
        }
        if (requestedHeight == Integer.MIN_VALUE) {
            targetHeight = isRotationRequired(degreesToRotate) ? sourceWidth : sourceHeight;
        } else {
            targetHeight = requestedHeight;
        }
        ImageHeaderParser.ImageType imageType = imageReader.getImageType();
        calculateScaling(imageType, imageReader, callbacks, this.bitmapPool, downsampleStrategy, degreesToRotate, sourceWidth, sourceHeight, targetWidth, targetHeight, options);
        calculateConfig(imageReader, decodeFormat, isHardwareConfigAllowed2, isExifOrientationRequired, options, targetWidth, targetHeight);
        boolean isKitKatOrGreater = Build.VERSION.SDK_INT >= 19;
        if (options.inSampleSize == 1 || isKitKatOrGreater) {
            downsampler = this;
            if (downsampler.shouldUsePool(imageType)) {
                if (sourceWidth >= 0 && sourceHeight >= 0 && fixBitmapToRequestedDimensions && isKitKatOrGreater) {
                    expectedWidth2 = targetWidth;
                    expectedHeight = targetHeight;
                    str = TAG;
                } else {
                    float densityMultiplier = isScaling(options) ? options.inTargetDensity / options.inDensity : 1.0f;
                    int sampleSize = options.inSampleSize;
                    int downsampledWidth = (int) Math.ceil(sourceWidth / sampleSize);
                    int downsampledHeight = (int) Math.ceil(sourceHeight / sampleSize);
                    int expectedWidth3 = Math.round(downsampledWidth * densityMultiplier);
                    int expectedHeight2 = Math.round(downsampledHeight * densityMultiplier);
                    str = TAG;
                    if (Log.isLoggable(str, 2)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Calculated target [");
                        sb.append(expectedWidth3);
                        sb.append("x");
                        sb.append(expectedHeight2);
                        expectedWidth = expectedWidth3;
                        sb.append("] for source [");
                        sb.append(sourceWidth);
                        sb.append("x");
                        sb.append(sourceHeight);
                        sb.append("], sampleSize: ");
                        sb.append(sampleSize);
                        sb.append(", targetDensity: ");
                        sb.append(options.inTargetDensity);
                        sb.append(", density: ");
                        sb.append(options.inDensity);
                        sb.append(", density multiplier: ");
                        sb.append(densityMultiplier);
                        Log.v(str, sb.toString());
                    } else {
                        expectedWidth = expectedWidth3;
                    }
                    expectedHeight = expectedHeight2;
                    expectedWidth2 = expectedWidth;
                }
                if (expectedWidth2 > 0 && expectedHeight > 0) {
                    setInBitmap(options, downsampler.bitmapPool, expectedWidth2, expectedHeight);
                }
            } else {
                str = TAG;
            }
        } else {
            downsampler = this;
            str = TAG;
        }
        if (Build.VERSION.SDK_INT >= 28) {
            if (preferredColorSpace == PreferredColorSpace.DISPLAY_P3 && options.outColorSpace != null && options.outColorSpace.isWideGamut()) {
                z = true;
            }
            boolean isP3Eligible = z;
            options.inPreferredColorSpace = ColorSpace.get(isP3Eligible ? ColorSpace.Named.DISPLAY_P3 : ColorSpace.Named.SRGB);
        } else if (Build.VERSION.SDK_INT >= 26) {
            options.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
        }
        Bitmap downsampled = decodeStream(imageReader, options, callbacks, downsampler.bitmapPool);
        callbacks.onDecodeComplete(downsampler.bitmapPool, downsampled);
        if (Log.isLoggable(str, 2)) {
            logDecode(sourceWidth, sourceHeight, sourceMimeType, options, downsampled, requestedWidth, requestedHeight, startTime);
        }
        Bitmap rotated = null;
        if (downsampled != null) {
            downsampled.setDensity(downsampler.displayMetrics.densityDpi);
            rotated = TransformationUtils.rotateImageExif(downsampler.bitmapPool, downsampled, orientation);
            if (!downsampled.equals(rotated)) {
                downsampler.bitmapPool.put(downsampled);
            }
        }
        return rotated;
    }

    private static void calculateScaling(ImageHeaderParser.ImageType imageType, ImageReader imageReader, DecodeCallbacks decodeCallbacks, BitmapPool bitmapPool, DownsampleStrategy downsampleStrategy, int degreesToRotate, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight, BitmapFactory.Options options) throws IOException {
        int scaleFactor;
        int powerOfTwoSampleSize;
        int scaleFactor2;
        int powerOfTwoHeight;
        int powerOfTwoWidth;
        if (sourceWidth > 0 && sourceHeight > 0) {
            int orientedSourceWidth = sourceWidth;
            int orientedSourceHeight = sourceHeight;
            if (isRotationRequired(degreesToRotate)) {
                orientedSourceWidth = sourceHeight;
                orientedSourceHeight = sourceWidth;
            }
            float exactScaleFactor = downsampleStrategy.getScaleFactor(orientedSourceWidth, orientedSourceHeight, targetWidth, targetHeight);
            if (exactScaleFactor <= 0.0f) {
                throw new IllegalArgumentException("Cannot scale with factor: " + exactScaleFactor + " from: " + downsampleStrategy + ", source: [" + sourceWidth + "x" + sourceHeight + "], target: [" + targetWidth + "x" + targetHeight + "]");
            }
            DownsampleStrategy.SampleSizeRounding rounding = downsampleStrategy.getSampleSizeRounding(orientedSourceWidth, orientedSourceHeight, targetWidth, targetHeight);
            if (rounding == null) {
                throw new IllegalArgumentException("Cannot round with null rounding");
            }
            int outWidth = round(orientedSourceWidth * exactScaleFactor);
            int outHeight = round(orientedSourceHeight * exactScaleFactor);
            int widthScaleFactor = orientedSourceWidth / outWidth;
            int outWidth2 = orientedSourceHeight / outHeight;
            if (rounding == DownsampleStrategy.SampleSizeRounding.MEMORY) {
                scaleFactor = Math.max(widthScaleFactor, outWidth2);
            } else {
                scaleFactor = Math.min(widthScaleFactor, outWidth2);
            }
            int heightScaleFactor = Build.VERSION.SDK_INT;
            if (heightScaleFactor <= 23 && NO_DOWNSAMPLE_PRE_N_MIME_TYPES.contains(options.outMimeType)) {
                powerOfTwoSampleSize = 1;
            } else {
                powerOfTwoSampleSize = Math.max(1, Integer.highestOneBit(scaleFactor));
                if (rounding == DownsampleStrategy.SampleSizeRounding.MEMORY && powerOfTwoSampleSize < 1.0f / exactScaleFactor) {
                    powerOfTwoSampleSize <<= 1;
                }
            }
            options.inSampleSize = powerOfTwoSampleSize;
            if (imageType != ImageHeaderParser.ImageType.JPEG) {
                scaleFactor2 = scaleFactor;
                if (imageType != ImageHeaderParser.ImageType.PNG && imageType != ImageHeaderParser.ImageType.PNG_A) {
                    if (imageType != ImageHeaderParser.ImageType.WEBP && imageType != ImageHeaderParser.ImageType.WEBP_A) {
                        if (orientedSourceWidth % powerOfTwoSampleSize != 0 || orientedSourceHeight % powerOfTwoSampleSize != 0) {
                            int[] dimensions = getDimensions(imageReader, options, decodeCallbacks, bitmapPool);
                            int powerOfTwoWidth2 = dimensions[0];
                            int powerOfTwoHeight2 = dimensions[1];
                            powerOfTwoHeight = powerOfTwoHeight2;
                            powerOfTwoWidth = powerOfTwoWidth2;
                        } else {
                            powerOfTwoWidth = orientedSourceWidth / powerOfTwoSampleSize;
                            powerOfTwoHeight = orientedSourceHeight / powerOfTwoSampleSize;
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 24) {
                        int powerOfTwoWidth3 = Math.round(orientedSourceWidth / powerOfTwoSampleSize);
                        powerOfTwoHeight = Math.round(orientedSourceHeight / powerOfTwoSampleSize);
                        powerOfTwoWidth = powerOfTwoWidth3;
                    } else {
                        int powerOfTwoWidth4 = (int) Math.floor(orientedSourceWidth / powerOfTwoSampleSize);
                        powerOfTwoHeight = (int) Math.floor(orientedSourceHeight / powerOfTwoSampleSize);
                        powerOfTwoWidth = powerOfTwoWidth4;
                    }
                }
                int powerOfTwoWidth5 = (int) Math.floor(orientedSourceWidth / powerOfTwoSampleSize);
                powerOfTwoHeight = (int) Math.floor(orientedSourceHeight / powerOfTwoSampleSize);
                powerOfTwoWidth = powerOfTwoWidth5;
            } else {
                int nativeScaling = Math.min(powerOfTwoSampleSize, 8);
                powerOfTwoWidth = (int) Math.ceil(orientedSourceWidth / nativeScaling);
                scaleFactor2 = scaleFactor;
                powerOfTwoHeight = (int) Math.ceil(orientedSourceHeight / nativeScaling);
                int secondaryScaling = powerOfTwoSampleSize / 8;
                if (secondaryScaling > 0) {
                    powerOfTwoWidth /= secondaryScaling;
                    powerOfTwoHeight /= secondaryScaling;
                }
            }
            double adjustedScaleFactor = downsampleStrategy.getScaleFactor(powerOfTwoWidth, powerOfTwoHeight, targetWidth, targetHeight);
            if (Build.VERSION.SDK_INT >= 19) {
                options.inTargetDensity = adjustTargetDensityForError(adjustedScaleFactor);
                options.inDensity = getDensityMultiplier(adjustedScaleFactor);
            }
            if (isScaling(options)) {
                options.inScaled = true;
            } else {
                options.inTargetDensity = 0;
                options.inDensity = 0;
            }
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Calculate scaling, source: [" + sourceWidth + "x" + sourceHeight + "], degreesToRotate: " + degreesToRotate + ", target: [" + targetWidth + "x" + targetHeight + "], power of two scaled: [" + powerOfTwoWidth + "x" + powerOfTwoHeight + "], exact scale factor: " + exactScaleFactor + ", power of 2 sample size: " + powerOfTwoSampleSize + ", adjusted scale factor: " + adjustedScaleFactor + ", target density: " + options.inTargetDensity + ", density: " + options.inDensity);
                return;
            }
            return;
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Unable to determine dimensions for: " + imageType + " with target [" + targetWidth + "x" + targetHeight + "]");
        }
    }

    private static int adjustTargetDensityForError(double adjustedScaleFactor) {
        int densityMultiplier = getDensityMultiplier(adjustedScaleFactor);
        int targetDensity = round(densityMultiplier * adjustedScaleFactor);
        float scaleFactorWithError = targetDensity / densityMultiplier;
        double difference = adjustedScaleFactor / scaleFactorWithError;
        return round(targetDensity * difference);
    }

    private static int getDensityMultiplier(double adjustedScaleFactor) {
        return (int) Math.round((adjustedScaleFactor <= 1.0d ? adjustedScaleFactor : 1.0d / adjustedScaleFactor) * 2.147483647E9d);
    }

    private static int round(double value) {
        return (int) (0.5d + value);
    }

    private boolean shouldUsePool(ImageHeaderParser.ImageType imageType) {
        if (Build.VERSION.SDK_INT >= 19) {
            return true;
        }
        return TYPES_THAT_USE_POOL_PRE_KITKAT.contains(imageType);
    }

    private void calculateConfig(ImageReader imageReader, DecodeFormat format, boolean isHardwareConfigAllowed, boolean isExifOrientationRequired, BitmapFactory.Options optionsWithScaling, int targetWidth, int targetHeight) {
        if (this.hardwareConfigState.setHardwareConfigIfAllowed(targetWidth, targetHeight, optionsWithScaling, isHardwareConfigAllowed, isExifOrientationRequired)) {
            return;
        }
        if (format == DecodeFormat.PREFER_ARGB_8888 || Build.VERSION.SDK_INT == 16) {
            optionsWithScaling.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return;
        }
        boolean hasAlpha = false;
        try {
            hasAlpha = imageReader.getImageType().hasAlpha();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Cannot determine whether the image has alpha or not from header, format " + format, e);
            }
        }
        optionsWithScaling.inPreferredConfig = hasAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        if (optionsWithScaling.inPreferredConfig == Bitmap.Config.RGB_565) {
            optionsWithScaling.inDither = true;
        }
    }

    private static int[] getDimensions(ImageReader imageReader, BitmapFactory.Options options, DecodeCallbacks decodeCallbacks, BitmapPool bitmapPool) throws IOException {
        options.inJustDecodeBounds = true;
        decodeStream(imageReader, options, decodeCallbacks, bitmapPool);
        options.inJustDecodeBounds = false;
        return new int[]{options.outWidth, options.outHeight};
    }

    private static Bitmap decodeStream(ImageReader imageReader, BitmapFactory.Options options, DecodeCallbacks callbacks, BitmapPool bitmapPool) throws IOException {
        if (!options.inJustDecodeBounds) {
            callbacks.onObtainBounds();
            imageReader.stopGrowingBuffers();
        }
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;
        String outMimeType = options.outMimeType;
        TransformationUtils.getBitmapDrawableLock().lock();
        try {
            try {
                Bitmap result = imageReader.decodeBitmap(options);
                TransformationUtils.getBitmapDrawableLock().unlock();
                return result;
            } catch (IllegalArgumentException e) {
                IOException bitmapAssertionException = newIoExceptionForInBitmapAssertion(e, sourceWidth, sourceHeight, outMimeType, options);
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Failed to decode with inBitmap, trying again without Bitmap re-use", bitmapAssertionException);
                }
                if (options.inBitmap != null) {
                    try {
                        bitmapPool.put(options.inBitmap);
                        options.inBitmap = null;
                        Bitmap decodeStream = decodeStream(imageReader, options, callbacks, bitmapPool);
                        TransformationUtils.getBitmapDrawableLock().unlock();
                        return decodeStream;
                    } catch (IOException e2) {
                        throw bitmapAssertionException;
                    }
                }
                throw bitmapAssertionException;
            }
        } catch (Throwable th) {
            TransformationUtils.getBitmapDrawableLock().unlock();
            throw th;
        }
    }

    private static boolean isScaling(BitmapFactory.Options options) {
        return options.inTargetDensity > 0 && options.inDensity > 0 && options.inTargetDensity != options.inDensity;
    }

    private static void logDecode(int sourceWidth, int sourceHeight, String outMimeType, BitmapFactory.Options options, Bitmap result, int requestedWidth, int requestedHeight, long startTime) {
        Log.v(TAG, "Decoded " + getBitmapString(result) + " from [" + sourceWidth + "x" + sourceHeight + "] " + outMimeType + " with inBitmap " + getInBitmapString(options) + " for [" + requestedWidth + "x" + requestedHeight + "], sample size: " + options.inSampleSize + ", density: " + options.inDensity + ", target density: " + options.inTargetDensity + ", thread: " + Thread.currentThread().getName() + ", duration: " + LogTime.getElapsedMillis(startTime));
    }

    private static String getInBitmapString(BitmapFactory.Options options) {
        return getBitmapString(options.inBitmap);
    }

    private static String getBitmapString(Bitmap bitmap) {
        String sizeString;
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            sizeString = " (" + bitmap.getAllocationByteCount() + ")";
        } else {
            sizeString = "";
        }
        return "[" + bitmap.getWidth() + "x" + bitmap.getHeight() + "] " + bitmap.getConfig() + sizeString;
    }

    private static IOException newIoExceptionForInBitmapAssertion(IllegalArgumentException e, int outWidth, int outHeight, String outMimeType, BitmapFactory.Options options) {
        return new IOException("Exception decoding bitmap, outWidth: " + outWidth + ", outHeight: " + outHeight + ", outMimeType: " + outMimeType + ", inBitmap: " + getInBitmapString(options), e);
    }

    private static void setInBitmap(BitmapFactory.Options options, BitmapPool bitmapPool, int width, int height) {
        Bitmap.Config expectedConfig = null;
        if (Build.VERSION.SDK_INT >= 26) {
            if (options.inPreferredConfig == Bitmap.Config.HARDWARE) {
                return;
            }
            expectedConfig = options.outConfig;
        }
        if (expectedConfig == null) {
            expectedConfig = options.inPreferredConfig;
        }
        options.inBitmap = bitmapPool.getDirty(width, height, expectedConfig);
    }

    private static synchronized BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options decodeBitmapOptions;
        synchronized (Downsampler.class) {
            Queue<BitmapFactory.Options> queue = OPTIONS_QUEUE;
            synchronized (queue) {
                decodeBitmapOptions = queue.poll();
            }
            if (decodeBitmapOptions == null) {
                decodeBitmapOptions = new BitmapFactory.Options();
                resetOptions(decodeBitmapOptions);
            }
        }
        return decodeBitmapOptions;
    }

    private static void releaseOptions(BitmapFactory.Options decodeBitmapOptions) {
        resetOptions(decodeBitmapOptions);
        Queue<BitmapFactory.Options> queue = OPTIONS_QUEUE;
        synchronized (queue) {
            queue.offer(decodeBitmapOptions);
        }
    }

    private static void resetOptions(BitmapFactory.Options decodeBitmapOptions) {
        decodeBitmapOptions.inTempStorage = null;
        decodeBitmapOptions.inDither = false;
        decodeBitmapOptions.inScaled = false;
        decodeBitmapOptions.inSampleSize = 1;
        decodeBitmapOptions.inPreferredConfig = null;
        decodeBitmapOptions.inJustDecodeBounds = false;
        decodeBitmapOptions.inDensity = 0;
        decodeBitmapOptions.inTargetDensity = 0;
        if (Build.VERSION.SDK_INT >= 26) {
            decodeBitmapOptions.inPreferredColorSpace = null;
            decodeBitmapOptions.outColorSpace = null;
            decodeBitmapOptions.outConfig = null;
        }
        decodeBitmapOptions.outWidth = 0;
        decodeBitmapOptions.outHeight = 0;
        decodeBitmapOptions.outMimeType = null;
        decodeBitmapOptions.inBitmap = null;
        decodeBitmapOptions.inMutable = true;
    }

    private static boolean isRotationRequired(int degreesToRotate) {
        return degreesToRotate == 90 || degreesToRotate == 270;
    }
}
