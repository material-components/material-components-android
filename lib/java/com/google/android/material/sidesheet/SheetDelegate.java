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
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.sidesheet.Sheet.SheetEdge;
import com.google.android.material.sidesheet.Sheet.StableSheetState;
import com.google.android.material.sidesheet.SideSheetBehavior.StateSettlingTracker;

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

  /**
   * Determines whether the sheet is currently settling to a target {@link StableSheetState} using
   * {@link StateSettlingTracker}.
   */
  abstract boolean isSettling(View child, int state, boolean isReleasingView);

  /** Returns the sheet's offset from the origin edge when hidden. */
  abstract int getHiddenOffset();

  /** Returns the sheet's offset from the origin edge when expanded. */
  abstract int getExpandedOffset();

  /**
   * Calculates the target {@link StableSheetState} state of the sheet after it's released from a
   * drag, using the x and y velocity of the drag to determine the state.
   *
   * @return the target state
   */
  @StableSheetState
  abstract int calculateTargetStateOnViewReleased(
      @NonNull View releasedChild, float xVelocity, float yVelocity);

  /**
   * Sets the target sheet state from the {@link
   * SideSheetBehavior#onNestedPreScroll(CoordinatorLayout, View, View, int, int, int[], int)}
   * callback, based on scroll and position information provided by the method parameters.
   */
  abstract <V extends View> void setTargetStateOnNestedPreScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dx,
      int dy,
      @NonNull int[] consumed,
      int type);

  @StableSheetState
  abstract <V extends View> int calculateTargetStateOnStopNestedScroll(@NonNull V child);

  /**
   * Whether the sheet has reached the expanded offset at the end of a nested scroll, used to
   * determine when to set the {@link SideSheetBehavior.SheetState} to {@link
   * SideSheetBehavior.SheetState#STATE_EXPANDED}.
   */
  abstract <V extends View> boolean hasReachedExpandedOffset(@NonNull V child);

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
  abstract <V extends View> int getOutwardEdge(@NonNull V child);
}
