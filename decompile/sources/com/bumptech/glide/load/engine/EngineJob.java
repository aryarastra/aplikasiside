package com.bumptech.glide.load.engine;

import androidx.core.util.Pools;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DecodeJob;
import com.bumptech.glide.load.engine.EngineResource;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.util.Executors;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.pool.FactoryPools;
import com.bumptech.glide.util.pool.StateVerifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes.dex */
class EngineJob<R> implements DecodeJob.Callback<R>, FactoryPools.Poolable {
    private static final EngineResourceFactory DEFAULT_FACTORY = new EngineResourceFactory();
    private final GlideExecutor animationExecutor;
    final ResourceCallbacksAndExecutors cbs;
    DataSource dataSource;
    private DecodeJob<R> decodeJob;
    private final GlideExecutor diskCacheExecutor;
    private final EngineJobListener engineJobListener;
    EngineResource<?> engineResource;
    private final EngineResourceFactory engineResourceFactory;
    GlideException exception;
    private boolean hasLoadFailed;
    private boolean hasResource;
    private boolean isCacheable;
    private volatile boolean isCancelled;
    private boolean isLoadedFromAlternateCacheKey;
    private Key key;
    private boolean onlyRetrieveFromCache;
    private final AtomicInteger pendingCallbacks;
    private final Pools.Pool<EngineJob<?>> pool;
    private Resource<?> resource;
    private final EngineResource.ResourceListener resourceListener;
    private final GlideExecutor sourceExecutor;
    private final GlideExecutor sourceUnlimitedExecutor;
    private final StateVerifier stateVerifier;
    private boolean useAnimationPool;
    private boolean useUnlimitedSourceGeneratorPool;

    /* JADX INFO: Access modifiers changed from: package-private */
    public EngineJob(GlideExecutor diskCacheExecutor, GlideExecutor sourceExecutor, GlideExecutor sourceUnlimitedExecutor, GlideExecutor animationExecutor, EngineJobListener engineJobListener, EngineResource.ResourceListener resourceListener, Pools.Pool<EngineJob<?>> pool) {
        this(diskCacheExecutor, sourceExecutor, sourceUnlimitedExecutor, animationExecutor, engineJobListener, resourceListener, pool, DEFAULT_FACTORY);
    }

