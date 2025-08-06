/*
 * Copyright (C) 2024 The Android Open Source Project
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
import android.view.Gravity;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.view.ViewCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MotionUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off of the
 * edge of the screen when scrolling down, and show it when scrolling up.
 *
 * <p>Supports hiding the View off of three screen edges: {@link #EDGE_RIGHT}, {@link #EDGE_BOTTOM},
 * and {@link #EDGE_LEFT}.
 *
 * <p>If Touch Exploration is enabled, the hide on scroll behavior should be disabled until Touch
 * Exploration is disabled. Ensure that the content is not obscured due to disabling this behavior
 * by adding padding to the content.
 */
public class HideViewOnScrollBehavior<V extends View> extends Behavior<V> {

  private HideViewOnScrollDelegate hideOnScrollViewDelegate;
  private AccessibilityManager accessibilityManager;
  private TouchExplorationStateChangeListener touchExplorationListener;

  private boolean disableOnTouchExploration = true;

  /**
   * Interface definition for a listener to be notified when the bottom view scroll state changes.
   */
  public interface OnScrollStateChangedListener {

    /**
     * Called when the view changes its scrolled state.
     *
     * @param view The scrollable view.
     * @param newState The new state. This will be one of {@link #STATE_SCROLLED_IN} or {@link
     *     #STATE_SCROLLED_OUT}.
     */
    void onStateChanged(@NonNull View view, @ScrollState int newState);
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
  @Nullable private TimeInterpolator enterAnimInterpolator;
  @Nullable private TimeInterpolator exitAnimInterpolator;

  /** The sheet slides out from the right edge of the screen. */
  public static final int EDGE_RIGHT = 0;

  /** The sheet slides out from the bottom edge of the screen. */
  public static final int EDGE_BOTTOM = 1;

  /** The sheet slides out from the left edge of the screen. */
  public static final int EDGE_LEFT = 2;

  /**
   * The edge of the screen that a sheet slides out from.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({EDGE_RIGHT, EDGE_BOTTOM, EDGE_LEFT})
  @Retention(RetentionPolicy.SOURCE)
  @interface ViewEdge {}

  /** State of the view when it's scrolled out. */
  public static final int STATE_SCROLLED_OUT = 1;

  /** State of the view when it's scrolled in. */
  public static final int STATE_SCROLLED_IN = 2;

  private int size = 0;

