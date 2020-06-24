/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class ForegroundLinearLayout extends LinearLayoutCompat {

  @Nullable private Drawable foreground;

  private final Rect selfBounds = new Rect();

  private final Rect overlayBounds = new Rect();

  private int foregroundGravity = Gravity.FILL;

  protected boolean mForegroundInPadding = true;

  boolean foregroundBoundsChanged = false;

  public ForegroundLinearLayout(@NonNull Context context) {
    this(context, null);
  }

  public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ForegroundLinearLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.ForegroundLinearLayout, defStyle, 0);

    foregroundGravity =
        a.getInt(R.styleable.ForegroundLinearLayout_android_foregroundGravity, foregroundGravity);

    final Drawable d = a.getDrawable(R.styleable.ForegroundLinearLayout_android_foreground);
    if (d != null) {
      setForeground(d);
    }

    mForegroundInPadding =
        a.getBoolean(R.styleable.ForegroundLinearLayout_foregroundInsidePadding, true);

    a.recycle();
  }

  /**
   * Describes how the foreground is positioned.
   *
   * @return foreground gravity.
   * @see #setForegroundGravity(int)
   */
  @Override
  public int getForegroundGravity() {
    return foregroundGravity;
  }

  /**
   * Describes how the foreground is positioned. Defaults to START and TOP.
   *
   * @param foregroundGravity See {@link android.view.Gravity}
   * @see #getForegroundGravity()
   */
  @Override
  public void setForegroundGravity(int foregroundGravity) {
    if (this.foregroundGravity != foregroundGravity) {
      if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
        foregroundGravity |= Gravity.START;
      }

      if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
        foregroundGravity |= Gravity.TOP;
      }

      this.foregroundGravity = foregroundGravity;

      if (this.foregroundGravity == Gravity.FILL && foreground != null) {
        Rect padding = new Rect();
        foreground.getPadding(padding);
      }

      requestLayout();
    }
  }

  @Override
  protected boolean verifyDrawable(Drawable who) {
    return super.verifyDrawable(who) || (who == foreground);
  }

  @RequiresApi(11)
  @Override
  public void jumpDrawablesToCurrentState() {
    super.jumpDrawablesToCurrentState();
    if (foreground != null) {
      foreground.jumpToCurrentState();
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    if (foreground != null && foreground.isStateful()) {
      foreground.setState(getDrawableState());
    }
  }

  /**
   * Supply a Drawable that is to be rendered on top of all of the child views in the frame layout.
   * Any padding in the Drawable will be taken into account by ensuring that the children are inset
   * to be placed inside of the padding area.
   *
   * @param drawable The Drawable to be drawn on top of the children.
   */
  @Override
  public void setForeground(@Nullable Drawable drawable) {
    if (foreground != drawable) {
      if (foreground != null) {
        foreground.setCallback(null);
        unscheduleDrawable(foreground);
      }

      foreground = drawable;

      if (drawable != null) {
        setWillNotDraw(false);
        drawable.setCallback(this);
        if (drawable.isStateful()) {
          drawable.setState(getDrawableState());
        }
        if (foregroundGravity == Gravity.FILL) {
          Rect padding = new Rect();
          drawable.getPadding(padding);
        }
      } else {
        setWillNotDraw(true);
      }
      requestLayout();
      invalidate();
    }
  }

  /**
   * Returns the drawable used as the foreground of this FrameLayout. The foreground drawable, if
   * non-null, is always drawn on top of the children.
   *
   * @return A Drawable or null if no foreground was set.
   */
  @Nullable
  @Override
  public Drawable getForeground() {
    return foreground;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    foregroundBoundsChanged |= changed;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    foregroundBoundsChanged = true;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    if (this.foreground != null) {
      final Drawable foreground = this.foreground;

      if (foregroundBoundsChanged) {
        foregroundBoundsChanged = false;
        final Rect selfBounds = this.selfBounds;
        final Rect overlayBounds = this.overlayBounds;

        final int w = getRight() - getLeft();
        final int h = getBottom() - getTop();

        if (mForegroundInPadding) {
          selfBounds.set(0, 0, w, h);
        } else {
          selfBounds.set(
              getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
        }

        Gravity.apply(
            foregroundGravity,
            foreground.getIntrinsicWidth(),
            foreground.getIntrinsicHeight(),
            selfBounds,
            overlayBounds);
        foreground.setBounds(overlayBounds);
      }

      foreground.draw(canvas);
    }
  }

  @TargetApi(21)
  @RequiresApi(21)
  @Override
  public void drawableHotspotChanged(float x, float y) {
    super.drawableHotspotChanged(x, y);
    if (foreground != null) {
      foreground.setHotspot(x, y);
    }
  }
}
