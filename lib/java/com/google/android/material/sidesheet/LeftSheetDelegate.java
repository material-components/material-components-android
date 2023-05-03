/*
 * Copyright (C) 2023 The Android Open Source Project
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
 * A delegate for {@link SideSheetBehavior} to handle positioning logic for sheets based on the left
 * edge of the screen that expand from left to right.
 */
final class LeftSheetDelegate extends SheetDelegate {

  final SideSheetBehavior<? extends View> sheetBehavior;

  LeftSheetDelegate(@NonNull SideSheetBehavior<? extends View> sheetBehavior) {
    this.sheetBehavior = sheetBehavior;
  }

  @SheetEdge
  @Override
  int getSheetEdge() {
    return SideSheetBehavior.EDGE_LEFT;
  }

  /** Returns the sheet's offset in pixels from the inner edge when hidden. */
  @Override
  int getHiddenOffset() {
    // Return the parent's width in pixels, which results in the sheet being offset entirely off of
    // screen.
    return -sheetBehavior.getChildWidth() - sheetBehavior.getInnerMargin();
  }

  /** Returns the sheet's offset in pixels from the inner edge when expanded. */
  @Override
  int getExpandedOffset() {
    // Calculate the expanded offset based on the width of the content.
    return max(0, sheetBehavior.getParentInnerEdge() + sheetBehavior.getInnerMargin());
  }

  /** Whether the view has been released from a drag close to the inner edge. */
  @Override
  boolean isReleasedCloseToInnerEdge(@NonNull View releasedChild) {
    // To be considered released close to the inner (left) edge, the released child's right must
    // be at least halfway to the inner (left) edge of the screen.
    return releasedChild.getRight() < (getExpandedOffset() - getHiddenOffset()) / 2;
  }

  @Override
  boolean isSwipeSignificant(float xVelocity, float yVelocity) {
    return SheetUtils.isSwipeMostlyHorizontal(xVelocity, yVelocity)
        && Math.abs(xVelocity) > sheetBehavior.getSignificantVelocityThreshold();
  }

  @Override
  boolean shouldHide(@NonNull View child, float velocity) {
    final float newLeft = child.getLeft() + velocity * sheetBehavior.getHideFriction();
    return Math.abs(newLeft) > sheetBehavior.getHideThreshold();
  }

  @Override
  <V extends View> int getOuterEdge(@NonNull V child) {
    return child.getRight() + sheetBehavior.getInnerMargin();
  }

  @Override
  float calculateSlideOffset(int left) {
    float hiddenOffset = getHiddenOffset();
    float sheetWidth = getExpandedOffset() - hiddenOffset;

    return (left - hiddenOffset) / sheetWidth;
  }

  @Override
  void updateCoplanarSiblingLayoutParams(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int sheetLeft, int sheetRight) {
    int parentWidth = sheetBehavior.getParentWidth();

    // Wait until the sheet partially enters the screen to avoid an initial content jump to the
    // right edge of the screen.
    if (sheetLeft <= parentWidth) {
      coplanarSiblingLayoutParams.leftMargin = sheetRight;
    }
  }

  @Override
  void updateCoplanarSiblingAdjacentMargin(
      @NonNull MarginLayoutParams coplanarSiblingLayoutParams, int coplanarSiblingAdjacentMargin) {
    coplanarSiblingLayoutParams.leftMargin = coplanarSiblingAdjacentMargin;
  }

  @Override
  int getCoplanarSiblingAdjacentMargin(@NonNull MarginLayoutParams coplanarSiblingLayoutParams) {
    return coplanarSiblingLayoutParams.leftMargin;
  }

  @Override
  public int getParentInnerEdge(@NonNull CoordinatorLayout parent) {
    return parent.getLeft();
  }

  @Override
  int calculateInnerMargin(@NonNull MarginLayoutParams marginLayoutParams) {
    return marginLayoutParams.leftMargin;
  }

  @Override
  int getMinViewPositionHorizontal() {
    return -sheetBehavior.getChildWidth();
  }

  @Override
  int getMaxViewPositionHorizontal() {
    return sheetBehavior.getInnerMargin();
  }

  @Override
  boolean isExpandingOutwards(float xVelocity) {
    return xVelocity > 0;
  }
}
