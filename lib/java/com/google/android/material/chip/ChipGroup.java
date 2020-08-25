/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.chip;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import androidx.annotation.BoolRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.FlowLayout;
import com.google.android.material.internal.ThemeEnforcement;
import java.util.ArrayList;
import java.util.List;

/**
 * A ChipGroup is used to hold multiple {@link Chip}s. By default, the chips are reflowed across
 * multiple lines. Set the {@link R.attr#singleLine app:singleLine} attribute to constrain the chips
 * to a single horizontal line. If you do so, you'll usually want to wrap this ChipGroup in a {@link
 * android.widget.HorizontalScrollView}.
 *
 * <p>ChipGroup also supports a multiple-exclusion scope for a set of chips. When you set the {@link
 * R.attr#singleSelection app:singleSelection} attribute, checking one chip that belongs to a chip
 * group unchecks any previously checked chip within the same group. The behavior mirrors that of
 * {@link android.widget.RadioGroup}.
 */
public class ChipGroup extends FlowLayout {

  /**
   * Interface definition for a callback to be invoked when the checked chip changed in this group.
   */
  public interface OnCheckedChangeListener {
    /**
     * Called when the checked chip has changed. When the selection is cleared, checkedId is {@link
     * View#NO_ID}.
     *
     * @param group the group in which the checked chip has changed
     * @param checkedId the unique identifier of the newly checked chip
     */
    public void onCheckedChanged(ChipGroup group, @IdRes int checkedId);
  }

  /** A {@link ChipGroup.LayoutParams} implementation for {@link ChipGroup}. */
  public static class LayoutParams extends MarginLayoutParams {
    public LayoutParams(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }
  }

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ChipGroup;

  @Dimension private int chipSpacingHorizontal;
  @Dimension private int chipSpacingVertical;
  private boolean singleSelection;
  private boolean selectionRequired;

  @Nullable private OnCheckedChangeListener onCheckedChangeListener;

  private final CheckedStateTracker checkedStateTracker = new CheckedStateTracker();

  @NonNull
  private PassThroughHierarchyChangeListener passThroughListener =
      new PassThroughHierarchyChangeListener();

  @IdRes private int checkedId = View.NO_ID;
  private boolean protectFromCheckedChange = false;

  public ChipGroup(Context context) {
    this(context, null);
  }

