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

import android.content.Context;
import android.graphics.Rect;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.core.math.MathUtils;
import java.util.List;

/**
 * The {@link Behavior} for a scrolling view that is positioned vertically below another view. See
 * {@link com.google.android.material.appbar.HeaderBehavior}.
 */
abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {

  final Rect tempRect1 = new Rect();
  final Rect tempRect2 = new Rect();

  private int verticalLayoutGap = 0;
  private int overlayTop;

  public HeaderScrollingViewBehavior() {}

  public HeaderScrollingViewBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onMeasureChild(
      @NonNull CoordinatorLayout parent,
      @NonNull View child,
      int parentWidthMeasureSpec,
      int widthUsed,
      int parentHeightMeasureSpec,
      int heightUsed) {
    final int childLpHeight = child.getLayoutParams().height;
    if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
        || childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
      // If the menu's height is set to match_parent/wrap_content then measure it
      // with the maximum visible height

      final List<View> dependencies = parent.getDependencies(child);
      final View header = findFirstDependency(dependencies);
      if (header != null) {
        int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
        if (availableHeight > 0) {
          if (ViewCompat.getFitsSystemWindows(header)) {
            final WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
            if (parentInsets != null) {
              availableHeight += parentInsets.getSystemWindowInsetTop()
                  + parentInsets.getSystemWindowInsetBottom();
            }
          }
        } else {
          // If the measure spec doesn't specify a size, use the current height
          availableHeight = parent.getHeight();
        }

        int height = availableHeight + getScrollRange(header);
        int headerHeight = header.getMeasuredHeight();
        if (shouldHeaderOverlapScrollingChild()) {
          child.setTranslationY(-headerHeight);
        } else {
          height -= headerHeight;
        }
        final int heightMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(
                height,
                childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
                    ? View.MeasureSpec.EXACTLY
                    : View.MeasureSpec.AT_MOST);

        // Now measure the scrolling view with the correct height
        parent.onMeasureChild(
            child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

        return true;
      }
    }
    return false;
  }

  @Override
  protected void layoutChild(
      @NonNull final CoordinatorLayout parent,
      @NonNull final View child,
      final int layoutDirection) {
    final List<View> dependencies = parent.getDependencies(child);
    final View header = findFirstDependency(dependencies);

    if (header != null) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      final Rect available = tempRect1;
      available.set(
          parent.getPaddingLeft() + lp.leftMargin,
          header.getBottom() + lp.topMargin,
          parent.getWidth() - parent.getPaddingRight() - lp.rightMargin,
          parent.getHeight() + header.getBottom() - parent.getPaddingBottom() - lp.bottomMargin);

      final WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
      if (parentInsets != null
          && ViewCompat.getFitsSystemWindows(parent)
          && !ViewCompat.getFitsSystemWindows(child)) {
        // If we're set to handle insets but this child isn't, then it has been measured as
        // if there are no insets. We need to lay it out to match horizontally.
        // Top and bottom and already handled in the logic above
        available.left += parentInsets.getSystemWindowInsetLeft();
        available.right -= parentInsets.getSystemWindowInsetRight();
      }

      final Rect out = tempRect2;
      GravityCompat.apply(
          resolveGravity(lp.gravity),
          child.getMeasuredWidth(),
          child.getMeasuredHeight(),
          available,
          out,
          layoutDirection);

      final int overlap = getOverlapPixelsForOffset(header);

      child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
      verticalLayoutGap = out.top - header.getBottom();
    } else {
      // If we don't have a dependency, let super handle it
      super.layoutChild(parent, child, layoutDirection);
      verticalLayoutGap = 0;
    }
  }

  protected boolean shouldHeaderOverlapScrollingChild() {
    return false;
  }

  float getOverlapRatioForOffset(final View header) {
    return 1f;
  }

  final int getOverlapPixelsForOffset(final View header) {
    return overlayTop == 0
        ? 0
        : MathUtils.clamp((int) (getOverlapRatioForOffset(header) * overlayTop), 0, overlayTop);
  }

  private static int resolveGravity(int gravity) {
    return gravity == Gravity.NO_GRAVITY ? GravityCompat.START | Gravity.TOP : gravity;
  }

  @Nullable
  abstract View findFirstDependency(List<View> views);

  int getScrollRange(@NonNull View v) {
    return v.getMeasuredHeight();
  }

  /**
   * The gap between the top of the scrolling view and the bottom of the header layout in pixels.
   */
  final int getVerticalLayoutGap() {
    return verticalLayoutGap;
  }

  /**
   * Set the distance that this view should overlap any {@link
   * com.google.android.material.appbar.AppBarLayout}.
   *
   * @param overlayTop the distance in px
   */
  public final void setOverlayTop(int overlayTop) {
    this.overlayTop = overlayTop;
  }

  /**
   * Returns the distance that this view should overlap any {@link
   * com.google.android.material.appbar.AppBarLayout}.
   */
  public final int getOverlayTop() {
    return overlayTop;
  }
}
