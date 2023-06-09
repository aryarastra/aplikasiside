package com.bumptech.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.ErrorRequestCoordinator;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestCoordinator;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.SingleRequest;
import com.bumptech.glide.request.ThumbnailRequestCoordinator;
import com.bumptech.glide.request.target.PreloadTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.AndroidResourceSignature;
import com.bumptech.glide.util.Executors;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class RequestBuilder<TranscodeType> extends BaseRequestOptions<RequestBuilder<TranscodeType>> implements Cloneable, ModelTypes<RequestBuilder<TranscodeType>> {
    protected static final RequestOptions DOWNLOAD_ONLY_OPTIONS = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).priority(Priority.LOW).skipMemoryCache(true);
    private final Context context;
    private RequestBuilder<TranscodeType> errorBuilder;
    private final Glide glide;
    private final GlideContext glideContext;
    private boolean isDefaultTransitionOptionsSet;
    private boolean isModelSet;
    private boolean isThumbnailBuilt;
    private Object model;
    private List<RequestListener<TranscodeType>> requestListeners;
    private final RequestManager requestManager;
    private Float thumbSizeMultiplier;
    private RequestBuilder<TranscodeType> thumbnailBuilder;
    private final Class<TranscodeType> transcodeClass;
    private TransitionOptions<?, ? super TranscodeType> transitionOptions;

    @Override // com.bumptech.glide.request.BaseRequestOptions
    public /* bridge */ /* synthetic */ BaseRequestOptions apply(BaseRequestOptions baseRequestOptions) {
        return apply((BaseRequestOptions<?>) baseRequestOptions);
    }

    public RequestBuilder(Glide glide, RequestManager requestManager, Class<TranscodeType> transcodeClass, Context context) {
        this.isDefaultTransitionOptionsSet = true;
        this.glide = glide;
        this.requestManager = requestManager;
        this.transcodeClass = transcodeClass;
        this.context = context;
        this.transitionOptions = requestManager.getDefaultTransitionOptions(transcodeClass);
        this.glideContext = glide.getGlideContext();
        initRequestListeners(requestManager.getDefaultRequestListeners());
        apply((BaseRequestOptions<?>) requestManager.getDefaultRequestOptions());
    }

    protected RequestBuilder(Class<TranscodeType> transcodeClass, RequestBuilder<?> other) {
        this(other.glide, other.requestManager, transcodeClass, other.context);
        this.model = other.model;
        this.isModelSet = other.isModelSet;
        apply((BaseRequestOptions<?>) other);
    }

    private void initRequestListeners(List<RequestListener<Object>> requestListeners) {
        for (RequestListener<Object> requestListener : requestListeners) {
            addListener(requestListener);
        }
    }

    @Override // com.bumptech.glide.request.BaseRequestOptions
    public RequestBuilder<TranscodeType> apply(BaseRequestOptions<?> requestOptions) {
        Preconditions.checkNotNull(requestOptions);
        return (RequestBuilder) super.apply(requestOptions);
    }

    public RequestBuilder<TranscodeType> transition(TransitionOptions<?, ? super TranscodeType> transitionOptions) {
        if (isAutoCloneEnabled()) {
            return mo46clone().transition(transitionOptions);
        }
        this.transitionOptions = (TransitionOptions) Preconditions.checkNotNull(transitionOptions);
        this.isDefaultTransitionOptionsSet = false;
        return selfOrThrowIfLocked();
    }

    public RequestBuilder<TranscodeType> listener(RequestListener<TranscodeType> requestListener) {
        if (isAutoCloneEnabled()) {
            return mo46clone().listener(requestListener);
        }
        this.requestListeners = null;
        return addListener(requestListener);
    }

    public RequestBuilder<TranscodeType> addListener(RequestListener<TranscodeType> requestListener) {
        if (isAutoCloneEnabled()) {
            return mo46clone().addListener(requestListener);
        }
        if (requestListener != null) {
            if (this.requestListeners == null) {
                this.requestListeners = new ArrayList();
            }
            this.requestListeners.add(requestListener);
        }
        return selfOrThrowIfLocked();
    }

    public RequestBuilder<TranscodeType> error(RequestBuilder<TranscodeType> errorBuilder) {
        if (isAutoCloneEnabled()) {
            return mo46clone().error((RequestBuilder) errorBuilder);
        }
        this.errorBuilder = errorBuilder;
        return selfOrThrowIfLocked();
    }

    public RequestBuilder<TranscodeType> error(Object model) {
        if (model == null) {
            return error((RequestBuilder) null);
        }
        return error((RequestBuilder) cloneWithNullErrorAndThumbnail().load(model));
    }

    private RequestBuilder<TranscodeType> cloneWithNullErrorAndThumbnail() {
        RequestBuilder<TranscodeType> requestBuilder = null;
        return mo46clone().error((RequestBuilder) requestBuilder).thumbnail(requestBuilder);
    }

    public RequestBuilder<TranscodeType> thumbnail(RequestBuilder<TranscodeType> thumbnailRequest) {
        if (isAutoCloneEnabled()) {
            return mo46clone().thumbnail(thumbnailRequest);
        }
        this.thumbnailBuilder = thumbnailRequest;
        return selfOrThrowIfLocked();
    }

    public RequestBuilder<TranscodeType> thumbnail(RequestBuilder<TranscodeType>... thumbnails) {
        if (thumbnails == null || thumbnails.length == 0) {
            return thumbnail((RequestBuilder) null);
        }
        return thumbnail(Arrays.asList(thumbnails));
    }

    public RequestBuilder<TranscodeType> thumbnail(List<RequestBuilder<TranscodeType>> thumbnails) {
        if (thumbnails == null || thumbnails.isEmpty()) {
            return thumbnail((RequestBuilder) null);
        }
        RequestBuilder<TranscodeType> previous = null;
        for (int i = thumbnails.size() - 1; i >= 0; i--) {
            RequestBuilder<TranscodeType> current = thumbnails.get(i);
            if (current != null) {
                if (previous == null) {
                    previous = current;
                } else {
                    previous = current.thumbnail(previous);
                }
            }
        }
        return thumbnail(previous);
    }

    public RequestBuilder<TranscodeType> thumbnail(float sizeMultiplier) {
        if (isAutoCloneEnabled()) {
            return mo46clone().thumbnail(sizeMultiplier);
        }
        if (sizeMultiplier < 0.0f || sizeMultiplier > 1.0f) {
            throw new IllegalArgumentException("sizeMultiplier must be between 0 and 1");
        }
        this.thumbSizeMultiplier = Float.valueOf(sizeMultiplier);
        return selfOrThrowIfLocked();
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(Object model) {
        return loadGeneric(model);
    }

    private RequestBuilder<TranscodeType> loadGeneric(Object model) {
        if (isAutoCloneEnabled()) {
            return mo46clone().loadGeneric(model);
        }
        this.model = model;
        this.isModelSet = true;
        return selfOrThrowIfLocked();
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(Bitmap bitmap) {
        return loadGeneric(bitmap).apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(Drawable drawable) {
        return loadGeneric(drawable).apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(String string) {
        return loadGeneric(string);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(Uri uri) {
        return loadGeneric(uri);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(File file) {
        return loadGeneric(file);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(Integer resourceId) {
        return loadGeneric(resourceId).apply((BaseRequestOptions<?>) RequestOptions.signatureOf(AndroidResourceSignature.obtain(this.context)));
    }

    @Override // com.bumptech.glide.ModelTypes
    @Deprecated
    public RequestBuilder<TranscodeType> load(URL url) {
        return loadGeneric(url);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<TranscodeType> load(byte[] model) {
        RequestBuilder<TranscodeType> result = loadGeneric(model);
        if (!result.isDiskCacheStrategySet()) {
            result = result.apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
        }
        if (!result.isSkipMemoryCacheSet()) {
            return result.apply((BaseRequestOptions<?>) RequestOptions.skipMemoryCacheOf(true));
        }
        return result;
    }

    @Override // com.bumptech.glide.request.BaseRequestOptions
    /* renamed from: clone */
    public RequestBuilder<TranscodeType> mo46clone() {
        RequestBuilder<TranscodeType> result = (RequestBuilder) super.mo46clone();
        result.transitionOptions = (TransitionOptions<?, ? super TranscodeType>) result.transitionOptions.m47clone();
        if (result.requestListeners != null) {
            result.requestListeners = new ArrayList(result.requestListeners);
        }
        RequestBuilder<TranscodeType> requestBuilder = result.thumbnailBuilder;
        if (requestBuilder != null) {
            result.thumbnailBuilder = requestBuilder.mo46clone();
        }
        RequestBuilder<TranscodeType> requestBuilder2 = result.errorBuilder;
        if (requestBuilder2 != null) {
            result.errorBuilder = requestBuilder2.mo46clone();
        }
        return result;
    }

    public <Y extends Target<TranscodeType>> Y into(Y target) {
        return (Y) into(target, null, Executors.mainThreadExecutor());
    }

    <Y extends Target<TranscodeType>> Y into(Y target, RequestListener<TranscodeType> targetListener, Executor callbackExecutor) {
        return (Y) into(target, targetListener, this, callbackExecutor);
    }

    private <Y extends Target<TranscodeType>> Y into(Y target, RequestListener<TranscodeType> targetListener, BaseRequestOptions<?> options, Executor callbackExecutor) {
        Preconditions.checkNotNull(target);
        if (!this.isModelSet) {
            throw new IllegalArgumentException("You must call #load() before calling #into()");
        }
        Request request = buildRequest(target, targetListener, options, callbackExecutor);
        Request previous = target.getRequest();
        if (request.isEquivalentTo(previous) && !isSkipMemoryCacheWithCompletePreviousRequest(options, previous)) {
            if (!((Request) Preconditions.checkNotNull(previous)).isRunning()) {
                previous.begin();
            }
            return target;
        }
        this.requestManager.clear((Target<?>) target);
        target.setRequest(request);
        this.requestManager.track(target, request);
        return target;
    }

    private boolean isSkipMemoryCacheWithCompletePreviousRequest(BaseRequestOptions<?> options, Request previous) {
        return !options.isMemoryCacheable() && previous.isComplete();
    }

    /* JADX WARN: Type inference failed for: r1v10, types: [com.bumptech.glide.request.BaseRequestOptions] */
    /* JADX WARN: Type inference failed for: r1v11, types: [com.bumptech.glide.request.BaseRequestOptions] */
    /* JADX WARN: Type inference failed for: r1v12, types: [com.bumptech.glide.request.BaseRequestOptions] */
    /* JADX WARN: Type inference failed for: r1v9, types: [com.bumptech.glide.request.BaseRequestOptions] */
    public ViewTarget<ImageView, TranscodeType> into(ImageView view) {
        Util.assertMainThread();
        Preconditions.checkNotNull(view);
        BaseRequestOptions<?> requestOptions = this;
        if (!requestOptions.isTransformationSet() && requestOptions.isTransformationAllowed() && view.getScaleType() != null) {
            switch (AnonymousClass1.$SwitchMap$android$widget$ImageView$ScaleType[view.getScaleType().ordinal()]) {
                case 1:
                    requestOptions = requestOptions.mo46clone().optionalCenterCrop();
                    break;
                case 2:
                    requestOptions = requestOptions.mo46clone().optionalCenterInside();
                    break;
                case 3:
                case 4:
                case 5:
                    requestOptions = requestOptions.mo46clone().optionalFitCenter();
                    break;
                case 6:
                    requestOptions = requestOptions.mo46clone().optionalCenterInside();
                    break;
            }
        }
        return (ViewTarget) into(this.glideContext.buildImageViewTarget(view, this.transcodeClass), null, requestOptions, Executors.mainThreadExecutor());
    }

    @Deprecated
    public FutureTarget<TranscodeType> into(int width, int height) {
        return submit(width, height);
    }

    public FutureTarget<TranscodeType> submit() {
        return submit(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public FutureTarget<TranscodeType> submit(int width, int height) {
        RequestFutureTarget<TranscodeType> target = new RequestFutureTarget<>(width, height);
        return (FutureTarget) into(target, target, Executors.directExecutor());
    }

    public Target<TranscodeType> preload(int width, int height) {
        PreloadTarget<TranscodeType> target = PreloadTarget.obtain(this.requestManager, width, height);
        return into((RequestBuilder<TranscodeType>) target);
    }

    public Target<TranscodeType> preload() {
        return preload(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Deprecated
    public <Y extends Target<File>> Y downloadOnly(Y target) {
        return (Y) getDownloadOnlyRequest().into((RequestBuilder<File>) target);
    }

    @Deprecated
    public FutureTarget<File> downloadOnly(int width, int height) {
        return getDownloadOnlyRequest().submit(width, height);
    }

    protected RequestBuilder<File> getDownloadOnlyRequest() {
        return new RequestBuilder(File.class, this).apply((BaseRequestOptions<?>) DOWNLOAD_ONLY_OPTIONS);
    }

    /* renamed from: com.bumptech.glide.RequestBuilder$1 */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$widget$ImageView$ScaleType;
        static final /* synthetic */ int[] $SwitchMap$com$bumptech$glide$Priority;

        static {
            int[] iArr = new int[Priority.values().length];
            $SwitchMap$com$bumptech$glide$Priority = iArr;
            try {
                iArr[Priority.LOW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.NORMAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.HIGH.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.IMMEDIATE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            int[] iArr2 = new int[ImageView.ScaleType.values().length];
            $SwitchMap$android$widget$ImageView$ScaleType = iArr2;
            try {
                iArr2[ImageView.ScaleType.CENTER_CROP.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER_INSIDE.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_START.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_END.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_XY.ordinal()] = 6;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER.ordinal()] = 7;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.MATRIX.ordinal()] = 8;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private Priority getThumbnailPriority(Priority current) {
        switch (AnonymousClass1.$SwitchMap$com$bumptech$glide$Priority[current.ordinal()]) {
            case 1:
                return Priority.NORMAL;
            case 2:
                return Priority.HIGH;
            case 3:
            case 4:
                return Priority.IMMEDIATE;
            default:
                throw new IllegalArgumentException("unknown priority: " + getPriority());
        }
    }

    private Request buildRequest(Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, BaseRequestOptions<?> requestOptions, Executor callbackExecutor) {
        return buildRequestRecursive(new Object(), target, targetListener, null, this.transitionOptions, requestOptions.getPriority(), requestOptions.getOverrideWidth(), requestOptions.getOverrideHeight(), requestOptions, callbackExecutor);
    }

    private Request buildRequestRecursive(Object requestLock, Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, RequestCoordinator parentCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int overrideWidth, int overrideHeight, BaseRequestOptions<?> requestOptions, Executor callbackExecutor) {
        ErrorRequestCoordinator errorRequestCoordinator;
        RequestCoordinator parentCoordinator2;
        if (this.errorBuilder != null) {
            RequestCoordinator errorRequestCoordinator2 = new ErrorRequestCoordinator(requestLock, parentCoordinator);
            errorRequestCoordinator = errorRequestCoordinator2;
            parentCoordinator2 = errorRequestCoordinator2;
        } else {
            errorRequestCoordinator = null;
            parentCoordinator2 = parentCoordinator;
        }
        Request mainRequest = buildThumbnailRequestRecursive(requestLock, target, targetListener, parentCoordinator2, transitionOptions, priority, overrideWidth, overrideHeight, requestOptions, callbackExecutor);
        if (errorRequestCoordinator == null) {
            return mainRequest;
        }
        int errorOverrideWidth = this.errorBuilder.getOverrideWidth();
        int errorOverrideHeight = this.errorBuilder.getOverrideHeight();
        if (Util.isValidDimensions(overrideWidth, overrideHeight) && !this.errorBuilder.isValidOverride()) {
            errorOverrideWidth = requestOptions.getOverrideWidth();
            errorOverrideHeight = requestOptions.getOverrideHeight();
        }
        RequestBuilder<TranscodeType> requestBuilder = this.errorBuilder;
        ErrorRequestCoordinator errorRequestCoordinator3 = errorRequestCoordinator;
        Request errorRequest = requestBuilder.buildRequestRecursive(requestLock, target, targetListener, errorRequestCoordinator3, requestBuilder.transitionOptions, requestBuilder.getPriority(), errorOverrideWidth, errorOverrideHeight, this.errorBuilder, callbackExecutor);
        errorRequestCoordinator3.setRequests(mainRequest, errorRequest);
        return errorRequestCoordinator3;
    }

    /* JADX WARN: Type inference failed for: r0v6, types: [com.bumptech.glide.request.BaseRequestOptions] */
    private Request buildThumbnailRequestRecursive(Object requestLock, Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, RequestCoordinator parentCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int overrideWidth, int overrideHeight, BaseRequestOptions<?> requestOptions, Executor callbackExecutor) {
        TransitionOptions<?, ? super TranscodeType> thumbTransitionOptions;
        Priority thumbPriority;
        int thumbOverrideWidth;
        int thumbOverrideHeight;
        RequestBuilder<TranscodeType> requestBuilder = this.thumbnailBuilder;
        if (requestBuilder == null) {
            if (this.thumbSizeMultiplier != null) {
                ThumbnailRequestCoordinator coordinator = new ThumbnailRequestCoordinator(requestLock, parentCoordinator);
                Request fullRequest = obtainRequest(requestLock, target, targetListener, requestOptions, coordinator, transitionOptions, priority, overrideWidth, overrideHeight, callbackExecutor);
                BaseRequestOptions<?> thumbnailOptions = requestOptions.mo46clone().sizeMultiplier(this.thumbSizeMultiplier.floatValue());
                Request thumbnailRequest = obtainRequest(requestLock, target, targetListener, thumbnailOptions, coordinator, transitionOptions, getThumbnailPriority(priority), overrideWidth, overrideHeight, callbackExecutor);
                coordinator.setRequests(fullRequest, thumbnailRequest);
                return coordinator;
            }
            return obtainRequest(requestLock, target, targetListener, requestOptions, parentCoordinator, transitionOptions, priority, overrideWidth, overrideHeight, callbackExecutor);
        } else if (this.isThumbnailBuilt) {
            throw new IllegalStateException("You cannot use a request as both the main request and a thumbnail, consider using clone() on the request(s) passed to thumbnail()");
        } else {
            TransitionOptions<?, ? super TranscodeType> thumbTransitionOptions2 = requestBuilder.transitionOptions;
            if (!requestBuilder.isDefaultTransitionOptionsSet) {
                thumbTransitionOptions = thumbTransitionOptions2;
            } else {
                thumbTransitionOptions = transitionOptions;
            }
            if (requestBuilder.isPrioritySet()) {
                thumbPriority = this.thumbnailBuilder.getPriority();
            } else {
                thumbPriority = getThumbnailPriority(priority);
            }
            int thumbOverrideWidth2 = this.thumbnailBuilder.getOverrideWidth();
            int thumbOverrideHeight2 = this.thumbnailBuilder.getOverrideHeight();
            if (Util.isValidDimensions(overrideWidth, overrideHeight) && !this.thumbnailBuilder.isValidOverride()) {
                int thumbOverrideWidth3 = requestOptions.getOverrideWidth();
                int thumbOverrideHeight3 = requestOptions.getOverrideHeight();
                thumbOverrideWidth = thumbOverrideWidth3;
                thumbOverrideHeight = thumbOverrideHeight3;
            } else {
                thumbOverrideWidth = thumbOverrideWidth2;
                thumbOverrideHeight = thumbOverrideHeight2;
            }
            ThumbnailRequestCoordinator coordinator2 = new ThumbnailRequestCoordinator(requestLock, parentCoordinator);
            Request fullRequest2 = obtainRequest(requestLock, target, targetListener, requestOptions, coordinator2, transitionOptions, priority, overrideWidth, overrideHeight, callbackExecutor);
            this.isThumbnailBuilt = true;
            RequestBuilder<TranscodeType> requestBuilder2 = this.thumbnailBuilder;
            Request thumbRequest = requestBuilder2.buildRequestRecursive(requestLock, target, targetListener, coordinator2, thumbTransitionOptions, thumbPriority, thumbOverrideWidth, thumbOverrideHeight, requestBuilder2, callbackExecutor);
            this.isThumbnailBuilt = false;
            coordinator2.setRequests(fullRequest2, thumbRequest);
            return coordinator2;
        }
    }

    private Request obtainRequest(Object requestLock, Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, BaseRequestOptions<?> requestOptions, RequestCoordinator requestCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int overrideWidth, int overrideHeight, Executor callbackExecutor) {
        Context context = this.context;
        GlideContext glideContext = this.glideContext;
        return SingleRequest.obtain(context, glideContext, requestLock, this.model, this.transcodeClass, requestOptions, overrideWidth, overrideHeight, priority, target, targetListener, this.requestListeners, requestCoordinator, glideContext.getEngine(), transitionOptions.getTransitionFactory(), callbackExecutor);
    }
}
