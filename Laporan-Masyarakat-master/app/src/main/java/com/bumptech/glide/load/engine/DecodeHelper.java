package com.bumptech.glide.load.engine;

import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DecodeJob;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.resource.UnitTransformation;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public final class DecodeHelper<Transcode> {
    private DecodeJob.DiskCacheProvider diskCacheProvider;
    private DiskCacheStrategy diskCacheStrategy;
    private GlideContext glideContext;
    private int height;
    private boolean isCacheKeysSet;
    private boolean isLoadDataSet;
    private boolean isScaleOnlyOrNoTransform;
    private boolean isTransformationRequired;
    private Object model;
    private Options options;
    private Priority priority;
    private Class<?> resourceClass;
    private Key signature;
    private Class<Transcode> transcodeClass;
    private Map<Class<?>, Transformation<?>> transformations;
    private int width;
    private final List<ModelLoader.LoadData<?>> loadData = new ArrayList();
    private final List<Key> cacheKeys = new ArrayList();

    /* JADX WARN: Multi-variable type inference failed */
    public <R> void init(GlideContext glideContext, Object model, Key signature, int width, int height, DiskCacheStrategy diskCacheStrategy, Class<?> resourceClass, Class<R> transcodeClass, Priority priority, Options options, Map<Class<?>, Transformation<?>> transformations, boolean isTransformationRequired, boolean isScaleOnlyOrNoTransform, DecodeJob.DiskCacheProvider diskCacheProvider) {
        this.glideContext = glideContext;
        this.model = model;
        this.signature = signature;
        this.width = width;
        this.height = height;
        this.diskCacheStrategy = diskCacheStrategy;
        this.resourceClass = resourceClass;
        this.diskCacheProvider = diskCacheProvider;
        this.transcodeClass = transcodeClass;
        this.priority = priority;
        this.options = options;
        this.transformations = transformations;
        this.isTransformationRequired = isTransformationRequired;
        this.isScaleOnlyOrNoTransform = isScaleOnlyOrNoTransform;
    }

    public void clear() {
        this.glideContext = null;
        this.model = null;
        this.signature = null;
        this.resourceClass = null;
        this.transcodeClass = null;
        this.options = null;
        this.priority = null;
        this.transformations = null;
        this.diskCacheStrategy = null;
        this.loadData.clear();
        this.isLoadDataSet = false;
        this.cacheKeys.clear();
        this.isCacheKeysSet = false;
    }

    public DiskCache getDiskCache() {
        return this.diskCacheProvider.getDiskCache();
    }

    public DiskCacheStrategy getDiskCacheStrategy() {
        return this.diskCacheStrategy;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public Options getOptions() {
        return this.options;
    }

    public Key getSignature() {
        return this.signature;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ArrayPool getArrayPool() {
        return this.glideContext.getArrayPool();
    }

    public Class<?> getTranscodeClass() {
        return (Class<Transcode>) this.transcodeClass;
    }

    public Class<?> getModelClass() {
        return this.model.getClass();
    }

    public List<Class<?>> getRegisteredResourceClasses() {
        return this.glideContext.getRegistry().getRegisteredResourceClasses(this.model.getClass(), this.resourceClass, this.transcodeClass);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean hasLoadPath(Class<?> dataClass) {
        return getLoadPath(dataClass) != null;
    }

    public <Data> LoadPath<Data, ?, Transcode> getLoadPath(Class<Data> dataClass) {
        return this.glideContext.getRegistry().getLoadPath(dataClass, this.resourceClass, this.transcodeClass);
    }

    public boolean isScaleOnlyOrNoTransform() {
        return this.isScaleOnlyOrNoTransform;
    }

    public <Z> Transformation<Z> getTransformation(Class<Z> resourceClass) {
        Transformation<Z> result = (Transformation<Z>) this.transformations.get(resourceClass);
        if (result == null) {
            Iterator<Map.Entry<Class<?>, Transformation<?>>> it = this.transformations.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<Class<?>, Transformation<?>> entry = it.next();
                if (entry.getKey().isAssignableFrom(resourceClass)) {
                    result = (Transformation<Z>) entry.getValue();
                    break;
                }
            }
        }
        if (result == null) {
            if (this.transformations.isEmpty() && this.isTransformationRequired) {
                throw new IllegalArgumentException("Missing transformation for " + resourceClass + ". If you wish to ignore unknown resource types, use the optional transformation methods.");
            }
            return UnitTransformation.get();
        }
        return result;
    }

    public boolean isResourceEncoderAvailable(Resource<?> resource) {
        return this.glideContext.getRegistry().isResourceEncoderAvailable(resource);
    }

    public <Z> ResourceEncoder<Z> getResultEncoder(Resource<Z> resource) {
        return this.glideContext.getRegistry().getResultEncoder(resource);
    }

    public List<ModelLoader<File, ?>> getModelLoaders(File file) throws Registry.NoModelLoaderAvailableException {
        return this.glideContext.getRegistry().getModelLoaders(file);
    }

    public boolean isSourceKey(Key key) {
        List<ModelLoader.LoadData<?>> loadData = getLoadData();
        int size = loadData.size();
        for (int i = 0; i < size; i++) {
            ModelLoader.LoadData<?> current = loadData.get(i);
            if (current.sourceKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public List<ModelLoader.LoadData<?>> getLoadData() {
        if (!this.isLoadDataSet) {
            this.isLoadDataSet = true;
            this.loadData.clear();
            List<ModelLoader<Object, ?>> modelLoaders = this.glideContext.getRegistry().getModelLoaders(this.model);
            int size = modelLoaders.size();
            for (int i = 0; i < size; i++) {
                ModelLoader<Object, ?> modelLoader = modelLoaders.get(i);
                ModelLoader.LoadData<?> current = modelLoader.buildLoadData(this.model, this.width, this.height, this.options);
                if (current != null) {
                    this.loadData.add(current);
                }
            }
        }
        return this.loadData;
    }

    public List<Key> getCacheKeys() {
        if (!this.isCacheKeysSet) {
            this.isCacheKeysSet = true;
            this.cacheKeys.clear();
            List<ModelLoader.LoadData<?>> loadData = getLoadData();
            int size = loadData.size();
            for (int i = 0; i < size; i++) {
                ModelLoader.LoadData<?> data = loadData.get(i);
                if (!this.cacheKeys.contains(data.sourceKey)) {
                    this.cacheKeys.add(data.sourceKey);
                }
                for (int j = 0; j < data.alternateKeys.size(); j++) {
                    if (!this.cacheKeys.contains(data.alternateKeys.get(j))) {
                        this.cacheKeys.add(data.alternateKeys.get(j));
                    }
                }
            }
        }
        return this.cacheKeys;
    }

    public <X> Encoder<X> getSourceEncoder(X data) throws Registry.NoSourceEncoderAvailableException {
        return this.glideContext.getRegistry().getSourceEncoder(data);
    }
}
