/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.sidesheet;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.sidesheet.Sheet.SheetEdge;

/**
 * A delegate for {@link SideSheetBehavior} to handle logic specific to the sheet's edge position.
 */
abstract class SheetDelegate {

  /**
   * Returns the edge of the screen in which the sheet is positioned. Must be a {@link SheetEdge}
   * value.
   */
  @SheetEdge
  abstract int getSheetEdge();

  /** Returns the sheet's offset from the inner edge when hidden. */
  abstract int getHiddenOffset();

  /** Returns the sheet's offset from the inner edge when expanded. */
  abstract int getExpandedOffset();

  /** Whether the view has been released from a drag close to the inner edge. */
  abstract boolean isReleasedCloseToInnerEdge(@NonNull View releasedChild);

  abstract boolean isSwipeSignificant(float xVelocity, float yVelocity);

  /**
   * Whether the sheet should hide, based on the position of child, velocity of the drag event, and
   * {@link SideSheetBehavior#getHideThreshold()}.
   */
  abstract boolean shouldHide(@NonNull View child, float velocity);

  /**
   * Returns the edge of the sheet that the sheet expands towards, calling the child parameter's
   * edge depending on which edge of the screen the sheet is positioned. For a right based sheet,
   * this would return {@code child.getLeft()}.
   */
  abstract <V extends View> int getOuterEdge(@NonNull V child);

  /**
   * Returns the calculated slide offset based on how close the sheet is to the outer edge. The
   * offset value increases as the sheet moves towards the outer edge.
   *
   * @return slide offset represented as a float value between 0 and 1. A value of 0 means that the
   *     sheet is hidden and a value of 1 means that the sheet is fully expanded.
   */
  abstract float calculateSlideOffset(int outerEdge);

  /** Set the coplanar sheet layout params depending on the screen size. */
  abstract void updateCoplanarSiblingLayoutParams(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int sheetLeft, int sheetRight);

  /** Sets the coplanar sheet's margin that's adjacent to the side sheet to the provided value. */
  abstract void updateCoplanarSiblingAdjacentMargin(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int coplanarSiblingAdjacentMargin);

  /**
   * Returns the coplanar sibling's margin that's adjacent to the sheet's left or right edge,
   * depending on the sheet edge.
   */
  abstract int getCoplanarSiblingAdjacentMargin(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams);

  /**
   * Calculates the margin on the inner side of the sheet based on the {@link MarginLayoutParams}.
   * For right based sheets, the inner margin would be the right margin.
   */
  abstract int calculateInnerMargin(@NonNull MarginLayoutParams marginLayoutParams);

  /**
   * Returns the inner edge of the parent. For example, for the right sheet, the return value would
   * be {@code parent.getRight()}.
   */
  abstract int getParentInnerEdge(@NonNull CoordinatorLayout parent);

  /**
   * Returns the minimum horizontal view position, used to calculate the drag range of the sheet.
   */
  abstract int getMinViewPositionHorizontal();

  /**
   * Returns the maximum horizontal view position, used to calculate the drag range of the sheet.
   */
  abstract int getMaxViewPositionHorizontal();

  /**
   * Returns whether the sheet is expanding outwards based on its horizontal velocity and sheet
   * edge.
   */
  abstract boolean isExpandingOutwards(float xVelocity);
}
