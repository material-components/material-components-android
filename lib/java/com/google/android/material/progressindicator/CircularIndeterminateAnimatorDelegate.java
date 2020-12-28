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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.math.MathUtils;

/**
 * This is the implementation class for drawing progress indicator in the circular indeterminate
 * mode.
 */
final class CircularIndeterminateAnimatorDelegate
    extends IndeterminateAnimatorDelegate<AnimatorSet> {

  // Constants for animation values.
  private static final float INDICATOR_MAX_DEGREES = 270f;
  private static final float INDICATOR_MIN_DEGREES = 20f;
  private static final float INDICATOR_DELTA_DEGREES =
      INDICATOR_MAX_DEGREES - INDICATOR_MIN_DEGREES;
  private static final float INDICATOR_OFFSET_PER_COLOR_DEGREES = 360f;

  // Constants for animation timing.
  private static final int DURATION_PER_COLOR_IN_MS = 1333;
  private static final int COLOR_FADING_DURATION = 333;
  private static final int COLOR_FADING_DELAY = 1000;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // The animators control circular indeterminate animation.
  private AnimatorSet animatorSet;
  private ObjectAnimator indicatorCollapsingAnimator;
  private ObjectAnimator colorFadingAnimator;

  // Internal parameters controlled by the animator.
  private int indicatorColorIndex;
  private int displayedIndicatorColor;
  private float indicatorStartOffset;
  private float indicatorInCycleOffset;
  private float indicatorHeadChangeFraction;
  private float indicatorTailChangeFraction;

  // For animator control.
  boolean animatorCompleteEndRequested = false;
  AnimationCallback animatorCompleteCallback = null;

  public CircularIndeterminateAnimatorDelegate(@NonNull CircularProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 1);

    baseSpec = spec;
  }

  @Override
  protected void registerDrawable(@NonNull IndeterminateDrawable drawable) {
    super.registerDrawable(drawable);

    colorFadingAnimator =
        ObjectAnimator.ofObject(
            this,
            DISPLAYED_INDICATOR_COLOR,
            new ArgbEvaluatorCompat(),
            MaterialColors.compositeARGBWithAlpha(
                baseSpec.indicatorColors[indicatorColorIndex], drawable.getAlpha()),
            MaterialColors.compositeARGBWithAlpha(
                baseSpec.indicatorColors[getNextIndicatorColorIndex()], drawable.getAlpha()));
    colorFadingAnimator.setDuration(COLOR_FADING_DURATION);
    colorFadingAnimator.setStartDelay(COLOR_FADING_DELAY);
    colorFadingAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);

    if (animatorSet != null) {
      animatorSet.playTogether(colorFadingAnimator);
    }
  }

  // ******************* Animation control *******************

  @Override
  void startAnimator() {
    maybeInitializeAnimators();

    animatorSet.start();
  }

  private void maybeInitializeAnimators() {
    if (animatorSet == null) {
      // Instantiates the animator.
      ObjectAnimator constantlyRotateAnimator =
          ObjectAnimator.ofFloat(
              this, INDICATOR_IN_CYCLE_OFFSET, 0f, INDICATOR_OFFSET_PER_COLOR_DEGREES);
      constantlyRotateAnimator.setDuration(DURATION_PER_COLOR_IN_MS);
      // Sets null to get a linear interpolator.
      constantlyRotateAnimator.setInterpolator(null);

      ObjectAnimator expandAnimator =
          ObjectAnimator.ofFloat(this, INDICATOR_HEAD_CHANGE_FRACTION, 0f, 1f);
      expandAnimator.setDuration(DURATION_PER_COLOR_IN_MS / 2);
      expandAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      expandAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              // Manipulates collapse animator to make the indicator span ends with 0 degree.
              if (animatorCompleteEndRequested) {
                indicatorCollapsingAnimator.setFloatValues(
                    0f, 1f + INDICATOR_MIN_DEGREES / INDICATOR_DELTA_DEGREES);
              }
            }
          });

      indicatorCollapsingAnimator =
          ObjectAnimator.ofFloat(this, INDICATOR_TAIL_CHANGE_FRACTION, 0f, 1f);
      indicatorCollapsingAnimator.setDuration(DURATION_PER_COLOR_IN_MS / 2);
      indicatorCollapsingAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);

      animatorSet = new AnimatorSet();
      animatorSet.playSequentially(expandAnimator, indicatorCollapsingAnimator);
      animatorSet.playTogether(constantlyRotateAnimator);
      if (colorFadingAnimator != null) {
        animatorSet.playTogether(colorFadingAnimator);
      }
      animatorSet.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);

              if (animatorCompleteEndRequested && segmentPositions[0] == segmentPositions[1]) {
                animatorCompleteCallback.onAnimationEnd(drawable);
                animatorCompleteEndRequested = false;
              } else {
                // If the drawable is still visible, continues the main animator by restarting.
                if (drawable.isVisible()) {
                  resetPropertiesForNextCycle();
                  startAnimator();
                }
              }
            }
          });
    }
  }

  @Override
  void cancelAnimatorImmediately() {
    if (animatorSet != null) {
      animatorSet.cancel();
    }
  }

  @Override
  void requestCancelAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (animatorCompleteEndRequested) {
      return;
    }

    if (drawable.isVisible()) {
      animatorCompleteEndRequested = true;
    } else {
      cancelAnimatorImmediately();
    }
  }

  @Override
  void resetPropertiesForNewStart() {
    setIndicatorHeadChangeFraction(0f);
    setIndicatorTailChangeFraction(0f);
    setIndicatorStartOffset(0f);
    if (indicatorCollapsingAnimator != null) {
      indicatorCollapsingAnimator.setFloatValues(0f, 1f);
    }
    resetSegmentColors();
  }

  @Override
  void resetPropertiesForNextCycle() {
    setIndicatorHeadChangeFraction(0f);
    setIndicatorTailChangeFraction(0f);
    setIndicatorStartOffset(
        MathUtils.floorMod(
            getIndicatorStartOffset()
                + INDICATOR_OFFSET_PER_COLOR_DEGREES
                + INDICATOR_DELTA_DEGREES,
            360));
    shiftSegmentColors();
  }

  @Override
  public void invalidateSpecValues() {
    resetSegmentColors();
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

  /** Returns the index of the next available color for indicator. */
  private int getNextIndicatorColorIndex() {
    return (indicatorColorIndex + 1) % baseSpec.indicatorColors.length;
  }

  /** Updates the segment position array based on current animator controlled parameters. */
  private void updateSegmentPositions() {
    segmentPositions[0] =
        (getIndicatorStartOffset()
                + getIndicatorInCycleOffset()
                - INDICATOR_MIN_DEGREES
                + getIndicatorTailChangeFraction() * INDICATOR_DELTA_DEGREES)
            / 360;
    segmentPositions[1] =
        (getIndicatorStartOffset()
                + getIndicatorInCycleOffset()
                + getIndicatorHeadChangeFraction() * INDICATOR_DELTA_DEGREES)
            / 360;
  }

  /** Shifts the color used in the segment colors to the next available one. */
  private void shiftSegmentColors() {
    indicatorColorIndex = getNextIndicatorColorIndex();
    int startColor =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[indicatorColorIndex], drawable.getAlpha());
    int endColor =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[getNextIndicatorColorIndex()], drawable.getAlpha());
    colorFadingAnimator.setIntValues(startColor, endColor);
    setDisplayedIndicatorColor(startColor);
  }

  /** Resets the segment colors to the first indicator color. */
  private void resetSegmentColors() {
    indicatorColorIndex = 0;
    int startColor =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[indicatorColorIndex], drawable.getAlpha());
    int endColor =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[getNextIndicatorColorIndex()], drawable.getAlpha());
    colorFadingAnimator.setIntValues(startColor, endColor);
    setDisplayedIndicatorColor(startColor);
  }

  // ******************* Getters and setters *******************

  private int getDisplayedIndicatorColor() {
    return displayedIndicatorColor;
  }

  private void setDisplayedIndicatorColor(int displayedIndicatorColor) {
    this.displayedIndicatorColor = displayedIndicatorColor;
    segmentColors[0] = displayedIndicatorColor;
    drawable.invalidateSelf();
  }

  private float getIndicatorStartOffset() {
    return indicatorStartOffset;
  }

  @VisibleForTesting
  void setIndicatorStartOffset(float indicatorStartOffset) {
    this.indicatorStartOffset = indicatorStartOffset;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  private float getIndicatorInCycleOffset() {
    return indicatorInCycleOffset;
  }

  @VisibleForTesting
  void setIndicatorInCycleOffset(float indicatorInCycleOffset) {
    this.indicatorInCycleOffset = indicatorInCycleOffset;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  private float getIndicatorHeadChangeFraction() {
    return indicatorHeadChangeFraction;
  }

  @VisibleForTesting
  void setIndicatorHeadChangeFraction(float indicatorHeadChangeFraction) {
    this.indicatorHeadChangeFraction = indicatorHeadChangeFraction;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  private float getIndicatorTailChangeFraction() {
    return indicatorTailChangeFraction;
  }

  @VisibleForTesting
  void setIndicatorTailChangeFraction(float indicatorTailChangeFraction) {
    this.indicatorTailChangeFraction = indicatorTailChangeFraction;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  // ******************* Properties *******************

  /** The property of indicator color being currently displayed. */
  private static final Property<CircularIndeterminateAnimatorDelegate, Integer>
      DISPLAYED_INDICATOR_COLOR =
          new Property<CircularIndeterminateAnimatorDelegate, Integer>(
              Integer.class, "displayedIndicatorColor") {
            @Override
            public Integer get(CircularIndeterminateAnimatorDelegate delegate) {
              return delegate.getDisplayedIndicatorColor();
            }

            @Override
            public void set(CircularIndeterminateAnimatorDelegate delegate, Integer value) {
              delegate.setDisplayedIndicatorColor(value);
            }
          };

  /**
   * The property of degrees which the indicator should rotate clockwise from the {@code
   * indicatorStartOffset}.
   */
  private static final Property<CircularIndeterminateAnimatorDelegate, Float>
      INDICATOR_IN_CYCLE_OFFSET =
          new Property<CircularIndeterminateAnimatorDelegate, Float>(
              Float.class, "indicatorInCycleOffset") {
            @Override
            public Float get(CircularIndeterminateAnimatorDelegate delegate) {
              return delegate.getIndicatorInCycleOffset();
            }

            @Override
            public void set(CircularIndeterminateAnimatorDelegate delegate, Float value) {
              delegate.setIndicatorInCycleOffset(value);
            }
          };

  /**
   * The property of the fraction of the head (the more clockwise end) of the indicator in the total
   * amount it can change.
   */
  private static final Property<CircularIndeterminateAnimatorDelegate, Float>
      INDICATOR_HEAD_CHANGE_FRACTION =
          new Property<CircularIndeterminateAnimatorDelegate, Float>(
              Float.class, "indicatorHeadChangeFraction") {
            @Override
            public Float get(CircularIndeterminateAnimatorDelegate delegate) {
              return delegate.getIndicatorHeadChangeFraction();
            }

            @Override
            public void set(CircularIndeterminateAnimatorDelegate delegate, Float value) {
              delegate.setIndicatorHeadChangeFraction(value);
            }
          };

  /**
   * The property of the fraction of the tail (the less clockwise end) of the indicator in the total
   * amount it can change.
   */
  private static final Property<CircularIndeterminateAnimatorDelegate, Float>
      INDICATOR_TAIL_CHANGE_FRACTION =
          new Property<CircularIndeterminateAnimatorDelegate, Float>(
              Float.class, "indicatorTailChangeFraction") {
            @Override
            public Float get(CircularIndeterminateAnimatorDelegate delegate) {
              return delegate.getIndicatorTailChangeFraction();
            }

            @Override
            public void set(CircularIndeterminateAnimatorDelegate delegate, Float value) {
              delegate.setIndicatorTailChangeFraction(value);
            }
          };
}
