package com.google.android.material.datepicker;

import androidx.fragment.app.Fragment;
import java.util.LinkedHashSet;

/* loaded from: classes.dex */
public abstract class PickerFragment<S> extends Fragment {
    protected final LinkedHashSet<OnSelectionChangedListener<S>> onSelectionChangedListeners = new LinkedHashSet<>();

    abstract DateSelector<S> getDateSelector();

    public boolean addOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
        return this.onSelectionChangedListeners.add(listener);
    }

    boolean removeOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
        return this.onSelectionChangedListeners.remove(listener);
    }

    public void clearOnSelectionChangedListeners() {
        this.onSelectionChangedListeners.clear();
    }
}
