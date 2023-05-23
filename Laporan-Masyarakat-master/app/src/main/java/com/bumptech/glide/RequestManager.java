package com.bumptech.glide;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.manager.ConnectivityMonitor;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.Lifecycle;
import com.bumptech.glide.manager.LifecycleListener;
import com.bumptech.glide.manager.RequestManagerTreeNode;
import com.bumptech.glide.manager.RequestTracker;
import com.bumptech.glide.manager.TargetTracker;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/* loaded from: classes.dex */
public class RequestManager implements ComponentCallbacks2, LifecycleListener, ModelTypes<RequestBuilder<Drawable>> {
    private static final RequestOptions DECODE_TYPE_BITMAP = RequestOptions.decodeTypeOf(Bitmap.class).lock();
    private static final RequestOptions DECODE_TYPE_GIF = RequestOptions.decodeTypeOf(GifDrawable.class).lock();
    private static final RequestOptions DOWNLOAD_ONLY_OPTIONS = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).priority(Priority.LOW).skipMemoryCache(true);
    private final Runnable addSelfToLifecycle;
    private final ConnectivityMonitor connectivityMonitor;
    protected final Context context;
    private final CopyOnWriteArrayList<RequestListener<Object>> defaultRequestListeners;
    protected final Glide glide;
    final Lifecycle lifecycle;
    private boolean pauseAllRequestsOnTrimMemoryModerate;
    private RequestOptions requestOptions;
    private final RequestTracker requestTracker;
    private final TargetTracker targetTracker;
    private final RequestManagerTreeNode treeNode;

    public RequestManager(Glide glide, Lifecycle lifecycle, RequestManagerTreeNode treeNode, Context context) {
        this(glide, lifecycle, treeNode, new RequestTracker(), glide.getConnectivityMonitorFactory(), context);
    }

    RequestManager(Glide glide, Lifecycle lifecycle, RequestManagerTreeNode treeNode, RequestTracker requestTracker, ConnectivityMonitorFactory factory, Context context) {
        this.targetTracker = new TargetTracker();
        Runnable runnable = new Runnable() { // from class: com.bumptech.glide.RequestManager.1
            @Override // java.lang.Runnable
            public void run() {
                RequestManager.this.lifecycle.addListener(RequestManager.this);
            }
        };
        this.addSelfToLifecycle = runnable;
        this.glide = glide;
        this.lifecycle = lifecycle;
        this.treeNode = treeNode;
        this.requestTracker = requestTracker;
        this.context = context;
        ConnectivityMonitor build = factory.build(context.getApplicationContext(), new RequestManagerConnectivityListener(requestTracker));
        this.connectivityMonitor = build;
        if (Util.isOnBackgroundThread()) {
            Util.postOnUiThread(runnable);
        } else {
            lifecycle.addListener(this);
        }
        lifecycle.addListener(build);
        this.defaultRequestListeners = new CopyOnWriteArrayList<>(glide.getGlideContext().getDefaultRequestListeners());
        setRequestOptions(glide.getGlideContext().getDefaultRequestOptions());
        glide.registerRequestManager(this);
    }

    protected synchronized void setRequestOptions(RequestOptions toSet) {
        this.requestOptions = toSet.mo46clone().autoClone();
    }

    private synchronized void updateRequestOptions(RequestOptions toUpdate) {
        this.requestOptions = this.requestOptions.apply(toUpdate);
    }

    public synchronized RequestManager applyDefaultRequestOptions(RequestOptions requestOptions) {
        updateRequestOptions(requestOptions);
        return this;
    }

    public synchronized RequestManager setDefaultRequestOptions(RequestOptions requestOptions) {
        setRequestOptions(requestOptions);
        return this;
    }

    public RequestManager addDefaultRequestListener(RequestListener<Object> requestListener) {
        this.defaultRequestListeners.add(requestListener);
        return this;
    }

    public void setPauseAllRequestsOnTrimMemoryModerate(boolean pauseAllOnTrim) {
        this.pauseAllRequestsOnTrimMemoryModerate = pauseAllOnTrim;
    }

    public synchronized boolean isPaused() {
        return this.requestTracker.isPaused();
    }

    public synchronized void pauseRequests() {
        this.requestTracker.pauseRequests();
    }

    public synchronized void pauseAllRequests() {
        this.requestTracker.pauseAllRequests();
    }

    public synchronized void pauseAllRequestsRecursive() {
        pauseAllRequests();
        for (RequestManager requestManager : this.treeNode.getDescendants()) {
            requestManager.pauseAllRequests();
        }
    }

    public synchronized void pauseRequestsRecursive() {
        pauseRequests();
        for (RequestManager requestManager : this.treeNode.getDescendants()) {
            requestManager.pauseRequests();
        }
    }

    public synchronized void resumeRequests() {
        this.requestTracker.resumeRequests();
    }

    public synchronized void resumeRequestsRecursive() {
        Util.assertMainThread();
        resumeRequests();
        for (RequestManager requestManager : this.treeNode.getDescendants()) {
            requestManager.resumeRequests();
        }
    }

    @Override // com.bumptech.glide.manager.LifecycleListener
    public synchronized void onStart() {
        resumeRequests();
        this.targetTracker.onStart();
    }

    @Override // com.bumptech.glide.manager.LifecycleListener
    public synchronized void onStop() {
        pauseRequests();
        this.targetTracker.onStop();
    }

    @Override // com.bumptech.glide.manager.LifecycleListener
    public synchronized void onDestroy() {
        this.targetTracker.onDestroy();
        for (Target<?> target : this.targetTracker.getAll()) {
            clear(target);
        }
        this.targetTracker.clear();
        this.requestTracker.clearRequests();
        this.lifecycle.removeListener(this);
        this.lifecycle.removeListener(this.connectivityMonitor);
        Util.removeCallbacksOnUiThread(this.addSelfToLifecycle);
        this.glide.unregisterRequestManager(this);
    }

    public RequestBuilder<Bitmap> asBitmap() {
        return as(Bitmap.class).apply((BaseRequestOptions<?>) DECODE_TYPE_BITMAP);
    }

    public RequestBuilder<GifDrawable> asGif() {
        return as(GifDrawable.class).apply((BaseRequestOptions<?>) DECODE_TYPE_GIF);
    }

    public RequestBuilder<Drawable> asDrawable() {
        return as(Drawable.class);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(Bitmap bitmap) {
        return asDrawable().load(bitmap);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(Drawable drawable) {
        return asDrawable().load(drawable);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(String string) {
        return asDrawable().load(string);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(Uri uri) {
        return asDrawable().load(uri);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(File file) {
        return asDrawable().load(file);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(Integer resourceId) {
        return asDrawable().load(resourceId);
    }

    @Override // com.bumptech.glide.ModelTypes
    @Deprecated
    public RequestBuilder<Drawable> load(URL url) {
        return asDrawable().load(url);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(byte[] model) {
        return asDrawable().load(model);
    }

    @Override // com.bumptech.glide.ModelTypes
    public RequestBuilder<Drawable> load(Object model) {
        return asDrawable().load(model);
    }

    public RequestBuilder<File> downloadOnly() {
        return as(File.class).apply((BaseRequestOptions<?>) DOWNLOAD_ONLY_OPTIONS);
    }

    public RequestBuilder<File> download(Object model) {
        return downloadOnly().load(model);
    }

    public RequestBuilder<File> asFile() {
        return as(File.class).apply((BaseRequestOptions<?>) RequestOptions.skipMemoryCacheOf(true));
    }

    public <ResourceType> RequestBuilder<ResourceType> as(Class<ResourceType> resourceClass) {
        return new RequestBuilder<>(this.glide, this, resourceClass, this.context);
    }

    public void clear(View view) {
        clear(new ClearTarget(view));
    }

    public void clear(Target<?> target) {
        if (target == null) {
            return;
        }
        untrackOrDelegate(target);
    }

    private void untrackOrDelegate(Target<?> target) {
        boolean isOwnedByUs = untrack(target);
        Request request = target.getRequest();
        if (!isOwnedByUs && !this.glide.removeFromManagers(target) && request != null) {
            target.setRequest(null);
            request.clear();
        }
    }

    public synchronized boolean untrack(Target<?> target) {
        Request request = target.getRequest();
        if (request == null) {
            return true;
        }
        if (this.requestTracker.clearAndRemove(request)) {
            this.targetTracker.untrack(target);
            target.setRequest(null);
            return true;
        }
        return false;
    }

    public synchronized void track(Target<?> target, Request request) {
        this.targetTracker.track(target);
        this.requestTracker.runRequest(request);
    }

    public List<RequestListener<Object>> getDefaultRequestListeners() {
        return this.defaultRequestListeners;
    }

    public synchronized RequestOptions getDefaultRequestOptions() {
        return this.requestOptions;
    }

    public <T> TransitionOptions<?, T> getDefaultTransitionOptions(Class<T> transcodeClass) {
        return this.glide.getGlideContext().getDefaultTransitionOptions(transcodeClass);
    }

    public synchronized String toString() {
        return super.toString() + "{tracker=" + this.requestTracker + ", treeNode=" + this.treeNode + "}";
    }

    @Override // android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
        if (level == 60 && this.pauseAllRequestsOnTrimMemoryModerate) {
            pauseAllRequestsRecursive();
        }
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
    }

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
    }

    /* loaded from: classes.dex */
    public class RequestManagerConnectivityListener implements ConnectivityMonitor.ConnectivityListener {
        private final RequestTracker requestTracker;

        RequestManagerConnectivityListener(RequestTracker requestTracker) {
            RequestManager.this = r1;
            this.requestTracker = requestTracker;
        }

        @Override // com.bumptech.glide.manager.ConnectivityMonitor.ConnectivityListener
        public void onConnectivityChanged(boolean isConnected) {
            if (isConnected) {
                synchronized (RequestManager.this) {
                    this.requestTracker.restartRequests();
                }
            }
        }
    }

    /* loaded from: classes.dex */
    private static class ClearTarget extends CustomViewTarget<View, Object> {
        ClearTarget(View view) {
            super(view);
        }

        @Override // com.bumptech.glide.request.target.CustomViewTarget
        protected void onResourceCleared(Drawable placeholder) {
        }

        @Override // com.bumptech.glide.request.target.Target
        public void onLoadFailed(Drawable errorDrawable) {
        }

        @Override // com.bumptech.glide.request.target.Target
        public void onResourceReady(Object resource, Transition<? super Object> transition) {
        }
    }
}
