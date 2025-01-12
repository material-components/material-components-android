/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static androidx.core.math.MathUtils.clamp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Property;
import android.view.animation.Interpolator;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat;
import com.google.android.material.progressindicator.DrawingDelegate.ActiveIndicator;

/**
 * This is the implementation class for drawing progress indicator in the linear disjoint
 * indeterminate mode.
 */
final class LinearIndeterminateDisjointAnimatorDelegate
    extends IndeterminateAnimatorDelegate<ObjectAnimator> {

  // Constants for animation timing.
  private static final int TOTAL_DURATION_IN_MS = 1800;
  private static final int[] DURATION_TO_MOVE_SEGMENT_ENDS = {533, 567, 850, 750};
  private static final int[] DELAY_TO_MOVE_SEGMENT_ENDS = {1267, 1000, 333, 0};

  // The animator controls disjoint linear indeterminate animation.
  private ObjectAnimator animator;
  private ObjectAnimator completeEndAnimator;
  private final Interpolator[] interpolatorArray;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // For animator control.
  private int indicatorColorIndex = 0;
  private boolean dirtyColors;
  private float animationFraction;
  AnimationCallback animatorCompleteCallback = null;

  public LinearIndeterminateDisjointAnimatorDelegate(
      @NonNull Context context, @NonNull LinearProgressIndicatorSpec spec) {
    super(/* indicatorCount= */ 2);

    baseSpec = spec;

    interpolatorArray =
        new Interpolator[] {
          AnimationUtilsCompat.loadInterpolator(
              context, R.anim.linear_indeterminate_line1_head_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.anim.linear_indeterminate_line1_tail_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.anim.linear_indeterminate_line2_head_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.anim.linear_indeterminate_line2_tail_interpolator)
        };
  }

  // ******************* Animation control *******************

  @Override
  public void startAnimator() {
    maybeInitializeAnimators();

    resetPropertiesForNewStart();
    animator.start();
  }

  private void maybeInitializeAnimators() {
    if (animator == null) {
      // Instantiates an animator with the linear interpolator to control the animation progress.
      animator = ObjectAnimator.ofFloat(this, ANIMATION_FRACTION, 0, 1);
      animator.setDuration(
          (long) (TOTAL_DURATION_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
      animator.setInterpolator(null);
      animator.setRepeatCount(ValueAnimator.INFINITE);
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
              super.onAnimationRepeat(animation);
              indicatorColorIndex = (indicatorColorIndex + 1) % baseSpec.indicatorColors.length;
              dirtyColors = true;
            }
          });
    }
    if (completeEndAnimator == null) {
      completeEndAnimator = ObjectAnimator.ofFloat(this, ANIMATION_FRACTION, 1);
      completeEndAnimator.setDuration(
          (long) (TOTAL_DURATION_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
      completeEndAnimator.setInterpolator(null);
      completeEndAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              cancelAnimatorImmediately();
              if (animatorCompleteCallback != null) {
                animatorCompleteCallback.onAnimationEnd(drawable);
              }
            }
          });
    }
  }

  private void updateAnimatorsDuration() {
    maybeInitializeAnimators();
    animator.setDuration(
        (long) (TOTAL_DURATION_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
    completeEndAnimator.setDuration(
        (long) (TOTAL_DURATION_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
  }

  @Override
  public void cancelAnimatorImmediately() {
    if (animator != null) {
      animator.cancel();
    }
  }

  @Override
  public void requestCancelAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (completeEndAnimator == null || completeEndAnimator.isRunning()) {
      return;
    }

    cancelAnimatorImmediately();
    if (drawable.isVisible()) {
      completeEndAnimator.setFloatValues(animationFraction, 1);
      completeEndAnimator.setDuration((long) (TOTAL_DURATION_IN_MS * (1 - animationFraction)));
      completeEndAnimator.start();
    }
  }

  @Override
  public void invalidateSpecValues() {
    updateAnimatorsDuration();
    resetPropertiesForNewStart();
  }

  @Override
  public void registerAnimatorsCompleteCallback(@NonNull AnimationCallback callback) {
    animatorCompleteCallback = callback;
  }

  @Override
  public void unregisterAnimatorsCompleteCallback() {
    animatorCompleteCallback = null;
  }

  // ******************* Helper methods *******************

  /** Updates the segment position array based on current playtime. */
  private void updateSegmentPositions(int playtime) {
    for (int i = 0; i < activeIndicators.size(); i++) {
      ActiveIndicator indicator = activeIndicators.get(i);
      float fraction =
          getFractionInRange(
              playtime, DELAY_TO_MOVE_SEGMENT_ENDS[2 * i], DURATION_TO_MOVE_SEGMENT_ENDS[2 * i]);
      indicator.startFraction = clamp(interpolatorArray[2 * i].getInterpolation(fraction), 0f, 1f);
      fraction =
          getFractionInRange(
              playtime,
              DELAY_TO_MOVE_SEGMENT_ENDS[2 * i + 1],
              DURATION_TO_MOVE_SEGMENT_ENDS[2 * i + 1]);
      indicator.endFraction =
          clamp(interpolatorArray[2 * i + 1].getInterpolation(fraction), 0f, 1f);
    }
  }

  /** Updates the segment color array based on the updated color index. */
  private void maybeUpdateSegmentColors() {
    if (dirtyColors) {
      for (ActiveIndicator indicator : activeIndicators) {
        indicator.color = baseSpec.indicatorColors[indicatorColorIndex];
      }
      dirtyColors = false;
    }
  }

  @VisibleForTesting
  @Override
  void resetPropertiesForNewStart() {
    indicatorColorIndex = 0;
    for (ActiveIndicator indicator : activeIndicators) {
      indicator.color = baseSpec.indicatorColors[0];
    }
  }

  // ******************* Getters and setters *******************

  private float getAnimationFraction() {
    return animationFraction;
  }

  @VisibleForTesting
  @Override
  void setAnimationFraction(float fraction) {
    animationFraction = fraction;
    int playtime = (int) (animationFraction * TOTAL_DURATION_IN_MS);
    updateSegmentPositions(playtime);
    maybeUpdateSegmentColors();
    drawable.invalidateSelf();
  }

  // ******************* Properties *******************

  private static final Property<LinearIndeterminateDisjointAnimatorDelegate, Float>
      ANIMATION_FRACTION =
          new Property<LinearIndeterminateDisjointAnimatorDelegate, Float>(
              Float.class, "animationFraction") {
            @Override
            public Float get(LinearIndeterminateDisjointAnimatorDelegate delegate) {
              return delegate.getAnimationFraction();
            }

            @Override
            public void set(LinearIndeterminateDisjointAnimatorDelegate delegate, Float value) {
              delegate.setAnimationFraction(value);
            }
          };
}
