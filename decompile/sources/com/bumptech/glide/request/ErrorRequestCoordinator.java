package com.bumptech.glide.request;

import com.bumptech.glide.request.RequestCoordinator;

/* loaded from: classes.dex */
public final class ErrorRequestCoordinator implements RequestCoordinator, Request {
    private volatile Request error;
    private final RequestCoordinator parent;
    private volatile Request primary;
    private final Object requestLock;
    private RequestCoordinator.RequestState primaryState = RequestCoordinator.RequestState.CLEARED;
    private RequestCoordinator.RequestState errorState = RequestCoordinator.RequestState.CLEARED;

    public ErrorRequestCoordinator(Object requestLock, RequestCoordinator parent) {
        this.requestLock = requestLock;
        this.parent = parent;
    }

    public void setRequests(Request primary, Request error) {
        this.primary = primary;
        this.error = error;
    }

    @Override // com.bumptech.glide.request.Request
    public void begin() {
        synchronized (this.requestLock) {
            if (this.primaryState != RequestCoordinator.RequestState.RUNNING) {
                this.primaryState = RequestCoordinator.RequestState.RUNNING;
                this.primary.begin();
            }
        }
    }

    @Override // com.bumptech.glide.request.Request
    public void clear() {
        synchronized (this.requestLock) {
            this.primaryState = RequestCoordinator.RequestState.CLEARED;
            this.primary.clear();
            if (this.errorState != RequestCoordinator.RequestState.CLEARED) {
                this.errorState = RequestCoordinator.RequestState.CLEARED;
                this.error.clear();
            }
        }
    }

    @Override // com.bumptech.glide.request.Request
    public void pause() {
        synchronized (this.requestLock) {
            if (this.primaryState == RequestCoordinator.RequestState.RUNNING) {
                this.primaryState = RequestCoordinator.RequestState.PAUSED;
                this.primary.pause();
            }
            if (this.errorState == RequestCoordinator.RequestState.RUNNING) {
                this.errorState = RequestCoordinator.RequestState.PAUSED;
                this.error.pause();
            }
        }
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isRunning() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.primaryState == RequestCoordinator.RequestState.RUNNING || this.errorState == RequestCoordinator.RequestState.RUNNING;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isComplete() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.primaryState == RequestCoordinator.RequestState.SUCCESS || this.errorState == RequestCoordinator.RequestState.SUCCESS;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isCleared() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.primaryState == RequestCoordinator.RequestState.CLEARED && this.errorState == RequestCoordinator.RequestState.CLEARED;
        }
        return z;
    }

    @Override // com.bumptech.glide.request.Request
    public boolean isEquivalentTo(Request o) {
        if (o instanceof ErrorRequestCoordinator) {
            ErrorRequestCoordinator other = (ErrorRequestCoordinator) o;
            return this.primary.isEquivalentTo(other.primary) && this.error.isEquivalentTo(other.error);
        }
        return false;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public boolean canSetImage(Request request) {
        boolean z;
        synchronized (this.requestLock) {
            z = parentCanSetImage() && isValidRequest(request);
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
            z = parentCanNotifyStatusChanged() && isValidRequest(request);
        }
        return z;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public boolean canNotifyCleared(Request request) {
        boolean z;
        synchronized (this.requestLock) {
            z = parentCanNotifyCleared() && isValidRequest(request);
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

    private boolean isValidRequest(Request request) {
        return request.equals(this.primary) || (this.primaryState == RequestCoordinator.RequestState.FAILED && request.equals(this.error));
    }

    @Override // com.bumptech.glide.request.RequestCoordinator, com.bumptech.glide.request.Request
    public boolean isAnyResourceSet() {
        boolean z;
        synchronized (this.requestLock) {
            z = this.primary.isAnyResourceSet() || this.error.isAnyResourceSet();
        }
        return z;
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public void onRequestSuccess(Request request) {
        synchronized (this.requestLock) {
            if (request.equals(this.primary)) {
                this.primaryState = RequestCoordinator.RequestState.SUCCESS;
            } else if (request.equals(this.error)) {
                this.errorState = RequestCoordinator.RequestState.SUCCESS;
            }
            RequestCoordinator requestCoordinator = this.parent;
            if (requestCoordinator != null) {
                requestCoordinator.onRequestSuccess(this);
            }
        }
    }

    @Override // com.bumptech.glide.request.RequestCoordinator
    public void onRequestFailed(Request request) {
        synchronized (this.requestLock) {
            if (!request.equals(this.error)) {
                this.primaryState = RequestCoordinator.RequestState.FAILED;
                if (this.errorState != RequestCoordinator.RequestState.RUNNING) {
                    this.errorState = RequestCoordinator.RequestState.RUNNING;
                    this.error.begin();
                }
                return;
            }
            this.errorState = RequestCoordinator.RequestState.FAILED;
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
}
