package com.google.android.material.button;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/* loaded from: classes.dex */
public class MaterialButtonToggleGroup extends LinearLayout {
    private int checkedId;
    private final CheckedStateTracker checkedStateTracker;
    private Integer[] childOrder;
    private final Comparator<MaterialButton> childOrderComparator;
    private final LinkedHashSet<OnButtonCheckedListener> onButtonCheckedListeners;
    private final List<CornerData> originalCornerData;
    private final PressedStateTracker pressedStateTracker;
    private boolean selectionRequired;
    private boolean singleSelection;
    private boolean skipCheckedStateTracker;
    private static final String LOG_TAG = MaterialButtonToggleGroup.class.getSimpleName();
    private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_MaterialButtonToggleGroup;

    /* loaded from: classes.dex */
    public interface OnButtonCheckedListener {
        void onButtonChecked(MaterialButtonToggleGroup materialButtonToggleGroup, int i, boolean z);
    }

    public MaterialButtonToggleGroup(Context context) {
        this(context, null);
    }

    public MaterialButtonToggleGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.materialButtonToggleGroupStyle);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public MaterialButtonToggleGroup(android.content.Context r8, android.util.AttributeSet r9, int r10) {
        /*
            r7 = this;
            int r4 = com.google.android.material.button.MaterialButtonToggleGroup.DEF_STYLE_RES
            android.content.Context r0 = com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap(r8, r9, r10, r4)
            r7.<init>(r0, r9, r10)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r7.originalCornerData = r0
            com.google.android.material.button.MaterialButtonToggleGroup$CheckedStateTracker r0 = new com.google.android.material.button.MaterialButtonToggleGroup$CheckedStateTracker
            r1 = 0
            r0.<init>()
            r7.checkedStateTracker = r0
            com.google.android.material.button.MaterialButtonToggleGroup$PressedStateTracker r0 = new com.google.android.material.button.MaterialButtonToggleGroup$PressedStateTracker
            r0.<init>()
            r7.pressedStateTracker = r0
            java.util.LinkedHashSet r0 = new java.util.LinkedHashSet
            r0.<init>()
            r7.onButtonCheckedListeners = r0
            com.google.android.material.button.MaterialButtonToggleGroup$1 r0 = new com.google.android.material.button.MaterialButtonToggleGroup$1
            r0.<init>()
            r7.childOrderComparator = r0
            r6 = 0
            r7.skipCheckedStateTracker = r6
            android.content.Context r8 = r7.getContext()
            int[] r2 = com.google.android.material.R.styleable.MaterialButtonToggleGroup
            int[] r5 = new int[r6]
            r0 = r8
            r1 = r9
            r3 = r10
            android.content.res.TypedArray r0 = com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes(r0, r1, r2, r3, r4, r5)
            int r1 = com.google.android.material.R.styleable.MaterialButtonToggleGroup_singleSelection
            boolean r1 = r0.getBoolean(r1, r6)
            r7.setSingleSelection(r1)
            int r1 = com.google.android.material.R.styleable.MaterialButtonToggleGroup_checkedButton
            r2 = -1
            int r1 = r0.getResourceId(r1, r2)
            r7.checkedId = r1
            int r1 = com.google.android.material.R.styleable.MaterialButtonToggleGroup_selectionRequired
            boolean r1 = r0.getBoolean(r1, r6)
            r7.selectionRequired = r1
            r1 = 1
            r7.setChildrenDrawingOrderEnabled(r1)
            r0.recycle()
            androidx.core.view.ViewCompat.setImportantForAccessibility(r7, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.button.MaterialButtonToggleGroup.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        int i = this.checkedId;
        if (i != -1) {
            checkForced(i, true);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        updateChildOrder();
        super.dispatchDraw(canvas);
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof MaterialButton)) {
            Log.e(LOG_TAG, "Child views must be of type MaterialButton.");
            return;
        }
        super.addView(child, index, params);
        MaterialButton buttonChild = (MaterialButton) child;
        setGeneratedIdIfNeeded(buttonChild);
        setupButtonChild(buttonChild);
        if (buttonChild.isChecked()) {
            updateCheckedStates(buttonChild.getId(), true);
            setCheckedId(buttonChild.getId());
        }
        ShapeAppearanceModel shapeAppearanceModel = buttonChild.getShapeAppearanceModel();
        this.originalCornerData.add(new CornerData(shapeAppearanceModel.getTopLeftCornerSize(), shapeAppearanceModel.getBottomLeftCornerSize(), shapeAppearanceModel.getTopRightCornerSize(), shapeAppearanceModel.getBottomRightCornerSize()));
        ViewCompat.setAccessibilityDelegate(buttonChild, new AccessibilityDelegateCompat() { // from class: com.google.android.material.button.MaterialButtonToggleGroup.2
            @Override // androidx.core.view.AccessibilityDelegateCompat
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(0, 1, MaterialButtonToggleGroup.this.getIndexWithinVisibleButtons(host), 1, false, ((MaterialButton) host).isChecked()));
            }
        });
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof MaterialButton) {
            ((MaterialButton) child).removeOnCheckedChangeListener(this.checkedStateTracker);
            ((MaterialButton) child).setOnPressedChangeListenerInternal(null);
        }
        int indexOfChild = indexOfChild(child);
        if (indexOfChild >= 0) {
            this.originalCornerData.remove(indexOfChild);
        }
        updateChildShapes();
        adjustChildMarginsAndUpdateLayout();
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateChildShapes();
        adjustChildMarginsAndUpdateLayout();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return MaterialButtonToggleGroup.class.getName();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
        infoCompat.setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(1, getVisibleButtonCount(), false, isSingleSelection() ? 1 : 2));
    }

    public void check(int id) {
        if (id == this.checkedId) {
            return;
        }
        checkForced(id, true);
    }

    public void uncheck(int id) {
        checkForced(id, false);
    }

    public void clearChecked() {
        this.skipCheckedStateTracker = true;
        for (int i = 0; i < getChildCount(); i++) {
            MaterialButton child = getChildButton(i);
            child.setChecked(false);
            dispatchOnButtonChecked(child.getId(), false);
        }
        this.skipCheckedStateTracker = false;
        setCheckedId(-1);
    }

    public int getCheckedButtonId() {
        if (this.singleSelection) {
            return this.checkedId;
        }
        return -1;
    }

    public List<Integer> getCheckedButtonIds() {
        List<Integer> checkedIds = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            MaterialButton child = getChildButton(i);
            if (child.isChecked()) {
                checkedIds.add(Integer.valueOf(child.getId()));
            }
        }
        return checkedIds;
    }

    public void addOnButtonCheckedListener(OnButtonCheckedListener listener) {
        this.onButtonCheckedListeners.add(listener);
    }

    public void removeOnButtonCheckedListener(OnButtonCheckedListener listener) {
        this.onButtonCheckedListeners.remove(listener);
    }

    public void clearOnButtonCheckedListeners() {
        this.onButtonCheckedListeners.clear();
    }

    public boolean isSingleSelection() {
        return this.singleSelection;
    }

    public void setSingleSelection(boolean singleSelection) {
        if (this.singleSelection != singleSelection) {
            this.singleSelection = singleSelection;
            clearChecked();
        }
    }

    public void setSelectionRequired(boolean selectionRequired) {
        this.selectionRequired = selectionRequired;
    }

    public boolean isSelectionRequired() {
        return this.selectionRequired;
    }

    public void setSingleSelection(int id) {
        setSingleSelection(getResources().getBoolean(id));
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView instanceof MaterialButton) {
            this.skipCheckedStateTracker = true;
            ((MaterialButton) checkedView).setChecked(checked);
            this.skipCheckedStateTracker = false;
        }
    }

    private void setCheckedId(int checkedId) {
        this.checkedId = checkedId;
        dispatchOnButtonChecked(checkedId, true);
    }

    private void adjustChildMarginsAndUpdateLayout() {
        int firstVisibleChildIndex = getFirstVisibleChildIndex();
        if (firstVisibleChildIndex == -1) {
            return;
        }
        for (int i = firstVisibleChildIndex + 1; i < getChildCount(); i++) {
            MaterialButton currentButton = getChildButton(i);
            MaterialButton previousButton = getChildButton(i - 1);
            int smallestStrokeWidth = Math.min(currentButton.getStrokeWidth(), previousButton.getStrokeWidth());
            LinearLayout.LayoutParams params = buildLayoutParams(currentButton);
            if (getOrientation() == 0) {
                MarginLayoutParamsCompat.setMarginEnd(params, 0);
                MarginLayoutParamsCompat.setMarginStart(params, -smallestStrokeWidth);
                params.topMargin = 0;
            } else {
                params.bottomMargin = 0;
                params.topMargin = -smallestStrokeWidth;
                MarginLayoutParamsCompat.setMarginStart(params, 0);
            }
            currentButton.setLayoutParams(params);
        }
        resetChildMargins(firstVisibleChildIndex);
    }

    private MaterialButton getChildButton(int index) {
        return (MaterialButton) getChildAt(index);
    }

    private void resetChildMargins(int childIndex) {
        if (getChildCount() == 0 || childIndex == -1) {
            return;
        }
        MaterialButton currentButton = getChildButton(childIndex);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) currentButton.getLayoutParams();
        if (getOrientation() == 1) {
            params.topMargin = 0;
            params.bottomMargin = 0;
            return;
        }
        MarginLayoutParamsCompat.setMarginEnd(params, 0);
        MarginLayoutParamsCompat.setMarginStart(params, 0);
        params.leftMargin = 0;
        params.rightMargin = 0;
    }

    void updateChildShapes() {
        int childCount = getChildCount();
        int firstVisibleChildIndex = getFirstVisibleChildIndex();
        int lastVisibleChildIndex = getLastVisibleChildIndex();
        for (int i = 0; i < childCount; i++) {
            MaterialButton button = getChildButton(i);
            if (button.getVisibility() != 8) {
                ShapeAppearanceModel.Builder builder = button.getShapeAppearanceModel().toBuilder();
                CornerData newCornerData = getNewCornerData(i, firstVisibleChildIndex, lastVisibleChildIndex);
                updateBuilderWithCornerData(builder, newCornerData);
                button.setShapeAppearanceModel(builder.build());
            }
        }
    }

    private int getFirstVisibleChildIndex() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (isChildVisible(i)) {
                return i;
            }
        }
        return -1;
    }

    private int getLastVisibleChildIndex() {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            if (isChildVisible(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isChildVisible(int i) {
        View child = getChildAt(i);
        return child.getVisibility() != 8;
    }

    private int getVisibleButtonCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if ((getChildAt(i) instanceof MaterialButton) && isChildVisible(i)) {
                count++;
            }
        }
        return count;
    }

    public int getIndexWithinVisibleButtons(View child) {
        if (child instanceof MaterialButton) {
            int index = 0;
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) == child) {
                    return index;
                }
                if ((getChildAt(i) instanceof MaterialButton) && isChildVisible(i)) {
                    index++;
                }
            }
            return -1;
        }
        return -1;
    }

    private CornerData getNewCornerData(int index, int firstVisibleChildIndex, int lastVisibleChildIndex) {
        CornerData cornerData = this.originalCornerData.get(index);
        if (firstVisibleChildIndex == lastVisibleChildIndex) {
            return cornerData;
        }
        boolean isHorizontal = getOrientation() == 0;
        if (index == firstVisibleChildIndex) {
            return isHorizontal ? CornerData.start(cornerData, this) : CornerData.top(cornerData);
        } else if (index == lastVisibleChildIndex) {
            return isHorizontal ? CornerData.end(cornerData, this) : CornerData.bottom(cornerData);
        } else {
            return null;
        }
    }

    private static void updateBuilderWithCornerData(ShapeAppearanceModel.Builder shapeAppearanceModelBuilder, CornerData cornerData) {
        if (cornerData == null) {
            shapeAppearanceModelBuilder.setAllCornerSizes(0.0f);
        } else {
            shapeAppearanceModelBuilder.setTopLeftCornerSize(cornerData.topLeft).setBottomLeftCornerSize(cornerData.bottomLeft).setTopRightCornerSize(cornerData.topRight).setBottomRightCornerSize(cornerData.bottomRight);
        }
    }

    public boolean updateCheckedStates(int childId, boolean childIsChecked) {
        List<Integer> checkedButtonIds = getCheckedButtonIds();
        if (this.selectionRequired && checkedButtonIds.isEmpty()) {
            setCheckedStateForView(childId, true);
            this.checkedId = childId;
            return false;
        }
        if (childIsChecked && this.singleSelection) {
            checkedButtonIds.remove(Integer.valueOf(childId));
            for (Integer num : checkedButtonIds) {
                int buttonId = num.intValue();
                setCheckedStateForView(buttonId, false);
                dispatchOnButtonChecked(buttonId, false);
            }
        }
        return true;
    }

    public void dispatchOnButtonChecked(int buttonId, boolean checked) {
        Iterator<OnButtonCheckedListener> it = this.onButtonCheckedListeners.iterator();
        while (it.hasNext()) {
            OnButtonCheckedListener listener = it.next();
            listener.onButtonChecked(this, buttonId, checked);
        }
    }

    private void checkForced(int checkedId, boolean checked) {
        MaterialButton button = (MaterialButton) findViewById(checkedId);
        if (button != null) {
            button.setChecked(checked);
        }
    }

    private void setGeneratedIdIfNeeded(MaterialButton materialButton) {
        if (materialButton.getId() == -1) {
            materialButton.setId(ViewCompat.generateViewId());
        }
    }

    private void setupButtonChild(MaterialButton buttonChild) {
        buttonChild.setMaxLines(1);
        buttonChild.setEllipsize(TextUtils.TruncateAt.END);
        buttonChild.setCheckable(true);
        buttonChild.addOnCheckedChangeListener(this.checkedStateTracker);
        buttonChild.setOnPressedChangeListenerInternal(this.pressedStateTracker);
        buttonChild.setShouldDrawSurfaceColorStroke(true);
    }

    private LinearLayout.LayoutParams buildLayoutParams(View child) {
        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            return (LinearLayout.LayoutParams) layoutParams;
        }
        return new LinearLayout.LayoutParams(layoutParams.width, layoutParams.height);
    }

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int childCount, int i) {
        Integer[] numArr = this.childOrder;
        if (numArr == null || i >= numArr.length) {
            Log.w(LOG_TAG, "Child order wasn't updated");
            return i;
        }
        return numArr[i].intValue();
    }

    private void updateChildOrder() {
        SortedMap<MaterialButton, Integer> viewToIndexMap = new TreeMap<>(this.childOrderComparator);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            viewToIndexMap.put(getChildButton(i), Integer.valueOf(i));
        }
        this.childOrder = (Integer[]) viewToIndexMap.values().toArray(new Integer[0]);
    }

    /* loaded from: classes.dex */
    public class CheckedStateTracker implements MaterialButton.OnCheckedChangeListener {
        private CheckedStateTracker() {
            MaterialButtonToggleGroup.this = r1;
        }

        @Override // com.google.android.material.button.MaterialButton.OnCheckedChangeListener
        public void onCheckedChanged(MaterialButton button, boolean isChecked) {
            if (!MaterialButtonToggleGroup.this.skipCheckedStateTracker) {
                if (MaterialButtonToggleGroup.this.singleSelection) {
                    MaterialButtonToggleGroup.this.checkedId = isChecked ? button.getId() : -1;
                }
                boolean buttonCheckedStateChanged = MaterialButtonToggleGroup.this.updateCheckedStates(button.getId(), isChecked);
                if (buttonCheckedStateChanged) {
                    MaterialButtonToggleGroup.this.dispatchOnButtonChecked(button.getId(), button.isChecked());
                }
                MaterialButtonToggleGroup.this.invalidate();
            }
        }
    }

    /* loaded from: classes.dex */
    public class PressedStateTracker implements MaterialButton.OnPressedChangeListener {
        private PressedStateTracker() {
            MaterialButtonToggleGroup.this = r1;
        }

        @Override // com.google.android.material.button.MaterialButton.OnPressedChangeListener
        public void onPressedChanged(MaterialButton button, boolean isPressed) {
            MaterialButtonToggleGroup.this.invalidate();
        }
    }

    /* loaded from: classes.dex */
    public static class CornerData {
        private static final CornerSize noCorner = new AbsoluteCornerSize(0.0f);
        CornerSize bottomLeft;
        CornerSize bottomRight;
        CornerSize topLeft;
        CornerSize topRight;

        CornerData(CornerSize topLeft, CornerSize bottomLeft, CornerSize topRight, CornerSize bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomRight = bottomRight;
            this.bottomLeft = bottomLeft;
        }

        public static CornerData start(CornerData orig, View view) {
            return ViewUtils.isLayoutRtl(view) ? right(orig) : left(orig);
        }

        public static CornerData end(CornerData orig, View view) {
            return ViewUtils.isLayoutRtl(view) ? left(orig) : right(orig);
        }

        public static CornerData left(CornerData orig) {
            CornerSize cornerSize = orig.topLeft;
            CornerSize cornerSize2 = orig.bottomLeft;
            CornerSize cornerSize3 = noCorner;
            return new CornerData(cornerSize, cornerSize2, cornerSize3, cornerSize3);
        }

        public static CornerData right(CornerData orig) {
            CornerSize cornerSize = noCorner;
            return new CornerData(cornerSize, cornerSize, orig.topRight, orig.bottomRight);
        }

        public static CornerData top(CornerData orig) {
            CornerSize cornerSize = orig.topLeft;
            CornerSize cornerSize2 = noCorner;
            return new CornerData(cornerSize, cornerSize2, orig.topRight, cornerSize2);
        }

        public static CornerData bottom(CornerData orig) {
            CornerSize cornerSize = noCorner;
            return new CornerData(cornerSize, orig.bottomLeft, cornerSize, orig.bottomRight);
        }
    }
}
