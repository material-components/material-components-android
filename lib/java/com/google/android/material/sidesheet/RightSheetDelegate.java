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

import static java.lang.Math.max;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.sidesheet.Sheet.SheetEdge;

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

  /** Returns the sheet's offset in pixels from the inner edge when hidden. */
  @Override
  int getHiddenOffset() {
    // Return the parent's width in pixels, which results in the sheet being offset entirely off of
    // screen.
    return sheetBehavior.getParentWidth();
  }

  /** Returns the sheet's offset in pixels from the inner edge when expanded. */
  @Override
  int getExpandedOffset() {
    // Calculate the expanded offset based on the width of the content.
    return max(
        0, getHiddenOffset() - sheetBehavior.getChildWidth() - sheetBehavior.getInnerMargin());
  }

  /** Whether the view has been released from a drag close to the inner edge. */
  @Override
  boolean isReleasedCloseToInnerEdge(@NonNull View releasedChild) {
    // To be considered released close to the inner (right) edge, the released child's left must
    // be at least halfway to the inner (right) edge of the screen.
    return releasedChild.getLeft() > (getHiddenOffset() + getExpandedOffset()) / 2;
  }

  @Override
  boolean isSwipeSignificant(float xVelocity, float yVelocity) {
    return SheetUtils.isSwipeMostlyHorizontal(xVelocity, yVelocity)
        && Math.abs(xVelocity) > sheetBehavior.getSignificantVelocityThreshold();
  }

  @Override
  boolean shouldHide(@NonNull View child, float velocity) {
    final float newRight = child.getRight() + velocity * sheetBehavior.getHideFriction();
    return Math.abs(newRight) > sheetBehavior.getHideThreshold();
  }

  @Override
  <V extends View> int getOuterEdge(@NonNull V child) {
    return child.getLeft() - sheetBehavior.getInnerMargin();
  }

  @Override
  float calculateSlideOffset(int left) {
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

  @Override
  void updateCoplanarSiblingAdjacentMargin(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int coplanarSiblingAdjacentMargin) {
    coplanarSiblingLayoutParams.rightMargin = coplanarSiblingAdjacentMargin;
  }

  @Override
  int getCoplanarSiblingAdjacentMargin(@NonNull MarginLayoutParams coplanarSiblingLayoutParams) {
    return coplanarSiblingLayoutParams.rightMargin;
  }

  @Override
  public int getParentInnerEdge(@NonNull CoordinatorLayout parent) {
    return parent.getRight();
  }

  @Override
  int calculateInnerMargin(@NonNull MarginLayoutParams marginLayoutParams) {
    return marginLayoutParams.rightMargin;
  }

  @Override
  int getMinViewPositionHorizontal() {
    return getExpandedOffset();
  }

  @Override
  int getMaxViewPositionHorizontal() {
    return sheetBehavior.getParentWidth();
  }

  @Override
  boolean isExpandingOutwards(float xVelocity) {
    return xVelocity < 0;
  }
}
