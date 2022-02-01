/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.behavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.core.view.ViewCompat;
import com.google.android.material.animation.AnimationUtils;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 */
public class HideBottomViewOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  protected static final int ENTER_ANIMATION_DURATION = 225;
  protected static final int EXIT_ANIMATION_DURATION = 175;

  private static final int STATE_SCROLLED_DOWN = 1;
  private static final int STATE_SCROLLED_UP = 2;

  private int height = 0;
  private int currentState = STATE_SCROLLED_UP;
  private int additionalHiddenOffsetY = 0;
  @Nullable private ViewPropertyAnimator currentAnimator;

  public HideBottomViewOnScrollBehavior() {}

  public HideBottomViewOnScrollBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    ViewGroup.MarginLayoutParams paramsCompat =
        (ViewGroup.MarginLayoutParams) child.getLayoutParams();
    height = child.getMeasuredHeight() + paramsCompat.bottomMargin;
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  /**
   * Sets an additional offset for the y position used to hide the view.
   *
   * @param child the child view that is hidden by this behavior
   * @param offset the additional offset in pixels that should be added when the view slides away
   */
  public void setAdditionalHiddenOffsetY(@NonNull V child, @Dimension int offset) {
    additionalHiddenOffsetY = offset;

    if (currentState == STATE_SCROLLED_DOWN) {
      child.setTranslationY(height + additionalHiddenOffsetY);
    }
  }

  @Override
  public boolean onStartNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View directTargetChild,
      @NonNull View target,
      int nestedScrollAxes,
      int type) {
    return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
  }

  @Override
  public void onNestedScroll(
      CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed,
      int type,
      @NonNull int[] consumed) {
    if (dyConsumed > 0) {
      slideDown(child);
    } else if (dyConsumed < 0) {
      slideUp(child);
    }
  }

  /** Returns true if the current state is scrolled up. */
  public boolean isScrolledUp() {
    return currentState == STATE_SCROLLED_UP;
  }

  /**
   * Performs an animation that will slide the child from it's current position to be totally on the
   * screen.
   */
  public void slideUp(@NonNull V child) {
    slideUp(child, /*animate=*/ true);
  }

  /**
   * Slides the child with or without animation from its current position to be totally on the
   * screen.
   *
   * @param animate {@code true} to slide with animation.
   */
  public void slideUp(@NonNull V child, boolean animate) {
    if (isScrolledUp()) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_UP;
    int targetTranslationY = 0;
    if (animate) {
      animateChildTo(
          child,
          targetTranslationY,
          ENTER_ANIMATION_DURATION,
          AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    } else {
      child.setTranslationY(targetTranslationY);
    }
  }

  /** Returns true if the current state is scrolled down. */
  public boolean isScrolledDown() {
    return currentState == STATE_SCROLLED_DOWN;
  }

  /**
   * Performs an animation that will slide the child from it's current position to be totally off
   * the screen.
   */
  public void slideDown(@NonNull V child) {
    slideDown(child, /*animate=*/ true);
  }

  /**
   * Slides the child with or without animation from its current position to be totally off the
   * screen.
   *
   * @param animate {@code true} to slide with animation.
   */
  public void slideDown(@NonNull V child, boolean animate) {
    if (isScrolledDown()) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_DOWN;
    int targetTranslationY = height + additionalHiddenOffsetY;
    if (animate) {
      animateChildTo(
          child,
          targetTranslationY,
          EXIT_ANIMATION_DURATION,
          AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    } else {
      child.setTranslationY(targetTranslationY);
    }
  }

  private void animateChildTo(
      @NonNull V child, int targetY, long duration, TimeInterpolator interpolator) {
    currentAnimator =
        child
            .animate()
            .translationY(targetY)
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(
                new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                    currentAnimator = null;
                  }
                });
  }
}
