package com.bumptech.glide.request;

import com.bumptech.glide.request.RequestCoordinator;

/* loaded from: classes.dex */
public class ThumbnailRequestCoordinator implements RequestCoordinator, Request {
    private volatile Request full;
    private boolean isRunningDuringBegin;
    private final RequestCoordinator parent;
    private final Object requestLock;
    private volatile Request thumb;
    private RequestCoordinator.RequestState fullState = RequestCoordinator.RequestState.CLEARED;
    private RequestCoordinator.RequestState thumbState = RequestCoordinator.RequestState.CLEARED;

    public ThumbnailRequestCoordinator(Object requestLock, RequestCoordinator parent) {
        this.requestLock = requestLock;
        this.parent = parent;
    }

    public void setRequests(Request full, Request thumb) {
        this.full = full;
        this.thumb = thumb;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public boolean canSetImage(Request request) {
        boolean z;
        synchronized (this.requestLock) {
            z = parentCanSetImage() && (request.equals(this.full) || this.fullState != RequestCoordinator.RequestState.SUCCESS);
        }
        return z;
    }

    private boolean parentCanSetImage() {
        RequestCoordinator requestCoordinator = this.parent;
        return requestCoordinator == null || requestCoordinator.canSetImage(this);
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public boolean canNotifyStatusChanged(Request request) {
        boolean z;
        synchronized (this.requestLock) {
            z = parentCanNotifyStatusChanged() && request.equals(this.full) && !isAnyResourceSet();
        }
        return z;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public boolean canNotifyCleared(Request request) {
        boolean z;
        synchronized (this.requestLock) {
            z = parentCanNotifyCleared() && request.equals(this.full) && this.fullState != RequestCoordinator.RequestState.PAUSED;
        }
        return z;
    }

    private boolean parentCanNotifyCleared() {
        RequestCoordinator requestCoordinator = this.parent;
        return requestCoordinator == null || requestCoordinator.canNotifyCleared(this);
    }

    private boolean parentCanNotifyStatusChanged() {
        RequestCoordinator requestCoordinator = this.parent;
        return requestCoordinator == null || requestCoordinator.canNotifyStatusChanged(this);
    }

    @Override // com.bumptech.glide.request.RequestCoordinator, com.bumptech.glide.request.Request
    public boolean isAnyResourceSet() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.thumb.isAnyResourceSet() || this.full.isAnyResourceSet();
        }
        return z;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public void onRequestSuccess(Request request) {
        synchronized (this.requestLock) {
            if (request.equals(this.thumb)) {
                this.thumbState = RequestCoordinator.RequestState.SUCCESS;
                return;
            }
            this.fullState = RequestCoordinator.RequestState.SUCCESS;
            RequestCoordinator requestCoordinator = this.parent;
            if (requestCoordinator != null) {
                requestCoordinator.onRequestSuccess(this);
            }
            if (!this.thumbState.isComplete()) {
                this.thumb.clear();
            }
        }
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public void onRequestFailed(Request request) {
        synchronized (this.requestLock) {
            if (!request.equals(this.full)) {
                this.thumbState = RequestCoordinator.RequestState.FAILED;
                return;
            }
            this.fullState = RequestCoordinator.RequestState.FAILED;
            RequestCoordinator requestCoordinator = this.parent;
            if (requestCoordinator != null) {
                requestCoordinator.onRequestFailed(this);
            }
        }
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public RequestCoordinator getRoot() {
        RequestCoordinator root;
        synchronized (this.requestLock) {
            RequestCoordinator requestCoordinator = this.parent;
            root = requestCoordinator != null ? requestCoordinator.getRoot() : this;
        }
        return root;
    }

    @Override // com.bumptech.glide.request.Request
    public void begin() {
        synchronized (this.requestLock) {
            this.isRunningDuringBegin = true;
            if (this.fullState != RequestCoordinator.RequestState.SUCCESS && this.thumbState != RequestCoordinator.RequestState.RUNNING) {
                this.thumbState = RequestCoordinator.RequestState.RUNNING;
                this.thumb.begin();
            }
            if (this.isRunningDuringBegin && this.fullState != RequestCoordinator.RequestState.RUNNING) {
                this.fullState = RequestCoordinator.RequestState.RUNNING;
                this.full.begin();
            }
            this.isRunningDuringBegin = false;
        }
    }

    @Override // com.bumptech.glide.request.Request
    public void clear() {
        synchronized (this.requestLock) {
            this.isRunningDuringBegin = false;
            this.fullState = RequestCoordinator.RequestState.CLEARED;
            this.thumbState = RequestCoordinator.RequestState.CLEARED;
            this.thumb.clear();
            this.full.clear();
        }
    }

    @Override // com.bumptech.glide.request.Request
    public void pause() {
        synchronized (this.requestLock) {
            if (!this.thumbState.isComplete()) {
                this.thumbState = RequestCoordinator.RequestState.PAUSED;
                this.thumb.pause();
            }
            if (!this.fullState.isComplete()) {
                this.fullState = RequestCoordinator.RequestState.PAUSED;
                this.full.pause();
            }
        }
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isRunning() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.fullState == RequestCoordinator.RequestState.RUNNING;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isComplete() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.fullState == RequestCoordinator.RequestState.SUCCESS;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isCleared() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.fullState == RequestCoordinator.RequestState.CLEARED;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isEquivalentTo(Request o) {
        if (o instanceof ThumbnailRequestCoordinator) {
            ThumbnailRequestCoordinator that = (ThumbnailRequestCoordinator) o;
            if (this.full == null) {
                if (that.full != null) {
                    return false;
                }
            } else if (!this.full.isEquivalentTo(that.full)) {
                return false;
            }
            if (this.thumb == null) {
                if (that.thumb != null) {
                    return false;
                }
            } else if (!this.thumb.isEquivalentTo(that.thumb)) {
                return false;
            }
            return true;
        }
        return false;
    }
}
