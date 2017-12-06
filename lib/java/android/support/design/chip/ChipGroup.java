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
import android.support.annotation.BoolRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

/**
 * A ChipGroup is used to hold multiple {@link Chip}s. By default, the chips are reflowed across
 * multiple lines. Set the {@link R.attr#singleLine app:singleLine} attribute to constrain the chips
 * to a single horizontal line. If you do so, you'll usually want to wrap this ChipGroup in a {@link
 * android.widget.HorizontalScrollView}.
 */
public class ChipGroup extends FlexboxLayout {

  @Dimension private int chipSpacingHorizontal;
  @Dimension private int chipSpacingVertical;
  private boolean singleLine;

  private final SpacingDrawable spacingDrawable = new SpacingDrawable();

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

    a.recycle();

    setDividerDrawable(spacingDrawable);
    setShowDivider(SHOW_DIVIDER_MIDDLE);
    // Superclass uses presence of dividers to determine whether it needs to draw.
    setWillNotDraw(true);
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
}
