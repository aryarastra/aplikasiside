package com.bumptech.glide;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class GlideExperiments {
    private final Map<Class<?>, Experiment> experiments;

    /* loaded from: classes.dex */
    public interface Experiment {
    }

    GlideExperiments(Builder builder) {
        this.experiments = Collections.unmodifiableMap(new HashMap(builder.experiments));
    }

    <T extends Experiment> T get(Class<T> clazz) {
        return (T) this.experiments.get(clazz);
    }

    public boolean isEnabled(Class<? extends Experiment> clazz) {
        return this.experiments.containsKey(clazz);
    }

    /* loaded from: classes.dex */
    public static final class Builder {
        private final Map<Class<?>, Experiment> experiments = new HashMap();

        public Builder update(Experiment experiment, boolean isEnabled) {
            if (isEnabled) {
                add(experiment);
            } else {
                this.experiments.remove(experiment.getClass());
            }
            return this;
        }

        Builder add(Experiment experiment) {
            this.experiments.put(experiment.getClass(), experiment);
            return this;
        }

        public GlideExperiments build() {
            return new GlideExperiments(this);
        }
    }
}
