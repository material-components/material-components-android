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

import static java.lang.Math.max;

import android.graphics.PointF;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.SmoothScroller;
import androidx.recyclerview.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Implementation of the {@link SnapHelper} that supports snapping items to the carousel keylines
 * according to the strategy.
 */
public class CarouselSnapHelper extends SnapHelper {

  private static final float HORIZONTAL_SNAP_SPEED = 100F;

  private static final float VERTICAL_SNAP_SPEED = 50F;

  private final boolean disableFling;
  private RecyclerView recyclerView;

  public CarouselSnapHelper() {
    this(true);
  }

  public CarouselSnapHelper(boolean disableFling) {
    this.disableFling = disableFling;
  }

  @Override
  public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
    super.attachToRecyclerView(recyclerView);
    this.recyclerView = recyclerView;
  }

  @Nullable
  @Override
  public int[] calculateDistanceToFinalSnap(
      @NonNull LayoutManager layoutManager, @NonNull View view) {
    return calculateDistanceToSnap(layoutManager, view, false);
  }

  private int[] calculateDistanceToSnap(
      @NonNull LayoutManager layoutManager, @NonNull View view, boolean partialSnap) {
    // If the layout manager is not a CarouselLayoutManager, we return with a zero offset
    // as there are no keylines to snap to.
    if (!(layoutManager instanceof CarouselLayoutManager)) {
      return new int[] {0, 0};
    }

    int offset =
        distanceToFirstFocalKeyline(view, (CarouselLayoutManager) layoutManager, partialSnap);
    if (layoutManager.canScrollHorizontally()) {
      return new int[] {offset, 0};
    }

    if (layoutManager.canScrollVertically()) {
      return new int[] {0, offset};
    }
    return new int[] {0, 0};
  }

  private int distanceToFirstFocalKeyline(
      @NonNull View targetView, CarouselLayoutManager layoutManager, boolean partialSnap) {
    return layoutManager.getOffsetToScrollToPositionForSnap(
        layoutManager.getPosition(targetView), partialSnap);
  }

  @Nullable
  @Override
  public View findSnapView(LayoutManager layoutManager) {
    return findViewNearestFirstKeyline(layoutManager);
  }

  /**
   * Return the child view that is currently closest to the first focal keyline.
   *
   * @param layoutManager The {@link LayoutManager} associated with the attached {@link
   *     RecyclerView}.
   * @return the child view that is currently closest to the first focal keyline.
   */
  @Nullable
  private View findViewNearestFirstKeyline(LayoutManager layoutManager) {
    int childCount = layoutManager.getChildCount();
    if (childCount == 0 || !(layoutManager instanceof CarouselLayoutManager)) {
      return null;
    }
    View closestChild = null;
    int absClosest = Integer.MAX_VALUE;

    CarouselLayoutManager carouselLayoutManager = (CarouselLayoutManager) layoutManager;
    for (int i = 0; i < childCount; i++) {
      final View child = layoutManager.getChildAt(i);
      final int position = layoutManager.getPosition(child);
      final int offset =
          Math.abs(carouselLayoutManager.getOffsetToScrollToPositionForSnap(position, false));

      // If child center is closer than previous closest, set it as closest
      if (offset < absClosest) {
        absClosest = offset;
        closestChild = child;
      }
    }
    return closestChild;
  }

  @Override
  public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX, int velocityY) {
    if (!disableFling) {
      return RecyclerView.NO_POSITION;
    }

    final int itemCount = layoutManager.getItemCount();
    if (itemCount == 0) {
      return RecyclerView.NO_POSITION;
    }

    // A child that is exactly centered on the first focal keyline is eligible
    // for both before and after
    View closestChildBeforeKeyline = null;
    int distanceBefore = Integer.MIN_VALUE;
    View closestChildAfterKeyline = null;
    int distanceAfter = Integer.MAX_VALUE;

    // Find the first view before the first focal keyline, and the first view after it
    final int childCount = layoutManager.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = layoutManager.getChildAt(i);
      if (child == null) {
        continue;
      }
      final int distance =
          distanceToFirstFocalKeyline(child, (CarouselLayoutManager) layoutManager, false);

      if (distance <= 0 && distance > distanceBefore) {
        // Child is before the keyline and closer then the previous best
        distanceBefore = distance;
        closestChildBeforeKeyline = child;
      }
      if (distance >= 0 && distance < distanceAfter) {
        // Child is after the keyline and closer then the previous best
        distanceAfter = distance;
        closestChildAfterKeyline = child;
      }
    }

    // Return the position of the closest child from the first focal keyline, in the direction of
    // the fling
    final boolean forwardDirection = isForwardFling(layoutManager, velocityX, velocityY);
    if (forwardDirection && closestChildAfterKeyline != null) {
      return layoutManager.getPosition(closestChildAfterKeyline);
    } else if (!forwardDirection && closestChildBeforeKeyline != null) {
      return layoutManager.getPosition(closestChildBeforeKeyline);
    }

    // There is no child in the direction of the fling (eg. start/end of list).
    // Extrapolate from the child that is visible to get the position of the view to
    // snap to.
    View visibleView = forwardDirection ? closestChildBeforeKeyline : closestChildAfterKeyline;
    if (visibleView == null) {
      return RecyclerView.NO_POSITION;
    }
    int visiblePosition = layoutManager.getPosition(visibleView);
    int snapToPosition =
        visiblePosition + (isReverseLayout(layoutManager) == forwardDirection ? -1 : 1);

    if (snapToPosition < 0 || snapToPosition >= itemCount) {
      return RecyclerView.NO_POSITION;
    }
    return snapToPosition;
  }

  private boolean isForwardFling(
      RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
    if (layoutManager.canScrollHorizontally()) {
      return velocityX > 0;
    } else {
      return velocityY > 0;
    }
  }

  // Calculates the direction of the layout based on the direction of the scroll vector when
  // scrolling to the end of the list. This is not equivalent to `isRtl` because the recyclerview
  // layout manager may set `reverseLayout`.
  private boolean isReverseLayout(RecyclerView.LayoutManager layoutManager) {
    final int itemCount = layoutManager.getItemCount();
    if ((layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
      RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
          (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
      PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
      if (vectorForEnd != null) {
        return vectorForEnd.x < 0 || vectorForEnd.y < 0;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This is mostly a copy of {@code SnapHelper#createSnapScroller} with a slight adjustment to
   * call {@link CarouselSnapHelper#calculateDistanceToSnap(LayoutManager, View, boolean)}
   * (LayoutManager, View)}. We want to do a partial snap since the correct target keyline state may
   * not have updated yet since this gets called before the keylines shift.
   */
  @Nullable
  @Override
  protected SmoothScroller createScroller(@NonNull LayoutManager layoutManager) {
    return layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider
        ? new LinearSmoothScroller(recyclerView.getContext()) {
          @Override
          protected void onTargetFound(
              View targetView,
              RecyclerView.State state,
              RecyclerView.SmoothScroller.Action action) {
            if (recyclerView != null) {
              int[] snapDistances =
                  calculateDistanceToSnap(recyclerView.getLayoutManager(), targetView, true);
              int dx = snapDistances[0];
              int dy = snapDistances[1];
              int time = this.calculateTimeForDeceleration(max(Math.abs(dx), Math.abs(dy)));
              if (time > 0) {
                action.update(dx, dy, time, this.mDecelerateInterpolator);
              }
            }
          }

          @Override
          protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            // If the carousel orientation is vertical, we want the scroll speed to be faster.
            if (layoutManager.canScrollVertically()) {
              return VERTICAL_SNAP_SPEED / (float) displayMetrics.densityDpi;
            }
            return HORIZONTAL_SNAP_SPEED / (float) displayMetrics.densityDpi;
          }
        }
        : null;
  }
}