    EngineJob(GlideExecutor diskCacheExecutor, GlideExecutor sourceExecutor, GlideExecutor sourceUnlimitedExecutor, GlideExecutor animationExecutor, EngineJobListener engineJobListener, EngineResource.ResourceListener resourceListener, Pools.Pool<EngineJob<?>> pool, EngineResourceFactory engineResourceFactory) {
        this.cbs = new ResourceCallbacksAndExecutors();
        this.stateVerifier = StateVerifier.newInstance();
        this.pendingCallbacks = new AtomicInteger();
        this.diskCacheExecutor = diskCacheExecutor;
        this.sourceExecutor = sourceExecutor;
        this.sourceUnlimitedExecutor = sourceUnlimitedExecutor;
        this.animationExecutor = animationExecutor;
        this.engineJobListener = engineJobListener;
        this.resourceListener = resourceListener;
        this.pool = pool;
        this.engineResourceFactory = engineResourceFactory;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized EngineJob<R> init(Key key, boolean isCacheable, boolean useUnlimitedSourceGeneratorPool, boolean useAnimationPool, boolean onlyRetrieveFromCache) {
        this.key = key;
        this.isCacheable = isCacheable;
        this.useUnlimitedSourceGeneratorPool = useUnlimitedSourceGeneratorPool;
        this.useAnimationPool = useAnimationPool;
        this.onlyRetrieveFromCache = onlyRetrieveFromCache;
        return this;
    }

    public synchronized void start(DecodeJob<R> decodeJob) {
        this.decodeJob = decodeJob;
        GlideExecutor executor = decodeJob.willDecodeFromCache() ? this.diskCacheExecutor : getActiveSourceExecutor();
        executor.execute(decodeJob);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void addCallback(ResourceCallback cb, Executor callbackExecutor) {
        this.stateVerifier.throwIfRecycled();
        this.cbs.add(cb, callbackExecutor);
        boolean z = true;
        if (this.hasResource) {
            incrementPendingCallbacks(1);
            callbackExecutor.execute(new CallResourceReady(cb));
        } else if (this.hasLoadFailed) {
            incrementPendingCallbacks(1);
            callbackExecutor.execute(new CallLoadFailed(cb));
        } else {
            if (this.isCancelled) {
                z = false;
            }
            Preconditions.checkArgument(z, "Cannot add callbacks to a cancelled EngineJob");
        }
    }

    void callCallbackOnResourceReady(ResourceCallback cb) {
        try {
            cb.onResourceReady(this.engineResource, this.dataSource, this.isLoadedFromAlternateCacheKey);
        } catch (Throwable t) {
            throw new CallbackException(t);
        }
    }

    void callCallbackOnLoadFailed(ResourceCallback cb) {
        try {
            cb.onLoadFailed(this.exception);
        } catch (Throwable t) {
            throw new CallbackException(t);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void removeCallback(ResourceCallback cb) {
        boolean isFinishedRunning;
        this.stateVerifier.throwIfRecycled();
        this.cbs.remove(cb);
        if (this.cbs.isEmpty()) {
            cancel();
            if (!this.hasResource && !this.hasLoadFailed) {
                isFinishedRunning = false;
                if (isFinishedRunning && this.pendingCallbacks.get() == 0) {
                    release();
                }
            }
            isFinishedRunning = true;
            if (isFinishedRunning) {
                release();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onlyRetrieveFromCache() {
        return this.onlyRetrieveFromCache;
    }

    private GlideExecutor getActiveSourceExecutor() {
        if (this.useUnlimitedSourceGeneratorPool) {
            return this.sourceUnlimitedExecutor;
        }
        return this.useAnimationPool ? this.animationExecutor : this.sourceExecutor;
    }

    void cancel() {
        if (isDone()) {
            return;
        }
        this.isCancelled = true;
        this.decodeJob.cancel();
        this.engineJobListener.onEngineJobCancelled(this, this.key);
    }

    synchronized boolean isCancelled() {
        return this.isCancelled;
    }

    private boolean isDone() {
        return this.hasLoadFailed || this.hasResource || this.isCancelled;
    }

    void notifyCallbacksOfResult() {
        synchronized (this) {
            this.stateVerifier.throwIfRecycled();
            if (this.isCancelled) {
                this.resource.recycle();
                release();
            } else if (this.cbs.isEmpty()) {
                throw new IllegalStateException("Received a resource without any callbacks to notify");
            } else {
                if (this.hasResource) {
                    throw new IllegalStateException("Already have resource");
                }
                this.engineResource = this.engineResourceFactory.build(this.resource, this.isCacheable, this.key, this.resourceListener);
                this.hasResource = true;
                ResourceCallbacksAndExecutors copy = this.cbs.copy();
                incrementPendingCallbacks(copy.size() + 1);
                Key localKey = this.key;
                EngineResource<?> localResource = this.engineResource;
                this.engineJobListener.onEngineJobComplete(this, localKey, localResource);
                Iterator<ResourceCallbackAndExecutor> it = copy.iterator();
                while (it.hasNext()) {
                    ResourceCallbackAndExecutor entry = it.next();
                    entry.executor.execute(new CallResourceReady(entry.cb));
                }
                decrementPendingCallbacks();
            }
        }
    }

    synchronized void incrementPendingCallbacks(int count) {
        EngineResource<?> engineResource;
        Preconditions.checkArgument(isDone(), "Not yet complete!");
        if (this.pendingCallbacks.getAndAdd(count) == 0 && (engineResource = this.engineResource) != null) {
            engineResource.acquire();
        }
    }

    void decrementPendingCallbacks() {
        EngineResource<?> toRelease = null;
        synchronized (this) {
            this.stateVerifier.throwIfRecycled();
            Preconditions.checkArgument(isDone(), "Not yet complete!");
            int decremented = this.pendingCallbacks.decrementAndGet();
            Preconditions.checkArgument(decremented >= 0, "Can't decrement below 0");
            if (decremented == 0) {
                toRelease = this.engineResource;
                release();
            }
        }
        if (toRelease != null) {
            toRelease.release();
        }
    }

    private synchronized void release() {
        if (this.key == null) {
            throw new IllegalArgumentException();
        }
        this.cbs.clear();
        this.key = null;
        this.engineResource = null;
        this.resource = null;
        this.hasLoadFailed = false;
        this.isCancelled = false;
        this.hasResource = false;
        this.isLoadedFromAlternateCacheKey = false;
        this.decodeJob.release(false);
        this.decodeJob = null;
        this.exception = null;
        this.dataSource = null;
        this.pool.release(this);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.bumptech.glide.load.engine.DecodeJob.Callback
    public void onResourceReady(Resource<R> resource, DataSource dataSource, boolean isLoadedFromAlternateCacheKey) {
        synchronized (this) {
            this.resource = resource;
            this.dataSource = dataSource;
            this.isLoadedFromAlternateCacheKey = isLoadedFromAlternateCacheKey;
        }
        notifyCallbacksOfResult();
    }

    @Override // com.bumptech.glide.load.engine.DecodeJob.Callback
    public void onLoadFailed(GlideException e) {
        synchronized (this) {
            this.exception = e;
        }
        notifyCallbacksOfException();
    }

    @Override // com.bumptech.glide.load.engine.DecodeJob.Callback
    public void reschedule(DecodeJob<?> job) {
        getActiveSourceExecutor().execute(job);
    }

    void notifyCallbacksOfException() {
        synchronized (this) {
            this.stateVerifier.throwIfRecycled();
            if (this.isCancelled) {
                release();
            } else if (this.cbs.isEmpty()) {
                throw new IllegalStateException("Received an exception without any callbacks to notify");
            } else {
                if (this.hasLoadFailed) {
                    throw new IllegalStateException("Already failed once");
                }
                this.hasLoadFailed = true;
                Key localKey = this.key;
                ResourceCallbacksAndExecutors copy = this.cbs.copy();
                incrementPendingCallbacks(copy.size() + 1);
                this.engineJobListener.onEngineJobComplete(this, localKey, null);
                Iterator<ResourceCallbackAndExecutor> it = copy.iterator();
                while (it.hasNext()) {
                    ResourceCallbackAndExecutor entry = it.next();
                    entry.executor.execute(new CallLoadFailed(entry.cb));
                }
                decrementPendingCallbacks();
            }
        }
    }

    @Override // com.bumptech.glide.util.pool.FactoryPools.Poolable
    public StateVerifier getVerifier() {
        return this.stateVerifier;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CallLoadFailed implements Runnable {
        private final ResourceCallback cb;

        CallLoadFailed(ResourceCallback cb) {
            this.cb = cb;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.cb.getLock()) {
                synchronized (EngineJob.this) {
                    if (EngineJob.this.cbs.contains(this.cb)) {
                        EngineJob.this.callCallbackOnLoadFailed(this.cb);
                    }
                    EngineJob.this.decrementPendingCallbacks();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CallResourceReady implements Runnable {
        private final ResourceCallback cb;

        CallResourceReady(ResourceCallback cb) {
            this.cb = cb;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.cb.getLock()) {
                synchronized (EngineJob.this) {
                    if (EngineJob.this.cbs.contains(this.cb)) {
                        EngineJob.this.engineResource.acquire();
                        EngineJob.this.callCallbackOnResourceReady(this.cb);
                        EngineJob.this.removeCallback(this.cb);
                    }
                    EngineJob.this.decrementPendingCallbacks();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ResourceCallbacksAndExecutors implements Iterable<ResourceCallbackAndExecutor> {
        private final List<ResourceCallbackAndExecutor> callbacksAndExecutors;

        ResourceCallbacksAndExecutors() {
            this(new ArrayList(2));
        }

        ResourceCallbacksAndExecutors(List<ResourceCallbackAndExecutor> callbacksAndExecutors) {
            this.callbacksAndExecutors = callbacksAndExecutors;
        }

        void add(ResourceCallback cb, Executor executor) {
            this.callbacksAndExecutors.add(new ResourceCallbackAndExecutor(cb, executor));
        }

        void remove(ResourceCallback cb) {
            this.callbacksAndExecutors.remove(defaultCallbackAndExecutor(cb));
        }

        boolean contains(ResourceCallback cb) {
            return this.callbacksAndExecutors.contains(defaultCallbackAndExecutor(cb));
        }

        boolean isEmpty() {
            return this.callbacksAndExecutors.isEmpty();
        }

        int size() {
            return this.callbacksAndExecutors.size();
        }

        void clear() {
            this.callbacksAndExecutors.clear();
        }

        ResourceCallbacksAndExecutors copy() {
            return new ResourceCallbacksAndExecutors(new ArrayList(this.callbacksAndExecutors));
        }

        private static ResourceCallbackAndExecutor defaultCallbackAndExecutor(ResourceCallback cb) {
            return new ResourceCallbackAndExecutor(cb, Executors.directExecutor());
        }

        @Override // java.lang.Iterable
        public Iterator<ResourceCallbackAndExecutor> iterator() {
            return this.callbacksAndExecutors.iterator();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ResourceCallbackAndExecutor {
        final ResourceCallback cb;
        final Executor executor;

        ResourceCallbackAndExecutor(ResourceCallback cb, Executor executor) {
            this.cb = cb;
            this.executor = executor;
        }

        public boolean equals(Object o) {
            if (o instanceof ResourceCallbackAndExecutor) {
                ResourceCallbackAndExecutor other = (ResourceCallbackAndExecutor) o;
                return this.cb.equals(other.cb);
            }
            return false;
        }

        public int hashCode() {
            return this.cb.hashCode();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class EngineResourceFactory {
        EngineResourceFactory() {
        }

        public <R> EngineResource<R> build(Resource<R> resource, boolean isMemoryCacheable, Key key, EngineResource.ResourceListener listener) {
            return new EngineResource<>(resource, isMemoryCacheable, true, key, listener);
        }
    }
}
