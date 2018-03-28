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

package android.support.design.appbar;

import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Utility helper for moving a {@link View} around using {@link
 * View#offsetLeftAndRight(int)} and {@link View#offsetTopAndBottom(int)}.
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

  public ViewOffsetHelper(View view) {
    this.view = view;
  }

  public void onViewLayout() {
    // Now grab the intended top
    layoutTop = view.getTop();
    layoutLeft = view.getLeft();

    // And offset it as needed
    updateOffsets();
  }

  private void updateOffsets() {
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
    if (offsetTop != offset) {
      offsetTop = offset;
      updateOffsets();
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
    if (offsetLeft != offset) {
      offsetLeft = offset;
      updateOffsets();
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
}
