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
package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static com.google.android.material.animation.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
import static com.google.android.material.math.MathUtils.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.progressindicator.DrawingDelegate.ActiveIndicator;

/**
 * This is the implementation class for drawing progress indicator in the circular indeterminate
 * mode with an animation that grows and shrinks the active segment by modifying the end point.
 */
final class CircularIndeterminateRetreatAnimatorDelegate
    extends IndeterminateAnimatorDelegate<ObjectAnimator> {
  private static final TimeInterpolator DEFAULT_INTERPOLATOR = FAST_OUT_SLOW_IN_INTERPOLATOR;

  // Constants for animation timing.
  private static final int TOTAL_DURATION_IN_MS = 6000;
  private static final int DURATION_SPIN_IN_MS = 500;
  private static final int DURATION_GROW_ACTIVE_IN_MS = 3000;
  private static final int DURATION_SHRINK_ACTIVE_IN_MS = 3000;
  private static final int[] DELAY_SPINS_IN_MS = new int[] {0, 1500, 3000, 4500};
  private static final int DELAY_GROW_ACTIVE_IN_MS = 0;
  private static final int DELAY_SHRINK_ACTIVE_IN_MS = 3000;
  private static final int DURATION_TO_COMPLETE_END_IN_MS = 500;
  private static final int DURATION_TO_FADE_IN_MS = 100;

  // Constants for animation values.

  // The total degrees that a constant rotation goes by.
  private static final int CONSTANT_ROTATION_DEGREES = 1080;
  // Despite of the constant rotation, there are also 5 extra rotations the entire animation. The
  // total degrees that each extra rotation goes by.
  private static final int SPIN_ROTATION_DEGREES = 90;
  private static final float START_FRACTION = 0f;
  private static final float[] END_FRACTION_RANGE = new float[] {0.10f, 0.87f};

  private ObjectAnimator animator;
  private ObjectAnimator completeEndAnimator;
  private final TimeInterpolator standardInterpolator;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // For animator control.
  private int indicatorColorIndexOffset = 0;
  private float animationFraction;
  private float completeEndFraction;
  AnimationCallback animatorCompleteCallback = null;

  public CircularIndeterminateRetreatAnimatorDelegate(
      @NonNull Context context, @NonNull CircularProgressIndicatorSpec spec) {
    super(/* indicatorCount= */ 1);

    baseSpec = spec;

    standardInterpolator =
        MotionUtils.resolveThemeInterpolator(
            context, R.attr.motionEasingStandardInterpolator, DEFAULT_INTERPOLATOR);
  }

  // ******************* Animation control *******************

  @Override
  void startAnimator() {
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
              indicatorColorIndexOffset =
                  (indicatorColorIndexOffset + DELAY_SPINS_IN_MS.length)
                      % baseSpec.indicatorColors.length;
            }
          });
    }

    if (completeEndAnimator == null) {
      completeEndAnimator = ObjectAnimator.ofFloat(this, COMPLETE_END_FRACTION, 0, 1);
      completeEndAnimator.setDuration(
          (long) (DURATION_TO_COMPLETE_END_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
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
        (long) (DURATION_TO_COMPLETE_END_IN_MS * baseSpec.indeterminateAnimatorDurationScale));
  }

  @Override
  void cancelAnimatorImmediately() {
    if (animator != null) {
      animator.cancel();
    }
  }

  @Override
  void requestCancelAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (completeEndAnimator == null || completeEndAnimator.isRunning()) {
      return;
    }

    if (drawable.isVisible()) {
      completeEndAnimator.start();
    } else {
      cancelAnimatorImmediately();
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
  private void updateSegmentPositions(int playtimeInMs) {
    ActiveIndicator indicator = activeIndicators.get(0);
    // Constant rotation.
    float constantRotation = CONSTANT_ROTATION_DEGREES * animationFraction;
    // Extra rotation for the faster spinning.
    float spinRotation = 0;
    for (int spinDelay : DELAY_SPINS_IN_MS) {
      spinRotation +=
          standardInterpolator.getInterpolation(
                  getFractionInRange(playtimeInMs, spinDelay, DURATION_SPIN_IN_MS))
              * SPIN_ROTATION_DEGREES;
    }
    indicator.rotationDegree = constantRotation + spinRotation;
    // Grow active indicator.
    float fraction =
        standardInterpolator.getInterpolation(
            getFractionInRange(playtimeInMs, DELAY_GROW_ACTIVE_IN_MS, DURATION_GROW_ACTIVE_IN_MS));
    fraction -=
        standardInterpolator.getInterpolation(
            getFractionInRange(
                playtimeInMs, DELAY_SHRINK_ACTIVE_IN_MS, DURATION_SHRINK_ACTIVE_IN_MS));
    indicator.startFraction = START_FRACTION;
    indicator.endFraction = lerp(END_FRACTION_RANGE[0], END_FRACTION_RANGE[1], fraction);

    // Completing animation.
    if (completeEndFraction > 0) {
      indicator.endFraction *= 1 - completeEndFraction;
    }
  }

  /** Updates the segment color array based on current playtime. */
  private void maybeUpdateSegmentColors(int playtimeInMs) {
    for (int cycleIndex = 0; cycleIndex < DELAY_SPINS_IN_MS.length; cycleIndex++) {
      float timeFraction =
          getFractionInRange(playtimeInMs, DELAY_SPINS_IN_MS[cycleIndex], DURATION_TO_FADE_IN_MS);
      if (timeFraction >= 0 && timeFraction <= 1) {
        int startColorIndex =
            (cycleIndex + indicatorColorIndexOffset) % baseSpec.indicatorColors.length;
        int endColorIndex = (startColorIndex + 1) % baseSpec.indicatorColors.length;
        int startColor = baseSpec.indicatorColors[startColorIndex];
        int endColor = baseSpec.indicatorColors[endColorIndex];
        float colorFraction = standardInterpolator.getInterpolation(timeFraction);
        activeIndicators.get(0).color =
            ArgbEvaluatorCompat.getInstance().evaluate(colorFraction, startColor, endColor);
        break;
      }
    }
  }

  @VisibleForTesting
  @Override
  void resetPropertiesForNewStart() {
    indicatorColorIndexOffset = 0;
    activeIndicators.get(0).color = baseSpec.indicatorColors[0];
    completeEndFraction = 0f;
  }

  // ******************* Getters and setters *******************

  private float getAnimationFraction() {
    return animationFraction;
  }

  @VisibleForTesting
  @Override
  void setAnimationFraction(float fraction) {
    animationFraction = fraction;
    int playtimeInMs = (int) (animationFraction * TOTAL_DURATION_IN_MS);
    updateSegmentPositions(playtimeInMs);
    maybeUpdateSegmentColors(playtimeInMs);
    drawable.invalidateSelf();
  }

  private float getCompleteEndFraction() {
    return completeEndFraction;
  }

  private void setCompleteEndFraction(float fraction) {
    completeEndFraction = fraction;
  }

  // ******************* Properties *******************

  private static final Property<CircularIndeterminateRetreatAnimatorDelegate, Float>
      ANIMATION_FRACTION =
          new Property<CircularIndeterminateRetreatAnimatorDelegate, Float>(
              Float.class, "animationFraction") {
            @Override
            public Float get(CircularIndeterminateRetreatAnimatorDelegate delegate) {
              return delegate.getAnimationFraction();
            }

            @Override
            public void set(CircularIndeterminateRetreatAnimatorDelegate delegate, Float value) {
              delegate.setAnimationFraction(value);
            }
          };

  private static final Property<CircularIndeterminateRetreatAnimatorDelegate, Float>
      COMPLETE_END_FRACTION =
          new Property<CircularIndeterminateRetreatAnimatorDelegate, Float>(
              Float.class, "completeEndFraction") {
            @Override
            public Float get(CircularIndeterminateRetreatAnimatorDelegate delegate) {
              return delegate.getCompleteEndFraction();
            }

            @Override
            public void set(CircularIndeterminateRetreatAnimatorDelegate delegate, Float value) {
              delegate.setCompleteEndFraction(value);
            }
          };
}
