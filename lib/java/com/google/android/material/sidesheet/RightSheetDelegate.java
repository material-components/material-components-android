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

import static com.google.android.material.sidesheet.Sheet.STATE_EXPANDED;
import static com.google.android.material.sidesheet.Sheet.STATE_HIDDEN;
import static java.lang.Math.max;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.sidesheet.Sheet.SheetEdge;
import com.google.android.material.sidesheet.Sheet.StableSheetState;

/**
 * A delegate for {@link SideSheetBehavior} to handle positioning logic for sheets based on the
 * right edge of the screen that expand from right to left.
 */
final class RightSheetDelegate extends SheetDelegate {

  final SideSheetBehavior<? extends View> sheetBehavior;

  RightSheetDelegate(@NonNull SideSheetBehavior<? extends View> sheetBehavior) {
    this.sheetBehavior = sheetBehavior;
  }

  @SheetEdge
  @Override
  int getSheetEdge() {
    return SideSheetBehavior.EDGE_RIGHT;
  }

  /** Returns the sheet's offset in pixels from the origin edge when hidden. */
  @Override
  int getHiddenOffset() {
    // Return the parent's width in pixels, which results in the sheet being offset entirely off of
    // screen.
    return sheetBehavior.getParentWidth();
  }

  /** Returns the sheet's offset in pixels from the origin edge when expanded. */
  @Override
  int getExpandedOffset() {
    // Calculate the expanded offset based on the width of the content.
    return max(0, getHiddenOffset() - sheetBehavior.getChildWidth());
  }

  /** Whether the view has been released from a drag close to the origin edge. */
  private boolean isReleasedCloseToOriginEdge(@NonNull View releasedChild) {
    // To be considered released close to the origin (right) edge, the released child's left must
    // be at least halfway to the origin (right) edge.
    return releasedChild.getLeft() > (getHiddenOffset() - getExpandedOffset()) / 2;
  }

  @Override
  @StableSheetState
  int calculateTargetStateOnViewReleased(
      @NonNull View releasedChild, float xVelocity, float yVelocity) {
    @StableSheetState int targetState;
    if (xVelocity < 0) { // Moving left, expanding outwards.
      targetState = STATE_EXPANDED;

    } else if (shouldHide(releasedChild, xVelocity)) {
      // Hide if the view was either released close to the origin/right edge or it was a significant
      // horizontal swipe; otherwise settle to expanded state.
      if (isSwipeSignificant(xVelocity, yVelocity) || isReleasedCloseToOriginEdge(releasedChild)) {
        targetState = STATE_HIDDEN;
      } else {
        targetState = STATE_EXPANDED;
      }
    } else if (xVelocity == 0f || !SheetUtils.isSwipeMostlyHorizontal(xVelocity, yVelocity)) {
      // If the X velocity is 0 or the swipe was mostly vertical, indicated by the Y
      // velocity being greater than the X velocity, settle to the nearest correct state.
      int currentLeft = releasedChild.getLeft();
      if (Math.abs(currentLeft - getExpandedOffset()) < Math.abs(currentLeft - getHiddenOffset())) {
        targetState = STATE_EXPANDED;
      } else {
        targetState = STATE_HIDDEN;
      }
    } else { // Moving right; collapse inwards and hide.
      targetState = STATE_HIDDEN;
    }
    return targetState;
  }

  private boolean isSwipeSignificant(float xVelocity, float yVelocity) {
    return SheetUtils.isSwipeMostlyHorizontal(xVelocity, yVelocity)
        && yVelocity > sheetBehavior.getSignificantVelocityThreshold();
  }

  @Override
  boolean shouldHide(@NonNull View child, float velocity) {
    final float newRight = child.getRight() + velocity * sheetBehavior.getHideFriction();
    return Math.abs(newRight) > sheetBehavior.getHideThreshold();
  }

  @Override
  boolean isSettling(View child, int state, boolean isReleasingView) {
    int left = sheetBehavior.getOutwardEdgeOffsetForState(state);
    ViewDragHelper viewDragHelper = sheetBehavior.getViewDragHelper();
    return viewDragHelper != null
        && (isReleasingView
            ? viewDragHelper.settleCapturedViewAt(left, child.getTop())
            : viewDragHelper.smoothSlideViewTo(child, left, child.getTop()));
  }

  @Override
  <V extends View> int getOutwardEdge(@NonNull V child) {
    return child.getLeft();
  }

  @Override
  float calculateSlideOffsetBasedOnOutwardEdge(int left) {
    float hiddenOffset = getHiddenOffset();
    float sheetWidth = hiddenOffset - getExpandedOffset();

    return (hiddenOffset - left) / sheetWidth;
  }

  @Override
  void updateCoplanarSiblingLayoutParams(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int sheetLeft, int sheetRight) {
    int parentWidth = sheetBehavior.getParentWidth();

    // Wait until the sheet partially enters the screen to avoid an initial content jump to the
    // right edge of the screen.
    if (sheetLeft <= parentWidth) {
      coplanarSiblingLayoutParams.rightMargin = parentWidth - sheetLeft;
    }
  }
}
