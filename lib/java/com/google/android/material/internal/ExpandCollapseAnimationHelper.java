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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.animation.AnimationUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A helper class that facilitates expand and collapse animations between two views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ExpandCollapseAnimationHelper {

  private final View collapsedView;
  private final View expandedView;
  private final List<AnimatorListenerAdapter> listeners;
  private final List<View> endAnchoredViews;

  @Nullable private AnimatorUpdateListener additionalUpdateListener;
  private long duration;
  private int collapsedViewOffsetY;
  private int expandedViewOffsetY;

  public ExpandCollapseAnimationHelper(@NonNull View collapsedView, @NonNull View expandedView) {
    this.collapsedView = collapsedView;
    this.expandedView = expandedView;
    this.listeners = new ArrayList<>();
    this.endAnchoredViews = new ArrayList<>();
  }

  /**
   * It's important that the expandedView has been measured (ie is not GONE) before this Animator is
   * run, or else it will not expand to the correct size. This can be accomplished by setting the
   * expandedView to INVISIBLE and posting the start() from it.
   *
   * @return an {@link Animator} that can be run to expand the {@code expandedView} and collapse the
   *     {@code collapsedView}
   */
  @NonNull
  public Animator getExpandAnimator() {
    final Animator animator = getAnimatorSet(true);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            expandedView.setVisibility(View.VISIBLE);
          }
        });
    addListeners(animator, listeners);

    return animator;
  }

  @NonNull
  public Animator getCollapseAnimator() {
    final Animator animator = getAnimatorSet(false);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            expandedView.setVisibility(View.GONE);
          }
        });
    addListeners(animator, listeners);

    return animator;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper setDuration(long duration) {
    this.duration = duration;
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper addListener(@NonNull AnimatorListenerAdapter listener) {
    listeners.add(listener);
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper addEndAnchoredViews(@NonNull View... views) {
    Collections.addAll(endAnchoredViews, views);
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper addEndAnchoredViews(@NonNull Collection<View> views) {
    endAnchoredViews.addAll(views);
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper setAdditionalUpdateListener(
      @Nullable AnimatorUpdateListener additionalUpdateListener) {
    this.additionalUpdateListener = additionalUpdateListener;
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper setCollapsedViewOffsetY(int collapsedViewOffsetY) {
    this.collapsedViewOffsetY = collapsedViewOffsetY;
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public ExpandCollapseAnimationHelper setExpandedViewOffsetY(int expandedViewOffsetY) {
    this.expandedViewOffsetY = expandedViewOffsetY;
    return this;
  }

  private AnimatorSet getAnimatorSet(boolean expand) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        getExpandCollapseAnimator(expand),
        getExpandedViewChildrenAlphaAnimator(expand),
        getEndAnchoredViewsTranslateAnimator(expand));
    return animatorSet;
  }

  private Animator getExpandCollapseAnimator(boolean expand) {
    Rect fromBounds = ViewUtils.calculateRectFromBounds(collapsedView, collapsedViewOffsetY);
    Rect toBounds = ViewUtils.calculateRectFromBounds(expandedView, expandedViewOffsetY);
    Rect bounds = new Rect(fromBounds);

    ValueAnimator animator =
        ValueAnimator.ofObject(new RectEvaluator(bounds), fromBounds, toBounds);
    animator.addUpdateListener(valueAnimator -> ViewUtils.setBoundsFromRect(expandedView, bounds));
    if (additionalUpdateListener != null) {
      animator.addUpdateListener(additionalUpdateListener);
    }
    animator.setDuration(duration);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(
            expand, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animator;
  }

  private Animator getExpandedViewChildrenAlphaAnimator(boolean expand) {
    List<View> expandedViewChildren = ViewUtils.getChildren(expandedView);
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(expandedViewChildren));
    animator.setDuration(duration);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(expand, AnimationUtils.LINEAR_INTERPOLATOR));
    return animator;
  }

  private Animator getEndAnchoredViewsTranslateAnimator(boolean expand) {
    // Counteracts the natural horizontal translation due to parent view bounds adjustment.
    int leftDelta = expandedView.getLeft() - collapsedView.getLeft();
    int rightDelta = collapsedView.getRight() - expandedView.getRight();
    int fromTranslationX = leftDelta + rightDelta;

    ValueAnimator animator = ValueAnimator.ofFloat(fromTranslationX, 0);
    animator.addUpdateListener(MultiViewUpdateListener.translationXListener(endAnchoredViews));
    animator.setDuration(duration);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(
            expand, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animator;
  }

  private void addListeners(Animator animator, List<AnimatorListenerAdapter> listeners) {
    for (AnimatorListenerAdapter listener : listeners) {
      animator.addListener(listener);
    }
  }
}
