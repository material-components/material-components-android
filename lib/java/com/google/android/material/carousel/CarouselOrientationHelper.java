/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.material.carousel;

import static com.google.android.material.carousel.CarouselLayoutManager.HORIZONTAL;
import static com.google.android.material.carousel.CarouselLayoutManager.VERTICAL;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Rect;
import android.graphics.RectF;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Orientation;
import android.view.View;

/**
 * A utility class that helps with calculating child offsets and mask coordinates based on
 * orientation.
 */
abstract class CarouselOrientationHelper {

  @RecyclerView.Orientation final int orientation;

  private CarouselOrientationHelper(@Orientation int orientation) {
    this.orientation = orientation;
  }

  /**
   * Creates an OrientationHelper for the given LayoutManager and orientation.
   *
   * @param layoutManager CarouselLayoutManager to attach to
   * @param orientation Desired orientation. Should be {@link CarouselLayoutManager#HORIZONTAL} or
   *     {@link CarouselLayoutManager#VERTICAL}
   * @return A new OrientationHelper
   */
  static CarouselOrientationHelper createOrientationHelper(
      CarouselLayoutManager layoutManager, @RecyclerView.Orientation int orientation) {
    switch (orientation) {
      case HORIZONTAL:
        return createHorizontalHelper(layoutManager);
      case VERTICAL:
        return createVerticalHelper(layoutManager);
      default: // fall out
    }
    throw new IllegalArgumentException("invalid orientation");
  }

  /** Returns the x-coordinate of the left edge of the parent recycler view. */
  abstract int getParentLeft();

  /**
   * Returns the coordinate of the start edge of the parent recycler view accounting for layout
   * direction. It returns the x-coordinate if horizontal and y-coordinate if vertical.
   */
  abstract int getParentStart();

  /** Returns the x-coordinate of the right edge of the parent recycler view. */
  abstract int getParentRight();

  /**
   * Returns the coordinate of the end edge of the parent recycler view accounting for layout
   * direction.
   */
  abstract int getParentEnd();

  /** Returns the y-coordinate of the top edge of the parent recycler view. */
  abstract int getParentTop();

  /** Returns the y-coordinate of the bottom edge of the parent recycler view. */
  abstract int getParentBottom();


  /**
   * Returns the space occupied by this View in the cross (non-scrolling) axis including
   * decorations and margins.
   *
   * @param child The view element to check
   * @return total space occupied by this view in the perpendicular orientation to current one
   */
  abstract int getDecoratedCrossAxisMeasurement(View child);

  /**
   * Helper method that calls {@link CarouselLayoutManager#layoutDecoratedWithMargins(View, int,
   * int, int, int)} with the correct coordinates according to the orientation.
   *
   * @param child the child to lay out.
   * @param start the coordinate of the starting edge, with item decoration insets and margin
   *     included. The axis depends on the orientation.
   * @param end the coordinate of the ending edge, with item decoration insets and margin included.
   *     The axis depends on the orientation.
   */
  abstract void layoutDecoratedWithMargins(View child, int start, int end);

  /**
   * Returns the margins on the orientation axis.
   *
   * @param layoutParams the LayoutParams to derive the margins from.
   * @return Margins according to orientation axis.
   */
  abstract float getMaskMargins(LayoutParams layoutParams);

  /**
   * Returns the mask rect with coordinates according to the orientation.
   *
   * @param childHeight height of the view to mask.
   * @param childWidth width of the view to mask.
   * @param maskHeight height of the mask.
   * @param maskWidth width of the mask.
   * @return RectF with coordinates according to orientation.
   */
  abstract RectF getMaskRect(
      float childHeight, float childWidth, float maskHeight, float maskWidth);

  /**
   * Helper method to adjust the given maskRect to be within the given bounds.
   *
   * @param maskRect the Rect to update to be within the bounds.
   * @param offsetMaskRect values of maskRect updated to reflect the offset within the RecyclerView
   *     to compare against the bounds.
   * @param boundsRect contains the values of the bounds to which the offset maskRect should stay
   *     within.
   */
  abstract void containMaskWithinBounds(RectF maskRect, RectF offsetMaskRect, RectF boundsRect);

