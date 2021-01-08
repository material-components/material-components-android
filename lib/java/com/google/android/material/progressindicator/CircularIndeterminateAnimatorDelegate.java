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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.google.android.material.color.MaterialColors;

/**
 * This is the implementation class for drawing progress indicator in the circular indeterminate
 * mode.
 */
final class CircularIndeterminateAnimatorDelegate
    extends IndeterminateAnimatorDelegate<ObjectAnimator> {

  // Constants for animation timing.
  private static final int TOTAL_CYCLES = 4;
  private static final int TOTAL_DURATION_IN_MS = 5400;
  private static final int DURATION_TO_EXPAND_IN_MS = 667;
  private static final int DURATION_TO_COLLAPSE_IN_MS = 667;
  private static final int DURATION_TO_FADE_IN_MS = 333;
  private static final int DURATION_TO_COMPLETE_END_IN_MS = 333;
  private static final int[] DELAY_TO_EXPAND_IN_MS = {0, 1350, 2700, 4050};
  private static final int[] DELAY_TO_COLLAPSE_IN_MS = {667, 2017, 3367, 4717};
  private static final int[] DELAY_TO_FADE_IN_MS = {1000, 2350, 3700, 5050};

  // Constants for animation values.
  private static final int TAIL_DEGREES_OFFSET = -20;
  private static final int EXTRA_DEGREES_PER_CYCLE = 250;
  private static final int CONSTANT_ROTATION_DEGREES = 1520;

  private ObjectAnimator animator;
  private ObjectAnimator completeEndAnimator;
  private final FastOutSlowInInterpolator interpolator;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // For animator control.
  private int indicatorColorIndexOffset = 0;
  private float animationFraction;
  private float completeEndFraction;
  AnimationCallback animatorCompleteCallback = null;

  public CircularIndeterminateAnimatorDelegate(@NonNull CircularProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 1);

    baseSpec = spec;

    interpolator = new FastOutSlowInInterpolator();
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
      animator.setDuration(TOTAL_DURATION_IN_MS);
      animator.setInterpolator(null);
      animator.setRepeatCount(ValueAnimator.INFINITE);
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
              super.onAnimationRepeat(animation);
              indicatorColorIndexOffset =
                  (indicatorColorIndexOffset + TOTAL_CYCLES) % baseSpec.indicatorColors.length;
            }
          });
    }

    if (completeEndAnimator == null) {
      completeEndAnimator = ObjectAnimator.ofFloat(this, COMPLETE_END_FRACTION, 0, 1);
      completeEndAnimator.setDuration(DURATION_TO_COMPLETE_END_IN_MS);
      completeEndAnimator.setInterpolator(interpolator);
      completeEndAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              cancelAnimatorImmediately();
              animatorCompleteCallback.onAnimationEnd(drawable);
            }
          });
    }
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
    if (completeEndAnimator.isRunning()) {
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
    // Adds constant rotation to segment positions.
    segmentPositions[0] = CONSTANT_ROTATION_DEGREES * animationFraction + TAIL_DEGREES_OFFSET;
    segmentPositions[1] = CONSTANT_ROTATION_DEGREES * animationFraction;
    // Adds cycle specific rotation to segment positions.
    for (int cycleIndex = 0; cycleIndex < TOTAL_CYCLES; cycleIndex++) {
      // While expanding.
      float fraction =
          getFractionInRange(playtime, DELAY_TO_EXPAND_IN_MS[cycleIndex], DURATION_TO_EXPAND_IN_MS);
      segmentPositions[1] += interpolator.getInterpolation(fraction) * EXTRA_DEGREES_PER_CYCLE;
      // While collapsing.
      fraction =
          getFractionInRange(
              playtime, DELAY_TO_COLLAPSE_IN_MS[cycleIndex], DURATION_TO_COLLAPSE_IN_MS);
      segmentPositions[0] += interpolator.getInterpolation(fraction) * EXTRA_DEGREES_PER_CYCLE;
    }
    // Closes the gap between head and tail for complete end.
    segmentPositions[0] += (segmentPositions[1] - segmentPositions[0]) * completeEndFraction;

    segmentPositions[0] /= 360;
    segmentPositions[1] /= 360;
  }

  /** Updates the segment color array based on current playtime. */
  private void maybeUpdateSegmentColors(int playtime) {
    for (int cycleIndex = 0; cycleIndex < TOTAL_CYCLES; cycleIndex++) {
      float timeFraction =
          getFractionInRange(playtime, DELAY_TO_FADE_IN_MS[cycleIndex], DURATION_TO_FADE_IN_MS);
      if (timeFraction >= 0 && timeFraction <= 1) {
        int startColorIndex =
            (cycleIndex + indicatorColorIndexOffset) % baseSpec.indicatorColors.length;
        int endColorIndex = (startColorIndex + 1) % baseSpec.indicatorColors.length;
        int startColor =
            MaterialColors.compositeARGBWithAlpha(
                baseSpec.indicatorColors[startColorIndex], drawable.getAlpha());
        int endColor =
            MaterialColors.compositeARGBWithAlpha(
                baseSpec.indicatorColors[endColorIndex], drawable.getAlpha());
        float colorFraction = interpolator.getInterpolation(timeFraction);
        segmentColors[0] =
            ArgbEvaluatorCompat.getInstance().evaluate(colorFraction, startColor, endColor);
        break;
      }
    }
  }

  @VisibleForTesting
  void resetPropertiesForNewStart() {
    indicatorColorIndexOffset = 0;
    segmentColors[0] =
        MaterialColors.compositeARGBWithAlpha(baseSpec.indicatorColors[0], drawable.getAlpha());
    completeEndFraction = 0f;
  }

  // ******************* Getters and setters *******************

  private float getAnimationFraction() {
    return animationFraction;
  }

  @VisibleForTesting
  void setAnimationFraction(float fraction) {
    animationFraction = fraction;
    int playtime = (int) (animationFraction * TOTAL_DURATION_IN_MS);
    updateSegmentPositions(playtime);
    maybeUpdateSegmentColors(playtime);
    drawable.invalidateSelf();
  }

  private float getCompleteEndFraction() {
    return completeEndFraction;
  }

  private void setCompleteEndFraction(float fraction) {
    completeEndFraction = fraction;
  }

  // ******************* Properties *******************

  private static final Property<CircularIndeterminateAnimatorDelegate, Float> ANIMATION_FRACTION =
      new Property<CircularIndeterminateAnimatorDelegate, Float>(Float.class, "animationFraction") {
        @Override
        public Float get(CircularIndeterminateAnimatorDelegate delegate) {
          return delegate.getAnimationFraction();
        }

        @Override
        public void set(CircularIndeterminateAnimatorDelegate delegate, Float value) {
          delegate.setAnimationFraction(value);
        }
      };

  private static final Property<CircularIndeterminateAnimatorDelegate, Float>
      COMPLETE_END_FRACTION =
          new Property<CircularIndeterminateAnimatorDelegate, Float>(
              Float.class, "completeEndFraction") {
            @Override
            public Float get(CircularIndeterminateAnimatorDelegate delegate) {
              return delegate.getCompleteEndFraction();
            }

            @Override
            public void set(CircularIndeterminateAnimatorDelegate delegate, Float value) {
              delegate.setCompleteEndFraction(value);
            }
          };
}
