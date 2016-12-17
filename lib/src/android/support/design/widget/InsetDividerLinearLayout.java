/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import android.support.design.R;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A LinearLayout which supports arbitrary divider insets. Inset dimensions can be set by calling
 * {@link #setDividerInsetStart(int)} and {@link #setDividerInsetEnd(int)}. You can also specify
 * which dividers to inset by calling {@link #setApplyInsets(int)}.
 *
 * <p>
 * Differences in behavior between this layout and the standard LinearLayout:
 * <ul>
 *     <li>Dividers are drawn on top of child views to avoid disturbing grid alignment.</li>
 *     <li>Dividers are drawn outside of padding applied to this layout.</li>
 *     <li>{@link #setDividerPadding(int)} is a shortcut to set equal insets on both ends</li>
 * </ul>
 *
 * <p>
 * See {@link LinearLayoutCompat} for additional documentation.
 */
public class InsetDividerLinearLayout extends LinearLayoutCompat {
  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(flag = true,
      value = {
          INSET_DIVIDER_NONE,
          INSET_DIVIDER_BEGINNING,
          INSET_DIVIDER_MIDDLE,
          INSET_DIVIDER_END
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface InsetMode {}

  /**
   * Don't inset any dividers.
   */
  public static final int INSET_DIVIDER_NONE = 0;
  /**
   * Inset the divider at the beginning of the group.
   */
  public static final int INSET_DIVIDER_BEGINNING = 1;
  /**
   * Inset dividers between each item in the group.
   */
  public static final int INSET_DIVIDER_MIDDLE = 2;
  /**
   * Inset the divider at the end of the group.
   */
  public static final int INSET_DIVIDER_END = 4;

  private Drawable mDivider;
  private int mDividerWidth;
  private int mDividerHeight;
  private int mDividerOffsetVertical;
  private int mDividerOffsetHorizontal;
  private int mDividerInsetStart;
  private int mDividerInsetEnd;
  private int mApplyInsets;

  public InsetDividerLinearLayout(Context context) {
    this(context, null);
  }

  public InsetDividerLinearLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public InsetDividerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray a = context.obtainStyledAttributes(attrs,
        R.styleable.InsetDividerLinearLayout, 0, 0);

    mDividerInsetStart =
        a.getDimensionPixelSize(R.styleable.InsetDividerLinearLayout_insetDividerStart, 0);
    mDividerInsetEnd =
        a.getDimensionPixelSize(R.styleable.InsetDividerLinearLayout_insetDividerEnd, 0);
    mApplyInsets = a.getInt(R.styleable.InsetDividerLinearLayout_insetDividers,
        INSET_DIVIDER_BEGINNING | INSET_DIVIDER_MIDDLE | INSET_DIVIDER_END);

    a.recycle();
  }

  /**
   * @return the divider Drawable that will divide each item.
   *
   * @see #setDividerDrawable(Drawable)
   */
  @Override
  public Drawable getDividerDrawable() {
    return mDivider;
  }

  /**
   * Set a drawable to be used as a divider between items.
   *
   * @param divider Drawable that will divide each item.
   *
   * @see #setShowDividers(int)
   */
  @Override
  public void setDividerDrawable(Drawable divider) {
    if (divider == mDivider) {
      return;
    }
    mDivider = divider;
    if (divider != null) {
      mDividerWidth = divider.getIntrinsicWidth();
      mDividerHeight = divider.getIntrinsicHeight();
      mDividerOffsetVertical = mDividerHeight / 2;
      mDividerOffsetHorizontal = mDividerWidth / 2;
    } else {
      mDividerWidth = 0;
      mDividerHeight = 0;
      mDividerOffsetVertical = 0;
      mDividerOffsetHorizontal = 0;
    }
    setWillNotDraw(divider == null);
    invalidate();
  }

  /**
   * Set the inset displayed on the start of dividers.
   *
   * @param dividerInsetStart Inset value in pixels that will be applied to the divider start
   *
   * @see #setApplyInsets(int)
   * @see #setDividerDrawable(Drawable)
   * @see #getDividerInsetStart()
   */
  public void setDividerInsetStart(int dividerInsetStart) {
    if (mDividerInsetStart != dividerInsetStart) {
      mDividerInsetStart = dividerInsetStart;
      super.setDividerPadding(0);
      invalidate();
    }
  }

  /**
   * Return the size used to inset the start of dividers in pixels.
   *
   * @see #setApplyInsets(int)
   * @see #setDividerDrawable(Drawable)
   * @see #setDividerInsetStart(int)
   */
  public int getDividerInsetStart() {
    return mDividerInsetStart;
  }

  /**
   * Set the inset displayed on the end of dividers.
   *
   * @param dividerInsetEnd Inset value in pixels that will be applied to the divider end
   *
   * @see #setApplyInsets(int)
   * @see #setDividerDrawable(Drawable)
   * @see #getDividerInsetStart()
   */
  public void setDividerInsetEnd(int dividerInsetEnd) {
    if (mDividerInsetEnd != dividerInsetEnd) {
      mDividerInsetEnd = dividerInsetEnd;
      super.setDividerPadding(0);
      invalidate();
    }
  }

  /**
   * Return the size used to inset the end of dividers in pixels.
   *
   * @see #setApplyInsets(int)
   * @see #setDividerDrawable(Drawable)
   * @see #setDividerInsetEnd(int)
   */
  public int getDividerInsetEnd() {
    return mDividerInsetEnd;
  }

  /**
   * Set which dividers should be inset by the values set in {@link #setDividerInsetStart(int)}
   * and {@link #setDividerInsetEnd(int)}. The default is to inset all dividers.
   *
   * @param applyInsets One or more of {@link #INSET_DIVIDER_BEGINNING},
   *                    {@link #INSET_DIVIDER_MIDDLE}, or {@link #INSET_DIVIDER_END},
   *                    or {@link #INSET_DIVIDER_NONE} to inset no dividers.
   */
  public void setApplyInsets(@InsetMode int applyInsets) {
    if (applyInsets != mApplyInsets) {
      mApplyInsets = applyInsets;
      invalidate();
    }
  }

  @InsetMode
  public int getApplyInsets() {
    return mApplyInsets;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDividerPadding(int padding) {
    super.setDividerPadding(padding);
    mDividerInsetStart = padding;
    mDividerInsetEnd = padding;
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (mDivider == null || getShowDividers() == SHOW_DIVIDER_NONE) {
      return;
    }

    if (getOrientation() == VERTICAL) {
      drawDividersVertical(canvas);
    } else {
      drawDividersHorizontal(canvas);
    }
  }

  private boolean hasStartDivider() {
    return (getShowDividers() & SHOW_DIVIDER_BEGINNING) != 0;
  }

  private boolean hasMiddleDivider() {
    return (getShowDividers() & SHOW_DIVIDER_MIDDLE) != 0;
  }

  private boolean hasEndDivider() {
    return (getShowDividers() & SHOW_DIVIDER_END) != 0;
  }

  private boolean hasStartDividerInsets() {
    return (mApplyInsets & INSET_DIVIDER_BEGINNING) != 0;
  }

  private boolean hasMiddleDividerInsets() {
    return (mApplyInsets & INSET_DIVIDER_MIDDLE) != 0;
  }

  private boolean hasEndDividerInsets() {
    return (mApplyInsets & INSET_DIVIDER_END) != 0;
  }

  int getFirstVisibleChildIndex() {
    final int count = getChildCount();
    for (int i = 0; i < count; i++) {
      if (getChildAt(i).getVisibility() != GONE) {
        return i;
      }
    }
    return Integer.MAX_VALUE;
  }

  int getLastVisibleChildIndex() {
    final int count = getChildCount();
    for (int i = count - 1; i >= 0; i--) {
      if (getChildAt(i).getVisibility() != GONE) {
        return i;
      }
    }
    return Integer.MIN_VALUE;
  }

  private void drawDividersVertical(Canvas canvas) {
    final boolean isLayoutRtl =
        ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

    final int insetStart = isLayoutRtl ? mDividerInsetEnd : mDividerInsetStart;
    final int insetEnd = isLayoutRtl ? mDividerInsetStart : mDividerInsetEnd;

    final int indexStart = getFirstVisibleChildIndex();
    final int indexEnd = getLastVisibleChildIndex();

    if (hasMiddleDivider() && indexStart < indexEnd) {
      final boolean hasMiddleInsets = hasMiddleDividerInsets();
      final int insetLeft = hasMiddleInsets ? insetStart : 0;
      final int insetRight = hasMiddleInsets ? insetEnd : 0;

      for (int i = indexStart + 1; i <= indexEnd; i++) {
        final View child = getChildAt(i);
        if (child != null && child.getVisibility() != GONE) {
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          final int top = child.getTop() - lp.topMargin - mDividerOffsetVertical;
          drawHorizontalDivider(canvas, top, insetLeft, insetRight);
        }
      }
    }

    if (hasStartDivider()) {
      final boolean hasInsets = hasStartDividerInsets();
      drawHorizontalDivider(canvas, 0,
          hasInsets ? insetStart : 0,
          hasInsets ? insetEnd : 0);
    }

    if (hasEndDivider()) {
      final boolean hasInsets = hasEndDividerInsets();
      drawHorizontalDivider(canvas, getHeight() - mDividerHeight,
          hasInsets ? insetStart: 0,
          hasInsets ? insetEnd : 0);
    }
  }

  void drawDividersHorizontal(Canvas canvas) {
    final boolean isLayoutRtl =
        ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

    final int insetStart = mDividerInsetStart;
    final int insetEnd = mDividerInsetEnd;

    final int indexStart = getFirstVisibleChildIndex();
    final int indexEnd = getLastVisibleChildIndex();

    if (hasMiddleDivider() && indexStart < indexEnd) {
      final boolean hasMiddleInsets = hasMiddleDividerInsets();
      final int insetTop = hasMiddleInsets ? insetStart : 0;
      final int insetBottom = hasMiddleInsets ? insetEnd : 0;

      for (int i = indexStart + 1; i < indexEnd; i++) {
        final View child = getChildAt(i);
        if (child != null && child.getVisibility() != GONE) {
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          final int position;
          if (isLayoutRtl) {
            position = child.getRight() + lp.rightMargin;
          } else {
            position = child.getLeft() - lp.leftMargin;
          }

          drawVerticalDivider(canvas, position - mDividerOffsetHorizontal,
              insetTop, insetBottom);
        }
      }
    }

    if (hasStartDivider()) {
      final boolean hasInsets = hasStartDividerInsets();
      drawVerticalDivider(canvas, isLayoutRtl ? getWidth() - mDividerHeight : 0,
          hasInsets ? mDividerInsetStart : 0,
          hasInsets ? mDividerInsetEnd : 0);
    }

    if (hasEndDivider()) {
      final boolean hasInsets = hasEndDividerInsets();
      drawVerticalDivider(canvas, isLayoutRtl ? 0 : getWidth() - mDividerHeight,
          hasInsets ? mDividerInsetStart : 0,
          hasInsets ? mDividerInsetEnd : 0);
    }
  }

  void drawHorizontalDivider(Canvas canvas, int top, int insetLeft, int insetRight) {
    mDivider.setBounds(insetLeft, top, getWidth() - insetRight, top + mDividerHeight);
    mDivider.draw(canvas);
  }

  void drawVerticalDivider(Canvas canvas, int left, int insetTop, int insetBottom) {
    mDivider.setBounds(left, insetTop, left + mDividerWidth, getHeight() - insetBottom);
    mDivider.draw(canvas);
  }
}
