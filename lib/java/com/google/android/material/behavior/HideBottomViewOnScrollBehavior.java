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

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.content.ContextCompat.getSystemService;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.core.view.ViewCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MotionUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 *
 * <p>If Touch Exploration is enabled, the hide on scroll behavior should be disabled until Touch
 * Exploration is disabled. Ensure that the content is not obscured due to disabling this behavior
 * by adding padding to the content.
 *
 * @deprecated Use {@link HideViewOnScrollBehavior} instead.
 *     <p>TODO(b/378132394): Migrate usages of this class to {@link HideViewOnScrollBehavior}.
 */
@Deprecated
public class HideBottomViewOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  /**
   * Interface definition for a listener to be notified when the bottom view scroll state changes.
   */
  public interface OnScrollStateChangedListener {

    /**
     * Called when the bottom view changes its scrolled state.
     *
     * @param bottomView The bottom view.
     * @param newState The new state. This will be one of {@link #STATE_SCROLLED_UP} or {@link
     *     #STATE_SCROLLED_DOWN}.
     */
    void onStateChanged(@NonNull View bottomView, @ScrollState int newState);
  }

  @NonNull
  private final LinkedHashSet<OnScrollStateChangedListener> onScrollStateChangedListeners =
      new LinkedHashSet<>();

  private static final int DEFAULT_ENTER_ANIMATION_DURATION_MS = 225;
  private static final int DEFAULT_EXIT_ANIMATION_DURATION_MS = 175;
  private static final int ENTER_ANIM_DURATION_ATTR = R.attr.motionDurationLong2;
  private static final int EXIT_ANIM_DURATION_ATTR = R.attr.motionDurationMedium4;
  private static final int ENTER_EXIT_ANIM_EASING_ATTR = R.attr.motionEasingEmphasizedInterpolator;

  private int enterAnimDuration;
  private int exitAnimDuration;
  private TimeInterpolator enterAnimInterpolator;
  private TimeInterpolator exitAnimInterpolator;

  /** State of the bottom view when it's scrolled down. */
  public static final int STATE_SCROLLED_DOWN = 1;

  /** State of the bottom view when it's scrolled up. */
  public static final int STATE_SCROLLED_UP = 2;

  private int height = 0;

  private AccessibilityManager accessibilityManager;
  private TouchExplorationStateChangeListener touchExplorationListener;

  private boolean disableOnTouchExploration = true;

  /**
   * Positions the scroll state can be set to.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_SCROLLED_DOWN, STATE_SCROLLED_UP})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ScrollState {}

  @ScrollState private int currentState = STATE_SCROLLED_UP;
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
    enterAnimDuration =
        MotionUtils.resolveThemeDuration(
            child.getContext(), ENTER_ANIM_DURATION_ATTR, DEFAULT_ENTER_ANIMATION_DURATION_MS);
    exitAnimDuration =
        MotionUtils.resolveThemeDuration(
            child.getContext(), EXIT_ANIM_DURATION_ATTR, DEFAULT_EXIT_ANIMATION_DURATION_MS);
    enterAnimInterpolator =
        MotionUtils.resolveThemeInterpolator(
            child.getContext(),
            ENTER_EXIT_ANIM_EASING_ATTR,
            AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    exitAnimInterpolator =
        MotionUtils.resolveThemeInterpolator(
            child.getContext(),
            ENTER_EXIT_ANIM_EASING_ATTR,
            AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    disableIfTouchExplorationEnabled(child);
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  private void disableIfTouchExplorationEnabled(V child) {
    if (accessibilityManager == null) {
      accessibilityManager = getSystemService(child.getContext(), AccessibilityManager.class);
    }
    if (accessibilityManager != null && touchExplorationListener == null) {
      touchExplorationListener =
          enabled -> {
            if (enabled && isScrolledDown()) {
              slideUp(child);
            }
          };
      accessibilityManager.addTouchExplorationStateChangeListener(touchExplorationListener);
      child.addOnAttachStateChangeListener(
          new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {}

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
              if (touchExplorationListener != null && accessibilityManager != null) {
                accessibilityManager.removeTouchExplorationStateChangeListener(
                    touchExplorationListener);
                touchExplorationListener = null;
              }
            }
          });
    }
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
    slideUp(child, /* animate= */ true);
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
    updateCurrentState(child, STATE_SCROLLED_UP);
    int targetTranslationY = 0;
    if (animate) {
      animateChildTo(child, targetTranslationY, enterAnimDuration, enterAnimInterpolator);
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
    slideDown(child, /* animate= */ true);
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

    // If Touch Exploration is on, we should disable sliding down due to a11y issues.
    if (disableOnTouchExploration
        && accessibilityManager != null
        && accessibilityManager.isTouchExplorationEnabled()) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    updateCurrentState(child, STATE_SCROLLED_DOWN);
    int targetTranslationY = height + additionalHiddenOffsetY;
    if (animate) {
      animateChildTo(child, targetTranslationY, exitAnimDuration, exitAnimInterpolator);
    } else {
      child.setTranslationY(targetTranslationY);
    }
  }

  private void updateCurrentState(@NonNull V child, @ScrollState int state) {
    currentState = state;
    for (OnScrollStateChangedListener listener : onScrollStateChangedListeners) {
      listener.onStateChanged(child, currentState);
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

  /**
   * Adds a listener to be notified of bottom view scroll state changes.
   *
   * @param listener The listener to notify when bottom view scroll state changes.
   */
  public void addOnScrollStateChangedListener(@NonNull OnScrollStateChangedListener listener) {
    onScrollStateChangedListeners.add(listener);
  }

  /**
   * Removes a previously added listener.
   *
   * @param listener The listener to remove.
   */
  public void removeOnScrollStateChangedListener(@NonNull OnScrollStateChangedListener listener) {
    onScrollStateChangedListeners.remove(listener);
  }

  /** Remove all previously added {@link OnScrollStateChangedListener}s. */
  public void clearOnScrollStateChangedListeners() {
    onScrollStateChangedListeners.clear();
  }

  /** Sets whether or not to disable this behavior if touch exploration is enabled. */
  public void disableOnTouchExploration(boolean disableOnTouchExploration) {
    this.disableOnTouchExploration = disableOnTouchExploration;
  }

  /** Returns whether or not this behavior is disabled if touch exploration is enabled. */
  public boolean isDisabledOnTouchExploration() {
    return disableOnTouchExploration;
  }
}
