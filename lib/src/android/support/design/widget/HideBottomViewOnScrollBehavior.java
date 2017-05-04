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

package android.support.design.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 */
public class HideBottomViewOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  private static final int ANIMATION_DURATION = 200;

  private int height = 0;

  /** Default constructor for instantiating HideBottomViewOnScrollBehaviors. */
  public HideBottomViewOnScrollBehavior() {}

  /**
   * Default constructor for inflating HideBottomViewOnScrollBehaviors from layout.
   *
   * @param context The {@link Context}.
   * @param attrs The {@link AttributeSet}.
   */
  public HideBottomViewOnScrollBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
    height = child.getMeasuredHeight();
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  @Override
  public boolean onStartNestedScroll(
      CoordinatorLayout coordinatorLayout,
      V child,
      View directTargetChild,
      View target,
      int nestedScrollAxes) {
    return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
  }

  @Override
  public void onNestedScroll(
      CoordinatorLayout coordinatorLayout,
      V child,
      View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed) {
    if (dyConsumed > 0) {
      slideDown(child);
    } else if (dyConsumed < 0) {
      slideUp(child);
    }
  }

  private void slideUp(V child) {
    child.clearAnimation();
    child
        .animate()
        .translationY(0)
        .setInterpolator(new LinearOutSlowInInterpolator())
        .setDuration(ANIMATION_DURATION);
  }

  private void slideDown(V child) {
    child.clearAnimation();
    child
        .animate()
        .translationY(height)
        .setInterpolator(new FastOutLinearInInterpolator())
        .setDuration(ANIMATION_DURATION);
  }
}
