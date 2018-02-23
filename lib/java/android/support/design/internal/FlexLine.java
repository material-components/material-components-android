/*
 * Copyright 2018 The Android Open Source Project
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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.support.annotation.RestrictTo;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds properties related to a single flex line. This class is not expected to be changed outside
 * of the {@link FlexboxLayout}, thus only exposing the getter methods that may be useful for other
 * classes using the {@link FlexboxLayout}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class FlexLine {

  FlexLine() {}

  int left = Integer.MAX_VALUE;

  int top = Integer.MAX_VALUE;

  int right = Integer.MIN_VALUE;

  int bottom = Integer.MIN_VALUE;

  /** @see #getMainSize() */
  int mainSize;

  /**
   * The sum of the lengths of dividers along the main axis. This value should be lower than the
   * value of {@link #mainSize}.
   */
  int dividerLengthInMainSize;

  /** @see #getCrossSize() */
  int crossSize;

  /** @see #getItemCount() */
  int itemCount;

  /** Holds the count of the views whose visibilities are gone */
  int goneItemCount;

  /** @see #getTotalFlexGrow() */
  float motalFlexGrow;

  /** @see #getTotalFlexShrink() */
  float totalFlexShrink;

  /** The largest value of the individual child's baseline (obtained by View#getBaseline() */
  int maxBaseline;

  /** The sum of the cross size used before this flex line. */
  int sumCrossSizeBefore;

  /**
   * Store the indices of the children views whose alignSelf property is stretch. The stored indices
   * are the absolute indices including all children in the Flexbox, not the relative indices in
   * this flex line.
   */
  List<Integer> indicesAlignSelfStretch = new ArrayList<>();

  int firstIndex;

  int lastIndex;

  /** @return the size of the flex line in pixels along the main axis of the flex container. */
  public int getMainSize() {
    return mainSize;
  }

  /** @return the size of the flex line in pixels along the cross axis of the flex container. */
  @SuppressWarnings("WeakerAccess")
  public int getCrossSize() {
    return crossSize;
  }

  /** @return the count of the views contained in this flex line. */
  @SuppressWarnings("WeakerAccess")
  public int getItemCount() {
    return itemCount;
  }

  /** @return the count of the views whose visibilities are not gone in this flex line. */
  @SuppressWarnings("WeakerAccess")
  public int getItemCountNotGone() {
    return itemCount - goneItemCount;
  }

  /** @return the sum of the flexGrow properties of the children included in this flex line */
  @SuppressWarnings("WeakerAccess")
  public float getTotalFlexGrow() {
    return motalFlexGrow;
  }

  /** @return the sum of the flexShrink properties of the children included in this flex line */
  @SuppressWarnings("WeakerAccess")
  public float getTotalFlexShrink() {
    return totalFlexShrink;
  }

  /** @return the first view's index included in this flex line. */
  public int getFirstIndex() {
    return firstIndex;
  }

  /**
   * Updates the position of the flex line from the contained view.
   *
   * @param view the view contained in this flex line
   * @param leftDecoration the length of the decoration on the left of the view
   * @param topDecoration the length of the decoration on the top of the view
   * @param rightDecoration the length of the decoration on the right of the view
   * @param bottomDecoration the length of the decoration on the bottom of the view
   */
  void updatePositionFromView(
      View view, int leftDecoration, int topDecoration, int rightDecoration, int bottomDecoration) {
    FlexItem flexItem = (FlexItem) view.getLayoutParams();
    left = Math.min(left, view.getLeft() - flexItem.getMarginLeft() - leftDecoration);
    top = Math.min(top, view.getTop() - flexItem.getMarginTop() - topDecoration);
    right = Math.max(right, view.getRight() + flexItem.getMarginRight() + rightDecoration);
    bottom = Math.max(bottom, view.getBottom() + flexItem.getMarginBottom() + bottomDecoration);
  }
}
