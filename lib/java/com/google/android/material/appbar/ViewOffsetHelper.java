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

package com.google.android.material.appbar;

import android.view.View;
import androidx.core.view.ViewCompat;

/**
 * Utility helper for moving a {@link View} around using {@link View#offsetLeftAndRight(int)} and
 * {@link View#offsetTopAndBottom(int)}.
 *
 * <p>Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
class ViewOffsetHelper {

  private final View view;

  private int layoutTop;
  private int layoutLeft;
  private int offsetTop;
  private int offsetLeft;
  private boolean verticalOffsetEnabled = true;
  private boolean horizontalOffsetEnabled = true;

  public ViewOffsetHelper(View view) {
    this.view = view;
  }

  void onViewLayout() {
    // Grab the original top and left
    layoutTop = view.getTop();
    layoutLeft = view.getLeft();
  }

  void applyOffsets() {
    ViewCompat.offsetTopAndBottom(view, offsetTop - (view.getTop() - layoutTop));
    ViewCompat.offsetLeftAndRight(view, offsetLeft - (view.getLeft() - layoutLeft));
  }

  /**
   * Set the top and bottom offset for this {@link ViewOffsetHelper}'s view.
   *
   * @param offset the offset in px.
   * @return true if the offset has changed
   */
  public boolean setTopAndBottomOffset(int offset) {
    if (verticalOffsetEnabled && offsetTop != offset) {
      offsetTop = offset;
      applyOffsets();
      return true;
    }
    return false;
  }

  /**
   * Set the left and right offset for this {@link ViewOffsetHelper}'s view.
   *
   * @param offset the offset in px.
   * @return true if the offset has changed
   */
  public boolean setLeftAndRightOffset(int offset) {
    if (horizontalOffsetEnabled && offsetLeft != offset) {
      offsetLeft = offset;
      applyOffsets();
      return true;
    }
    return false;
  }

  public int getTopAndBottomOffset() {
    return offsetTop;
  }

  public int getLeftAndRightOffset() {
    return offsetLeft;
  }

  public int getLayoutTop() {
    return layoutTop;
  }

  public int getLayoutLeft() {
    return layoutLeft;
  }

  public void setVerticalOffsetEnabled(boolean verticalOffsetEnabled) {
    this.verticalOffsetEnabled = verticalOffsetEnabled;
  }

  public boolean isVerticalOffsetEnabled() {
    return verticalOffsetEnabled;
  }

  public void setHorizontalOffsetEnabled(boolean horizontalOffsetEnabled) {
    this.horizontalOffsetEnabled = horizontalOffsetEnabled;
  }

  public boolean isHorizontalOffsetEnabled() {
    return horizontalOffsetEnabled;
  }
}