  /**
   * Helper method to move a mask to outside of the bounds if it is right on the edge. Masks on the
   * edge are pushed outside of the bounds.
   *
   * @param maskRect the Rect to update to be outside the bounds if on the edge.
   * @param offsetMaskRect values of maskRect updated to reflect the offset within the RecyclerView
   *     to compare against the bounds.
   * @param boundsRect contains the values of the bounds to which the offset maskRect should be
   *     outside of if on the edge.
   */
  abstract void moveMaskOnEdgeOutsideBounds(RectF maskRect, RectF offsetMaskRect, RectF boundsRect);

  /**
   * Helper method that offsets the view in the direction according to the orientation.
   *
   * @param child view to offset.
   * @param boundsRect bounds to calculate starting position.
   * @param halfItemSize half the recycler view item size to calculate the center of the view.
   * @param offsetCenter calculated offset view's center.
   */
  abstract void offsetChild(View child, Rect boundsRect, float halfItemSize, float offsetCenter);

  private static CarouselOrientationHelper createVerticalHelper(
      CarouselLayoutManager carouselLayoutManager) {
    return new CarouselOrientationHelper(VERTICAL) {
      @Override
      int getParentLeft() {
        return carouselLayoutManager.getPaddingLeft();
      }

      @Override
      int getParentStart() {
        return getParentTop();
      }

      @Override
      int getParentRight() {
        // If orientation is vertical, we want to subtract padding from the right.
        return carouselLayoutManager.getWidth() - carouselLayoutManager.getPaddingRight();
      }

      @Override
      int getParentEnd() {
        return getParentBottom();
      }

      @Override
      int getParentTop() {
        return 0;
      }

      @Override
      int getParentBottom() {
        return carouselLayoutManager.getHeight();
      }

      @Override
      int getDecoratedCrossAxisMeasurement(View child) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
            child.getLayoutParams();
        return carouselLayoutManager.getDecoratedMeasuredWidth(child) + params.leftMargin
            + params.rightMargin;
      }

      @Override
      public void layoutDecoratedWithMargins(View child, int head, int tail) {
        int left = getParentLeft();
        int right = left + getDecoratedCrossAxisMeasurement(child);
        carouselLayoutManager.layoutDecoratedWithMargins(
            child,
            /* left= */ left,
            /* top= */ head,
            /* right= */ right,
            /* bottom= */ tail);
      }

      @Override
      public float getMaskMargins(LayoutParams layoutParams) {
        return layoutParams.topMargin + layoutParams.bottomMargin;
      }

      @Override
      public RectF getMaskRect(
          float childHeight, float childWidth, float maskHeight, float maskWidth) {
        return new RectF(0F, maskHeight, childWidth, childHeight - maskHeight);
      }

      @Override
      public void containMaskWithinBounds(RectF maskRect, RectF offsetMaskRect, RectF boundsRect) {
        if (offsetMaskRect.top < boundsRect.top && offsetMaskRect.bottom > boundsRect.top) {
          float diff = boundsRect.top - offsetMaskRect.top;
          maskRect.top += diff;
          boundsRect.top += diff;
        }

        if (offsetMaskRect.bottom > boundsRect.bottom && offsetMaskRect.top < boundsRect.bottom) {
          float diff = offsetMaskRect.bottom - boundsRect.bottom;
          maskRect.bottom = max(maskRect.bottom - diff, maskRect.top);
          offsetMaskRect.bottom = max(offsetMaskRect.bottom - diff, offsetMaskRect.top);
        }
      }

      @Override
      public void moveMaskOnEdgeOutsideBounds(
          RectF maskRect, RectF offsetMaskRect, RectF parentBoundsRect) {
        if (offsetMaskRect.bottom <= parentBoundsRect.top) {
          maskRect.bottom = (float) Math.floor(maskRect.bottom) - 1;
          // Make sure that maskRect.top is equal to or smaller than maskRect.bottom.
          // Otherwise the mask may not be re-drawn as the mask is invalid.
          maskRect.top = min(maskRect.top, maskRect.bottom);
        }
        if (offsetMaskRect.top >= parentBoundsRect.bottom) {
          maskRect.top = (float) Math.ceil(maskRect.top) + 1;
          // Make sure that maskRect.bottom is equal to or bigger than maskRect.top.
          // Otherwise the mask may not be re-drawn as the mask is invalid.
          maskRect.bottom = max(maskRect.top, maskRect.bottom);
        }
      }

      @Override
      public void offsetChild(View child, Rect boundsRect, float halfItemSize, float offsetCenter) {
        float actualCy = boundsRect.top + halfItemSize;
        child.offsetTopAndBottom((int) (offsetCenter - actualCy));
      }
    };
  }

  private static CarouselOrientationHelper createHorizontalHelper(
      CarouselLayoutManager carouselLayoutManager) {
    return new CarouselOrientationHelper(HORIZONTAL) {
      @Override
      int getParentLeft() {
        return 0;
      }

      @Override
      int getParentStart() {
        return carouselLayoutManager.isLayoutRtl() ? getParentRight() : getParentLeft();
      }

      @Override
      int getParentRight() {
        return carouselLayoutManager.getWidth();
      }

      @Override
      int getParentEnd() {
        return carouselLayoutManager.isLayoutRtl() ? getParentLeft() : getParentRight();
      }

      @Override
      int getParentTop() {
        return carouselLayoutManager.getPaddingTop();
      }

      @Override
      int getParentBottom() {
        return carouselLayoutManager.getHeight() - carouselLayoutManager.getPaddingBottom();
      }

      @Override
      int getDecoratedCrossAxisMeasurement(View child) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
            child.getLayoutParams();
        return carouselLayoutManager.getDecoratedMeasuredHeight(child) + params.topMargin
            + params.bottomMargin;
      }

      @Override
      public void layoutDecoratedWithMargins(View child, int head, int tail) {
        int top = getParentTop();
        int bottom = top + getDecoratedCrossAxisMeasurement(child);
        carouselLayoutManager.layoutDecoratedWithMargins(
            child,
            /* left= */ head,
            /* top= */ top,
            /* right= */ tail,
            /* bottom= */ bottom);
      }

      @Override
      public float getMaskMargins(LayoutParams layoutParams) {
        return layoutParams.rightMargin + layoutParams.leftMargin;
      }

      @Override
      public RectF getMaskRect(
          float childHeight, float childWidth, float maskHeight, float maskWidth) {
        return new RectF(maskWidth, 0F, (childWidth - maskWidth), childHeight);
      }

      @Override
      public void containMaskWithinBounds(RectF maskRect, RectF offsetMaskRect, RectF boundsRect) {
        if (offsetMaskRect.left < boundsRect.left && offsetMaskRect.right > boundsRect.left) {
          float diff = boundsRect.left - offsetMaskRect.left;
          maskRect.left += diff;
          offsetMaskRect.left += diff;
        }

        if (offsetMaskRect.right > boundsRect.right && offsetMaskRect.left < boundsRect.right) {
          float diff = offsetMaskRect.right - boundsRect.right;
          maskRect.right = max(maskRect.right - diff, maskRect.left);
          offsetMaskRect.right = max(offsetMaskRect.right - diff, offsetMaskRect.left);
        }
      }

      @Override
      public void moveMaskOnEdgeOutsideBounds(
          RectF maskRect, RectF offsetMaskRect, RectF parentBoundsRect) {
        if (offsetMaskRect.right <= parentBoundsRect.left) {
          maskRect.right = (float) Math.floor(maskRect.right) - 1;
          // Make sure that maskRect.left is equal to or smaller than maskRect.right.
          // Otherwise the mask may not be re-drawn as the mask is invalid.
          maskRect.left = min(maskRect.left, maskRect.right);
        }
        if (offsetMaskRect.left >= parentBoundsRect.right) {
          maskRect.left = (float) Math.ceil(maskRect.left) + 1;
          // Make sure that maskRect.right is equal to or bigger than maskRect.left.
          // Otherwise the mask may not be re-drawn as the mask is invalid.
          maskRect.right = max(maskRect.left, maskRect.right);
        }
      }

      @Override
      public void offsetChild(View child, Rect boundsRect, float halfItemSize, float offsetCenter) {
        float actualCx = boundsRect.left + halfItemSize;
        child.offsetLeftAndRight((int) (offsetCenter - actualCx));
      }
    };
  }
}