  public ChipGroup(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipGroupStyle);
  }

  public ChipGroup(Context context, AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.ChipGroup, defStyleAttr, DEF_STYLE_RES);

    int chipSpacing = a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacing, 0);
    setChipSpacingHorizontal(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingHorizontal, chipSpacing));
    setChipSpacingVertical(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingVertical, chipSpacing));
    setSingleLine(a.getBoolean(R.styleable.ChipGroup_singleLine, false));
    setSingleSelection(a.getBoolean(R.styleable.ChipGroup_singleSelection, false));
    setSelectionRequired(a.getBoolean(R.styleable.ChipGroup_selectionRequired, false));
    int checkedChip = a.getResourceId(R.styleable.ChipGroup_checkedChip, View.NO_ID);
    if (checkedChip != View.NO_ID) {
      checkedId = checkedChip;
    }

    a.recycle();
    super.setOnHierarchyChangeListener(passThroughListener);

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    // -1 for an unknown number of columns in a reflowing layout
    int columnCount = isSingleLine() ? getChipCount() : -1;
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
            /* rowCount= */ getRowCount(),
            /* columnCount= */ columnCount,
            /* hierarchical= */ false,
            /* selectionMode = */ isSingleSelection()
                ? CollectionInfoCompat.SELECTION_MODE_SINGLE
                : CollectionInfoCompat.SELECTION_MODE_MULTIPLE));
  }

  @NonNull
  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new ChipGroup.LayoutParams(getContext(), attrs);
  }

  @NonNull
  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    return new ChipGroup.LayoutParams(lp);
  }

  @NonNull
  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new ChipGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p) && (p instanceof ChipGroup.LayoutParams);
  }

  @Override
  public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
    // the user listener is delegated to our pass-through listener
    passThroughListener.onHierarchyChangeListener = listener;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    // checks the appropriate chip as requested in the XML file
    if (checkedId != View.NO_ID) {
      setCheckedStateForView(checkedId, true);
      setCheckedId(checkedId);
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (child instanceof Chip) {
      final Chip chip = (Chip) child;
      if (chip.isChecked()) {
        if (checkedId != View.NO_ID && singleSelection) {
          setCheckedStateForView(checkedId, false);
        }
        setCheckedId(chip.getId());
      }
    }

    super.addView(child, index, params);
  }

  /** @deprecated Use {@link ChipGroup#setChipSpacingHorizontal(int)} instead. */
  @Deprecated
  public void setDividerDrawableHorizontal(Drawable divider) {
    throw new UnsupportedOperationException(
        "Changing divider drawables have no effect. ChipGroup do not use divider drawables as "
            + "spacing.");
  }

  /** @deprecated Use {@link ChipGroup#setChipSpacingVertical(int)} instead. */
  @Deprecated
  public void setDividerDrawableVertical(@Nullable Drawable divider) {
    throw new UnsupportedOperationException(
        "Changing divider drawables have no effect. ChipGroup do not use divider drawables as "
            + "spacing.");
  }

  /** @deprecated Use {@link ChipGroup#setChipSpacingHorizontal(int)} instead. */
  @Deprecated
  public void setShowDividerHorizontal(int dividerMode) {
    throw new UnsupportedOperationException(
        "Changing divider modes has no effect. ChipGroup do not use divider drawables as spacing.");
  }

  /** @deprecated Use {@link ChipGroup#setChipSpacingVertical(int)} instead. */
  @Deprecated
  public void setShowDividerVertical(int dividerMode) {
    throw new UnsupportedOperationException(
        "Changing divider modes has no effect. ChipGroup do not use divider drawables as spacing.");
  }

  /** @deprecated Use {@link ChipGroup#setSingleLine(int)} instead. */
  @Deprecated
  public void setFlexWrap(int flexWrap) {
    throw new UnsupportedOperationException(
        "Changing flex wrap not allowed. ChipGroup exposes a singleLine attribute instead.");
  }

  /**
   * Sets the selection to the chip whose identifier is passed in parameter.
   *
   * <p>In {@link #isSingleSelection() single selection mode}, checking a chip also unchecks all
   * others.
   *
   * @param id the unique id of the chip to select in this group
   * @see #getCheckedChipId()
   * @see #clearCheck()
   */
  public void check(@IdRes int id) {
    if (id == checkedId) {
      return;
    }

    if (checkedId != View.NO_ID && singleSelection) {
      setCheckedStateForView(checkedId, false);
    }

    if (id != View.NO_ID) {
      setCheckedStateForView(id, true);
    }

    setCheckedId(id);
  }
  /**
   * When in {@link #isSingleSelection() single selection mode}, returns the identifier of the
   * selected chip in this group. Upon empty selection, the returned value is {@link View#NO_ID}. If
   * not in single selection mode, the return value is {@link View#NO_ID}.
   *
   * @return the unique id of the selected chip in this group in single selection mode
   * @see #check(int)
   * @see #clearCheck()
   * @see #getCheckedChipIds()
   * @attr ref R.styleable#ChipGroup_checkedChip
   */
  @IdRes
  public int getCheckedChipId() {
    return singleSelection ? checkedId : View.NO_ID;
  }

  /**
   * Returns the identifiers of the selected {@link Chip}s in this group. Upon empty selection, the
   * returned value is an empty list.
   *
   * @return The unique IDs of the selected {@link Chip}s in this group. When in {@link
   *     #isSingleSelection() single selection mode}, returns a list with a single ID. When no
   *     {@link Chip}s are selected, returns an empty list.
   * @see #check(int)
   * @see #clearCheck()
   * @see #getCheckedChipId()
   */
  @NonNull
  public List<Integer> getCheckedChipIds() {
    ArrayList<Integer> checkedIds = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof Chip) {
        if (((Chip) child).isChecked()) {
          checkedIds.add(child.getId());
          if (singleSelection) {
            return checkedIds;
          }
        }
      }
    }

    return checkedIds;
  }

  /**
   * Clears the selection. When the selection is cleared, no chip in this group is selected and
   * {@link #getCheckedChipId()} returns {@link View#NO_ID}.
   *
   * @see #check(int)
   * @see #getCheckedChipId()
   * @see #getCheckedChipIds()
   */
  public void clearCheck() {
    protectFromCheckedChange = true;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof Chip) {
        ((Chip) child).setChecked(false);
      }
    }
    protectFromCheckedChange = false;

    setCheckedId(View.NO_ID);
  }

  /**
   * Register a callback to be invoked when the checked chip changes in this group. This callback is
   * only invoked in {@link #isSingleSelection() single selection mode}.
   *
   * @param listener the callback to call on checked state change
   */
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    onCheckedChangeListener = listener;
  }

  private void setCheckedId(int checkedId) {
    setCheckedId(checkedId, true);
  }

  private void setCheckedId(int checkedId, boolean fromUser) {
    this.checkedId = checkedId;

    if (onCheckedChangeListener != null && singleSelection && fromUser) {
      onCheckedChangeListener.onCheckedChanged(this, checkedId);
    }
  }

  private void setCheckedStateForView(@IdRes int viewId, boolean checked) {
    View checkedView = findViewById(viewId);
    if (checkedView instanceof Chip) {
      protectFromCheckedChange = true;
      ((Chip) checkedView).setChecked(checked);
      protectFromCheckedChange = false;
    }
  }

  private int getChipCount() {
    int count = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) instanceof Chip) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns the index of the Chip within the Chip children.
   *
   * <p>Non-Chip children are ignored when computing the index.
   */
  int getIndexOfChip(@Nullable View child) {
    if (!(child instanceof Chip)) {
      return -1;
    }
    int index = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) instanceof Chip) {
        Chip chip = (Chip) getChildAt(i);
        if (chip == child) {
          return index;
        }
        index++;
      }
    }
    return -1;
  }

  /** Sets the horizontal and vertical spacing between visible chips in this group. */
  public void setChipSpacing(@Dimension int chipSpacing) {
    setChipSpacingHorizontal(chipSpacing);
    setChipSpacingVertical(chipSpacing);
  }

  /** Sets the horizontal and vertical spacing between visible chips in this group. */
  public void setChipSpacingResource(@DimenRes int id) {
    setChipSpacing(getResources().getDimensionPixelOffset(id));
  }

  /** Returns the horizontal spacing between visible chips in this group. */
  @Dimension
  public int getChipSpacingHorizontal() {
    return chipSpacingHorizontal;
  }

  /** Sets the horizontal spacing between visible chips in this group. */
  public void setChipSpacingHorizontal(@Dimension int chipSpacingHorizontal) {
    if (this.chipSpacingHorizontal != chipSpacingHorizontal) {
      this.chipSpacingHorizontal = chipSpacingHorizontal;
      setItemSpacing(chipSpacingHorizontal);
      requestLayout();
    }
  }

  /** Sets the horizontal spacing between visible chips in this group. */
  public void setChipSpacingHorizontalResource(@DimenRes int id) {
    setChipSpacingHorizontal(getResources().getDimensionPixelOffset(id));
  }

  /** Returns the vertical spacing between visible chips in this group. */
  @Dimension
  public int getChipSpacingVertical() {
    return chipSpacingVertical;
  }

  /** Sets the vertical spacing between visible chips in this group. */
  public void setChipSpacingVertical(@Dimension int chipSpacingVertical) {
    if (this.chipSpacingVertical != chipSpacingVertical) {
      this.chipSpacingVertical = chipSpacingVertical;
      setLineSpacing(chipSpacingVertical);
      requestLayout();
    }
  }

  /** Sets the vertical spacing between visible chips in this group. */
  public void setChipSpacingVerticalResource(@DimenRes int id) {
    setChipSpacingVertical(getResources().getDimensionPixelOffset(id));
  }

  // Need to override here in order to un-restrict access to this method outside of the library.
  @SuppressWarnings("RedundantOverride")
  @Override
  public boolean isSingleLine() {
    return super.isSingleLine();
  }

  // Need to override here in order to un-restrict access to this method outside of the library.
  @SuppressWarnings("RedundantOverride")
  @Override
  public void setSingleLine(boolean singleLine) {
    super.setSingleLine(singleLine);
  }

  /** Sets whether this chip group is single line, or reflowed multiline. */
  public void setSingleLine(@BoolRes int id) {
    setSingleLine(getResources().getBoolean(id));
  }

  /** Returns whether this chip group only allows a single chip to be checked. */
  public boolean isSingleSelection() {
    return singleSelection;
  }

  /**
   * Sets whether this chip group only allows a single chip to be checked.
   *
   * <p>Calling this method results in all the chips in this group to become unchecked.
   */
  public void setSingleSelection(boolean singleSelection) {
    if (this.singleSelection != singleSelection) {
      this.singleSelection = singleSelection;

      clearCheck();
    }
  }

  /**
   * Sets whether this chip group only allows a single chip to be checked.
   *
   * <p>Calling this method results in all the chips in this group to become unchecked.
   */
  public void setSingleSelection(@BoolRes int id) {
    setSingleSelection(getResources().getBoolean(id));
  }

  /**
   * Sets whether we prevent all child chips from being deselected.
   *
   * @attr ref R.styleable#ChipGroup_selectionRequired
   * @see #setSingleSelection(boolean)
   */
  public void setSelectionRequired(boolean selectionRequired) {
    this.selectionRequired = selectionRequired;
  }

  /**
   * Returns whether we prevent all child chips from being deselected.
   *
   * @attr ref R.styleable#ChipGroup_selectionRequired
   * @see #setSingleSelection(boolean)
   * @see #setSelectionRequired(boolean)
   */
  public boolean isSelectionRequired() {
    return selectionRequired;
  }

  private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
      // prevents from infinite recursion
      if (protectFromCheckedChange) {
        return;
      }

      List<Integer> checkedChipIds = getCheckedChipIds();
      if (checkedChipIds.isEmpty() && selectionRequired) {
        setCheckedStateForView(buttonView.getId(), true);
        setCheckedId(buttonView.getId(), false);
        return;
      }

      int id = buttonView.getId();

      if (isChecked) {
        if (checkedId != View.NO_ID && checkedId != id && singleSelection) {
          setCheckedStateForView(checkedId, false);
        }
        setCheckedId(id);
      } else if (checkedId == id) {
        setCheckedId(View.NO_ID);
      }
    }
  }

  /**
   * A pass-through listener acts upon the events and dispatches them to another listener. This
   * allows the layout to set its own internal hierarchy change listener without preventing the user
   * to setup theirs.
   */
  private class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {
    private OnHierarchyChangeListener onHierarchyChangeListener;

    @Override
    public void onChildViewAdded(View parent, View child) {
      if (parent == ChipGroup.this && child instanceof Chip) {
        int id = child.getId();
        // generates an id if it's missing
        if (id == View.NO_ID) {
          id = ViewCompat.generateViewId();
          child.setId(id);
        }
        Chip chip = ((Chip) child);
        if (chip.isChecked()) {
          ((ChipGroup) parent).check(chip.getId());
        }
        chip.setOnCheckedChangeListenerInternal(checkedStateTracker);
      }

      if (onHierarchyChangeListener != null) {
        onHierarchyChangeListener.onChildViewAdded(parent, child);
      }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
      if (parent == ChipGroup.this && child instanceof Chip) {
        ((Chip) child).setOnCheckedChangeListenerInternal(null);
      }

      if (onHierarchyChangeListener != null) {
        onHierarchyChangeListener.onChildViewRemoved(parent, child);
      }
    }
  }
}
