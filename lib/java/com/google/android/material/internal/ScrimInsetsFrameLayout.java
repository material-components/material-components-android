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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class ScrimInsetsFrameLayout extends FrameLayout {

  @Nullable Drawable insetForeground;

  Rect insets;

  private Rect tempRect = new Rect();
  private boolean drawTopInsetForeground = true;
  private boolean drawBottomInsetForeground = true;

  public ScrimInsetsFrameLayout(@NonNull Context context) {
    this(context, null);
  }

  public ScrimInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScrimInsetsFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.ScrimInsetsFrameLayout,
            defStyleAttr,
            R.style.Widget_Design_ScrimInsetsFrameLayout);
    insetForeground = a.getDrawable(R.styleable.ScrimInsetsFrameLayout_insetForeground);
    a.recycle();
    setWillNotDraw(true); // No need to draw until the insets are adjusted

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            if (null == ScrimInsetsFrameLayout.this.insets) {
              ScrimInsetsFrameLayout.this.insets = new Rect();
            }
            ScrimInsetsFrameLayout.this.insets.set(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom());
            onInsetsChanged(insets);
            setWillNotDraw(!insets.hasSystemWindowInsets() || insetForeground == null);
            ViewCompat.postInvalidateOnAnimation(ScrimInsetsFrameLayout.this);
            return insets.consumeSystemWindowInsets();
          }
        });
  }

  /** Sets the drawable used for the inset foreground. */
  public void setScrimInsetForeground(@Nullable Drawable drawable) {
    insetForeground = drawable;
  }

  public void setDrawTopInsetForeground(boolean drawTopInsetForeground) {
    this.drawTopInsetForeground = drawTopInsetForeground;
  }

  public void setDrawBottomInsetForeground(boolean drawBottomInsetForeground) {
    this.drawBottomInsetForeground = drawBottomInsetForeground;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    int width = getWidth();
    int height = getHeight();
    if (insets != null && insetForeground != null) {
      int sc = canvas.save();
      canvas.translate(getScrollX(), getScrollY());

      // Top
      if (drawTopInsetForeground) {
        tempRect.set(0, 0, width, insets.top);
        insetForeground.setBounds(tempRect);
        insetForeground.draw(canvas);
      }

      // Bottom
      if (drawBottomInsetForeground) {
        tempRect.set(0, height - insets.bottom, width, height);
        insetForeground.setBounds(tempRect);
        insetForeground.draw(canvas);
      }

      // Left
      tempRect.set(0, insets.top, insets.left, height - insets.bottom);
      insetForeground.setBounds(tempRect);
      insetForeground.draw(canvas);

      // Right
      tempRect.set(width - insets.right, insets.top, width, height - insets.bottom);
      insetForeground.setBounds(tempRect);
      insetForeground.draw(canvas);

      canvas.restoreToCount(sc);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (insetForeground != null) {
      insetForeground.setCallback(this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (insetForeground != null) {
      insetForeground.setCallback(null);
    }
  }

  protected void onInsetsChanged(WindowInsetsCompat insets) {}
}
