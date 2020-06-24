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

import static com.google.android.material.progressindicator.ProgressIndicator.ANIMATION_SPEED_FACTOR;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Property;
import androidx.annotation.NonNull;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.google.android.material.math.MathUtils;

/**
 * This is the implementation class for drawing progress indicator in the circular indeterminate
 * mode.
 */
class CircularIndeterminateDrawable extends DrawableWithAnimatedVisibilityChange
    implements IndeterminateAnimatorControl {

  // Constants for drawing.
  private static final float INDICATOR_MAX_DEGREES = 270f;
  private static final float INDICATOR_MIN_DEGREES = 20f;
  private static final float INDICATOR_DELTA_DEGREES =
      INDICATOR_MAX_DEGREES - INDICATOR_MIN_DEGREES;
  private static final float INDICATOR_OFFSET_PER_COLOR_DEGREES = 360f;

  // Constants for animation timing.
  private static final long DURATION_PER_COLOR_IN_MS = (long) (1333 * ANIMATION_SPEED_FACTOR);
  private static final long COLOR_FADING_DURATION = (long) (333 * ANIMATION_SPEED_FACTOR);
  private static final long COLOR_FADING_DELAY = (long) (1000 * ANIMATION_SPEED_FACTOR);

  // Drawing delegate object.
  private final CircularDrawingDelegate drawingDelegate;

  // Index of current displayed indicator color.
  private int indicatorColorIndex;

  // Animators.
  private Animator mainAnimator;
  private ObjectAnimator colorFadingAnimator;
  private ObjectAnimator collapseAnimator;

  // Animator properties.
  private int displayedIndicatorColor;
  private float indicatorStartOffset;
  private float indicatorInCycleOffset;
  private float indicatorHeadChangeFraction;
  private float indicatorTailChangeFraction;

  // For IndeterminateAnimatorControl.
  boolean mainAnimatorCompleteEndRequested = false;
  boolean indicatorWillFullyCollapsed = false;
  AnimationCallback mainAnimatorCompleteCallback = null;

  CircularIndeterminateDrawable(@NonNull ProgressIndicator progressIndicator) {
    super(progressIndicator);

    drawingDelegate = new CircularDrawingDelegate();

    // Initializes Paint object.
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Cap.BUTT);
    paint.setAntiAlias(true);

    initializeAnimators();
  }

  // ******************* Initialization *******************

  private void initializeAnimators() {
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
            if (mainAnimatorCompleteEndRequested) {
              collapseAnimator.setFloatValues(
                  0f, 1f + INDICATOR_MIN_DEGREES / INDICATOR_DELTA_DEGREES);
              indicatorWillFullyCollapsed = true;
            }
          }
        });

    collapseAnimator = ObjectAnimator.ofFloat(this, INDICATOR_TAIL_CHANGE_FRACTION, 0f, 1f);
    collapseAnimator.setDuration(DURATION_PER_COLOR_IN_MS / 2);
    collapseAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);

    colorFadingAnimator =
        ObjectAnimator.ofObject(
            this,
            DISPLAYED_INDICATOR_COLOR,
            new ArgbEvaluatorCompat(),
            combinedIndicatorColorArray[indicatorColorIndex],
            combinedIndicatorColorArray[getNextIndicatorColorIndex()]);
    colorFadingAnimator.setDuration(COLOR_FADING_DURATION);
    colorFadingAnimator.setStartDelay(COLOR_FADING_DELAY);
    colorFadingAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);

    AnimatorSet set = new AnimatorSet();
    set.playSequentially(expandAnimator, collapseAnimator);
    set.playTogether(constantlyRotateAnimator, colorFadingAnimator);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            if (mainAnimatorCompleteEndRequested && indicatorWillFullyCollapsed) {
              mainAnimatorCompleteCallback.onAnimationEnd(CircularIndeterminateDrawable.this);
              mainAnimatorCompleteEndRequested = false;
              resetMainAnimatorPropertiesForEnd();
            } else {
              // If the drawable is still visible, continues the main animator by restarting.
              if (isVisible()) {
                resetMainAnimatorPropertiesForNextCycle();
                startMainAnimator();
              } else {
                resetMainAnimatorPropertiesForEnd();
              }
            }
          }
        });

    mainAnimator = set;

    // Add a listener to cancel main animator after hide.
    getHideAnimator()
        .addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cancelMainAnimatorImmediately();
                resetMainAnimatorPropertiesForEnd();
              }
            });

    resetMainAnimatorPropertiesForEnd();
    setGrowFraction(1f);
  }

  // ******************* Overridden methods *******************

  @Override
  public boolean setVisible(boolean visible, boolean animationDesired) {
    // Animations are not desired if tests request to disable them.
    if (animatorsDisabledForTesting) {
      animationDesired = false;
    }

    boolean changed = super.setVisible(visible, animationDesired);

    // Unless it's showing or hiding, cancels and resets main animator.
    if (!isRunning()) {
      cancelMainAnimatorImmediately();
      resetMainAnimatorPropertiesForEnd();
    }
    // Restarts the main animator if it's visible and needs to be animated.
    if (visible && animationDesired) {
      startMainAnimator();
    }

    return changed;
  }

  // ******************* Drawing methods *******************

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect clipBounds = new Rect();

    if (getBounds().isEmpty() || !isVisible() || !canvas.getClipBounds(clipBounds)) {
      // Escape if bounds are empty, clip bounds are empty, or currently hidden.
      return;
    }

    canvas.save();
    drawingDelegate.adjustCanvas(canvas, progressIndicator, getGrowFraction());

    float displayedIndicatorWidth = progressIndicator.getIndicatorWidth() * getGrowFraction();
    float displayedRoundedCornerRadius =
        progressIndicator.getIndicatorCornerRadius() * getGrowFraction();

    // Draws the track.
    drawingDelegate.fillTrackWithColor(
        canvas,
        paint,
        combinedTrackColor,
        0f,
        1f,
        displayedIndicatorWidth,
        displayedRoundedCornerRadius);
    // Draws the indicator.
    drawingDelegate.fillTrackWithColor(
        canvas,
        paint,
        displayedIndicatorColor,
        (getIndicatorStartOffset()
                + getIndicatorInCycleOffset()
                - INDICATOR_MIN_DEGREES
                + getIndicatorTailChangeFraction() * INDICATOR_DELTA_DEGREES)
            / 360,
        (getIndicatorStartOffset()
                + getIndicatorInCycleOffset()
                + getIndicatorHeadChangeFraction() * INDICATOR_DELTA_DEGREES)
            / 360,
        displayedIndicatorWidth,
        displayedRoundedCornerRadius);
    canvas.restore();
  }

  // ******************* Animation control *******************

  @Override
  public void startMainAnimator() {
    mainAnimator.start();
  }

  @Override
  public void resetMainAnimatorPropertiesForEnd() {
    setIndicatorHeadChangeFraction(0f);
    setIndicatorTailChangeFraction(0f);
    setIndicatorStartOffset(0f);
    indicatorColorIndex = 0;
    collapseAnimator.setFloatValues(0f, 1f);
    indicatorWillFullyCollapsed = false;
    colorFadingAnimator.setIntValues(
        combinedIndicatorColorArray[indicatorColorIndex],
        combinedIndicatorColorArray[getNextIndicatorColorIndex()]);
    displayedIndicatorColor = combinedIndicatorColorArray[indicatorColorIndex];
  }

  @Override
  public void resetMainAnimatorPropertiesForNextCycle() {
    setIndicatorHeadChangeFraction(0f);
    setIndicatorTailChangeFraction(0f);
    setIndicatorStartOffset(
        MathUtils.floorMod(
            getIndicatorStartOffset()
                + INDICATOR_OFFSET_PER_COLOR_DEGREES
                + INDICATOR_DELTA_DEGREES,
            360));
    indicatorColorIndex = getNextIndicatorColorIndex();
    colorFadingAnimator.setIntValues(
        combinedIndicatorColorArray[indicatorColorIndex],
        combinedIndicatorColorArray[getNextIndicatorColorIndex()]);
    displayedIndicatorColor = combinedIndicatorColorArray[indicatorColorIndex];
  }

  @Override
  public void cancelMainAnimatorImmediately() {
    mainAnimator.cancel();
  }

  @Override
  public void requestCancelMainAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (mainAnimatorCompleteEndRequested) {
      return;
    }

    if (isVisible()) {
      mainAnimatorCompleteEndRequested = true;
    } else {
      cancelMainAnimatorImmediately();
    }
  }

  @Override
  public void registerMainAnimatorCompleteEndCallback(AnimationCallback callback) {
    mainAnimatorCompleteCallback = callback;
  }

  // ******************* Helper methods *******************

  /** Returns the index of the next available color for indicator. */
  private int getNextIndicatorColorIndex() {
    return (indicatorColorIndex + 1) % combinedIndicatorColorArray.length;
  }

  // ******************* Getters and setters *******************

  private int getDisplayedIndicatorColor() {
    return displayedIndicatorColor;
  }

  private void setDisplayedIndicatorColor(int displayedIndicatorColor) {
    this.displayedIndicatorColor = displayedIndicatorColor;
    invalidateSelf();
  }

  private float getIndicatorStartOffset() {
    return indicatorStartOffset;
  }

  void setIndicatorStartOffset(float indicatorStartOffset) {
    this.indicatorStartOffset = indicatorStartOffset;
    invalidateSelf();
  }

  private float getIndicatorInCycleOffset() {
    return indicatorInCycleOffset;
  }

  void setIndicatorInCycleOffset(float indicatorInCycleOffset) {
    this.indicatorInCycleOffset = indicatorInCycleOffset;
    invalidateSelf();
  }

  private float getIndicatorHeadChangeFraction() {
    return indicatorHeadChangeFraction;
  }

  void setIndicatorHeadChangeFraction(float indicatorHeadChangeFraction) {
    this.indicatorHeadChangeFraction = indicatorHeadChangeFraction;
    invalidateSelf();
  }

  private float getIndicatorTailChangeFraction() {
    return indicatorTailChangeFraction;
  }

  void setIndicatorTailChangeFraction(float indicatorTailChangeFraction) {
    this.indicatorTailChangeFraction = indicatorTailChangeFraction;
    invalidateSelf();
  }

  // ******************* Properties *******************

  /** The property of indicator color being currently displayed. */
  private static final Property<CircularIndeterminateDrawable, Integer> DISPLAYED_INDICATOR_COLOR =
      new Property<CircularIndeterminateDrawable, Integer>(
          Integer.class, "displayedIndicatorColor") {
        @Override
        public Integer get(CircularIndeterminateDrawable drawble) {
          return drawble.getDisplayedIndicatorColor();
        }

        @Override
        public void set(CircularIndeterminateDrawable drawable, Integer value) {
          drawable.setDisplayedIndicatorColor(value);
        }
      };

  /**
   * The property of degrees which the indicator should rotate clockwise from the {@code
   * indicatorStartOffset}.
   */
  private static final Property<CircularIndeterminateDrawable, Float> INDICATOR_IN_CYCLE_OFFSET =
      new Property<CircularIndeterminateDrawable, Float>(Float.class, "indicatorInCycleOffset") {
        @Override
        public Float get(CircularIndeterminateDrawable drawable) {
          return drawable.getIndicatorInCycleOffset();
        }

        @Override
        public void set(CircularIndeterminateDrawable drawable, Float value) {
          drawable.setIndicatorInCycleOffset(value);
        }
      };

  /**
   * The property of the fraction of the head (the more clockwise end) of the indicator in the total
   * amount it can change.
   */
  private static final Property<CircularIndeterminateDrawable, Float>
      INDICATOR_HEAD_CHANGE_FRACTION =
          new Property<CircularIndeterminateDrawable, Float>(
              Float.class, "indicatorHeadChangeFraction") {
            @Override
            public Float get(CircularIndeterminateDrawable drawable) {
              return drawable.getIndicatorHeadChangeFraction();
            }

            @Override
            public void set(CircularIndeterminateDrawable drawable, Float value) {
              drawable.setIndicatorHeadChangeFraction(value);
            }
          };

  /**
   * The property of the fraction of the tail (the less clockwise end) of the indicator in the total
   * amount it can change.
   */
  private static final Property<CircularIndeterminateDrawable, Float>
      INDICATOR_TAIL_CHANGE_FRACTION =
          new Property<CircularIndeterminateDrawable, Float>(
              Float.class, "indicatorTailChangeFraction") {
            @Override
            public Float get(CircularIndeterminateDrawable drawable) {
              return drawable.getIndicatorTailChangeFraction();
            }

            @Override
            public void set(CircularIndeterminateDrawable drawable, Float value) {
              drawable.setIndicatorTailChangeFraction(value);
            }
          };
}
