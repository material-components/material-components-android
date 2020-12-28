/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.feature;


import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;

/**
 * A View that measures itself to be as tall as the bottom window inset.
 */
public class BottomWindowInsetView extends View {

  private int systemWindowInsetBottom;

  public BottomWindowInsetView(Context context) {
    this(context, null);
  }

  public BottomWindowInsetView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomWindowInsetView(Context context, @Nullable AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    super.setVisibility(GONE);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    ViewGroup parent = (ViewGroup) getParent();
    while (parent != null && !ViewCompat.getFitsSystemWindows(parent)) {
      parent = (ViewGroup) parent.getParent();
    }

    if (parent == null) {
      return;
    }

    ViewCompat.setOnApplyWindowInsetsListener(
        parent,
        (v, insets) -> {
          systemWindowInsetBottom = insets.getSystemWindowInsetBottom();
          super.setVisibility(systemWindowInsetBottom == 0 ? GONE : VISIBLE);
          if (systemWindowInsetBottom > 0) {
            requestLayout();
          }

          return insets;
        });
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(width, systemWindowInsetBottom);
  }

  /**
   * Throws {@link UnsupportedOperationException} if called. Let the view handle its own visibility.
   */
  @Override
  public void setVisibility(int visibility) {
    throw new UnsupportedOperationException("don't call setVisibility on BottomWindowInsetView");
  }
}
