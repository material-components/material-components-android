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

package android.support.design.chip;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.BoolRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

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
public class ChipGroup extends FlexboxLayout {

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

  @Dimension private int chipSpacingHorizontal;
  @Dimension private int chipSpacingVertical;
  private boolean singleLine;
  private boolean singleSelection;

  @Nullable private OnCheckedChangeListener onCheckedChangeListener;

  private final SpacingDrawable spacingDrawable = new SpacingDrawable();
  private final CheckedStateTracker checkedStateTracker = new CheckedStateTracker();
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
    super(context, attrs, defStyleAttr);

    TypedArray a =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ChipGroup,
            defStyleAttr,
            R.style.Widget_MaterialComponents_ChipGroup);

    int chipSpacing = a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacing, 0);
    setChipSpacingHorizontal(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingHorizontal, chipSpacing));
    setChipSpacingVertical(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingVertical, chipSpacing));
    setSingleLine(a.getBoolean(R.styleable.ChipGroup_singleLine, false));
    setSingleSelection(a.getBoolean(R.styleable.ChipGroup_singleSelection, false));
    int checkedChip = a.getResourceId(R.styleable.ChipGroup_checkedChip, View.NO_ID);
    if (checkedChip != View.NO_ID) {
      checkedId = checkedChip;
    }

    a.recycle();

    setDividerDrawable(spacingDrawable);
    setShowDivider(SHOW_DIVIDER_MIDDLE);
    // Superclass uses presence of dividers to determine whether it needs to draw.
    setWillNotDraw(true);

    super.setOnHierarchyChangeListener(passThroughListener);
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

  @Override
  public void setDividerDrawableHorizontal(Drawable divider) {
    if (divider != spacingDrawable) {
      throw new UnsupportedOperationException(
          "Changing divider drawables not allowed. ChipGroup uses divider drawables as spacing.");
    }
    super.setDividerDrawableHorizontal(divider);
  }

  @Override
  public void setDividerDrawableVertical(@Nullable Drawable divider) {
    if (divider != spacingDrawable) {
      throw new UnsupportedOperationException(
          "Changing divider drawables not allowed. ChipGroup uses divider drawables as spacing.");
    }
    super.setDividerDrawableVertical(divider);
  }

  @Override
  public void setShowDividerHorizontal(int dividerMode) {
    if (dividerMode != SHOW_DIVIDER_MIDDLE) {
      throw new UnsupportedOperationException(
          "Changing divider modes not allowed. ChipGroup uses divider drawables as spacing.");
    }
    super.setShowDividerHorizontal(dividerMode);
  }

  @Override
  public void setShowDividerVertical(int dividerMode) {
    if (dividerMode != SHOW_DIVIDER_MIDDLE) {
      throw new UnsupportedOperationException(
          "Changing divider modes not allowed. ChipGroup uses divider drawables as spacing.");
    }
    super.setShowDividerVertical(dividerMode);
  }

  @Override
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
   * @attr ref R.styleable#ChipGroup_checkedChip
   */
  @IdRes
  public int getCheckedChipId() {
    return singleSelection ? checkedId : View.NO_ID;
  }

  /**
   * Clears the selection. When the selection is cleared, no chip in this group is selected and
   * {@link #getCheckedChipId()} returns {@link View#NO_ID}.
   *
   * @see #check(int)
   * @see #getCheckedChipId()
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
    this.checkedId = checkedId;

    if (onCheckedChangeListener != null && singleSelection) {
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

  /** Sets the horizontal and vertical spacing between chips in this group. */
  public void setChipSpacing(@Dimension int chipSpacing) {
    setChipSpacingHorizontal(chipSpacing);
    setChipSpacingVertical(chipSpacing);
  }

  /** Sets the horizontal and vertical spacing between chips in this group. */
  public void setChipSpacingResource(@DimenRes int id) {
    setChipSpacing(getResources().getDimensionPixelOffset(id));
  }

  /** Returns the horizontal spacing between chips in this group. */
  @Dimension
  public int getChipSpacingHorizontal() {
    return chipSpacingHorizontal;
  }

  /** Sets the horizontal spacing between chips in this group. */
  public void setChipSpacingHorizontal(@Dimension int chipSpacingHorizontal) {
    if (this.chipSpacingHorizontal != chipSpacingHorizontal) {
      this.chipSpacingHorizontal = chipSpacingHorizontal;
      requestLayout();
    }
  }

  /** Sets the horizontal spacing between chips in this group. */
  public void setChipSpacingHorizontalResource(@DimenRes int id) {
    setChipSpacingHorizontal(getResources().getDimensionPixelOffset(id));
  }

  /** Returns the vertical spacing between chips in this group. */
  @Dimension
  public int getChipSpacingVertical() {
    return chipSpacingVertical;
  }

  /** Sets the vertical spacing between chips in this group. */
  public void setChipSpacingVertical(@Dimension int chipSpacingVertical) {
    if (this.chipSpacingVertical != chipSpacingVertical) {
      this.chipSpacingVertical = chipSpacingVertical;
      requestLayout();
    }
  }

  /** Sets the vertical spacing between chips in this group. */
  public void setChipSpacingVerticalResource(@DimenRes int id) {
    setChipSpacingVertical(getResources().getDimensionPixelOffset(id));
  }

  /** Returns whether this chip group is single line, or reflowed multiline. */
  public boolean isSingleLine() {
    return singleLine;
  }

  /** Sets whether this chip group is single line, or reflowed multiline. */
  public void setSingleLine(boolean singleLine) {
    this.singleLine = singleLine;
    super.setFlexWrap(singleLine ? FlexWrap.NOWRAP : FlexWrap.WRAP);
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
   * Drawable that only has intrinsic width/height and nothing else. Intended to be used as spacing
   * for {@link ChipGroup#setDividerDrawable(Drawable)}.
   */
  private class SpacingDrawable extends Drawable {

    @Override
    public int getIntrinsicWidth() {
      return chipSpacingHorizontal;
    }

    @Override
    public int getIntrinsicHeight() {
      return chipSpacingVertical;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      // No-op.
    }

    @Override
    public void setAlpha(int alpha) {
      // No-op.
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
      // No-op.
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSPARENT;
    }
  }

  private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      // prevents from infinite recursion
      if (protectFromCheckedChange) {
        return;
      }

      int id = buttonView.getId();

      if (isChecked) {
        if (checkedId != View.NO_ID && checkedId != id && singleSelection) {
          setCheckedStateForView(checkedId, false);
        }
        setCheckedId(id);
      } else {
        if (checkedId == id) {
          setCheckedId(View.NO_ID);
        }
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
          if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            id = View.generateViewId();
          } else {
            id = child.hashCode();
          }
          child.setId(id);
        }
        ((Chip) child).setOnCheckedChangeListenerInternal(checkedStateTracker);
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