  /**
   * Positions the scroll state can be set to.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_SCROLLED_OUT, STATE_SCROLLED_IN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ScrollState {}

  @ScrollState private int currentState = STATE_SCROLLED_IN;
  private int additionalHiddenOffset = 0;
  @Nullable private ViewPropertyAnimator currentAnimator;

  private boolean viewEdgeOverride = false;

  public HideViewOnScrollBehavior() {}

  public HideViewOnScrollBehavior(@ViewEdge int viewEdge) {
    this();

    setViewEdge(viewEdge);
  }

  public HideViewOnScrollBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  private void setViewEdge(@NonNull V view, int layoutDirection) {
    if (viewEdgeOverride) {
      return;
    }

    LayoutParams params = (LayoutParams) view.getLayoutParams();
    int viewGravity = params.gravity;

    if (isGravityBottom(viewGravity)) {
      setViewEdgeInternal(EDGE_BOTTOM);
    } else {
      viewGravity = Gravity.getAbsoluteGravity(viewGravity, layoutDirection);
      setViewEdgeInternal(isGravityLeft(viewGravity) ? EDGE_LEFT : EDGE_RIGHT);
    }
  }

  public void setViewEdge(@ViewEdge int viewEdge) {
    viewEdgeOverride = true;
    setViewEdgeInternal(viewEdge);
  }

  private void setViewEdgeInternal(@ViewEdge int viewEdge) {
    if (hideOnScrollViewDelegate == null || hideOnScrollViewDelegate.getViewEdge() != viewEdge) {
      switch (viewEdge) {
        case EDGE_RIGHT:
          this.hideOnScrollViewDelegate = new HideRightViewOnScrollDelegate();
          break;
        case EDGE_BOTTOM:
          this.hideOnScrollViewDelegate = new HideBottomViewOnScrollDelegate();
          break;
        case EDGE_LEFT:
          this.hideOnScrollViewDelegate = new HideLeftViewOnScrollDelegate();
          break;
        default:
          throw new IllegalArgumentException(
              "Invalid view edge position value: "
                  + viewEdge
                  + ". Must be "
                  + EDGE_RIGHT
                  + ", "
                  + EDGE_BOTTOM
                  + " or "
                  + EDGE_LEFT
                  + ".");
      }
    }
  }

  private boolean isGravityBottom(int viewGravity) {
    return viewGravity == Gravity.BOTTOM || (viewGravity == (Gravity.BOTTOM | Gravity.CENTER));
  }

  private boolean isGravityLeft(int viewGravity) {
    return viewGravity == Gravity.LEFT || (viewGravity == (Gravity.LEFT | Gravity.CENTER));
  }

  private void disableIfTouchExplorationEnabled(V child) {
    if (accessibilityManager == null) {
      accessibilityManager = getSystemService(child.getContext(), AccessibilityManager.class);
    }

    if (accessibilityManager != null && touchExplorationListener == null) {
      touchExplorationListener =
          enabled -> {
            if (disableOnTouchExploration && enabled && isScrolledOut()) {
              slideIn(child);
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

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {

    disableIfTouchExplorationEnabled(child);

    ViewGroup.MarginLayoutParams marginParams =
        (ViewGroup.MarginLayoutParams) child.getLayoutParams();
    setViewEdge(child, layoutDirection);

    this.size = hideOnScrollViewDelegate.getSize(child, marginParams);

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
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  /**
   * Sets an additional offset used to hide the view.
   *
   * @param child the child view that is hidden by this behavior
   * @param offset the additional offset in pixels that should be added when the view slides away
   */
  public void setAdditionalHiddenOffset(@NonNull V child, @Dimension int offset) {
    additionalHiddenOffset = offset;

    if (currentState == STATE_SCROLLED_OUT) {
      hideOnScrollViewDelegate.setAdditionalHiddenOffset(child, size, additionalHiddenOffset);
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
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed,
      int type,
      @NonNull int[] consumed) {
    if (dyConsumed > 0) {
      slideOut(child);
    } else if (dyConsumed < 0) {
      slideIn(child);
    }
  }

  /** Returns true if the current state is scrolled in. */
  public boolean isScrolledIn() {
    return currentState == STATE_SCROLLED_IN;
  }

  /**
   * Performs an animation that will slide the child from its current position to be totally on the
   * screen.
   */
  public void slideIn(@NonNull V child) {
    slideIn(child, /* animate= */ true);
  }

  /**
   * Slides the child with or without animation from its current position to be totally on the
   * screen.
   *
   * @param animate {@code true} to slide with animation.
   */
  public void slideIn(@NonNull V child, boolean animate) {
    if (isScrolledIn()) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    updateCurrentState(child, STATE_SCROLLED_IN);
    int targetTranslation = hideOnScrollViewDelegate.getTargetTranslation();

    if (animate) {
      animateChildTo(child, targetTranslation, enterAnimDuration, enterAnimInterpolator);
    } else {
      hideOnScrollViewDelegate.setViewTranslation(child, targetTranslation);
    }
  }

  /** Returns true if the current state is scrolled out. */
  public boolean isScrolledOut() {
    return currentState == STATE_SCROLLED_OUT;
  }

  /**
   * Performs an animation that will slide the child from it's current position to be totally off
   * the screen.
   */
  public void slideOut(@NonNull V child) {
    slideOut(child, /* animate= */ true);
  }

  /**
   * Slides the child with or without animation from its current position to be totally off the
   * screen.
   *
   * @param animate {@code true} to slide with animation.
   */
  public void slideOut(@NonNull V child, boolean animate) {
    if (isScrolledOut()) {
      return;
    }

    // If Touch Exploration is on, we prevent sliding out due to a11y issues.
    if (disableOnTouchExploration
        && accessibilityManager != null
        && accessibilityManager.isTouchExplorationEnabled()) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    updateCurrentState(child, STATE_SCROLLED_OUT);
    int targetTranslation = size + additionalHiddenOffset;
    if (animate) {
      animateChildTo(child, targetTranslation, exitAnimDuration, exitAnimInterpolator);
    } else {
      hideOnScrollViewDelegate.setViewTranslation(child, targetTranslation);
    }
  }

  private void updateCurrentState(@NonNull V child, @ScrollState int state) {
    currentState = state;
    for (OnScrollStateChangedListener listener : onScrollStateChangedListeners) {
      listener.onStateChanged(child, currentState);
    }
  }

  private void animateChildTo(
      @NonNull V child,
      int targetTranslation,
      long duration,
      @NonNull TimeInterpolator interpolator) {
    currentAnimator =
        hideOnScrollViewDelegate
            .getViewTranslationAnimator(child, targetTranslation)
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
   * Adds a listener to be notified of View scroll state changes.
   *
   * @param listener The listener to notify when View scroll state changes.
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

  /**
   * Sets whether or not to disable this behavior if touch exploration is enabled.
   */
  public void disableOnTouchExploration(boolean disableOnTouchExploration) {
    this.disableOnTouchExploration = disableOnTouchExploration;
  }

  /**
   * Returns whether or not this behavior is disabled if touch exploration is enabled.
   */
  public boolean isDisabledOnTouchExploration() {
    return disableOnTouchExploration;
  }

  /**
   * A utility function to get the {@link HideViewOnScrollBehavior} associated with the {@code
   * view}.
   *
   * @param view The {@link View} with {@link HideViewOnScrollBehavior}.
   * @return The {@link HideViewOnScrollBehavior} associated with the {@code view}.
   */
  @NonNull
  @SuppressWarnings("unchecked")
  public static <V extends View> HideViewOnScrollBehavior<V> from(@NonNull V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior = ((LayoutParams) params).getBehavior();
    if (!(behavior instanceof HideViewOnScrollBehavior)) {
      throw new IllegalArgumentException(
          "The view is not associated with HideViewOnScrollBehavior");
    }
    return (HideViewOnScrollBehavior<V>) behavior;
  }
}
