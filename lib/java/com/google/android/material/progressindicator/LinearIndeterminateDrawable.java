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

import static com.google.android.material.progressindicator.ProgressIndicator.ANIMATION_SPEED_FACTOR;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.math.MathUtils;

/**
 * This is the implementation class for drawing progress indicator in the linear indeterminate mode.
 */
class LinearIndeterminateDrawable extends DrawableWithAnimatedVisibilityChange
    implements IndeterminateAnimatorControl {

  // Constants for animation timing.
  private static final long MAIN_LINE_1_HEAD_DURATION = (long) (750 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_1_TAIL_DELAY = (long) (333 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_1_TAIL_DURATION = (long) (850 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_2_HEAD_DELAY = (long) (1000 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_2_HEAD_DURATION = (long) (567 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_2_TAIL_DELAY = (long) (1267 * ANIMATION_SPEED_FACTOR);
  private static final long MAIN_LINE_2_TAIL_DURATION = (long) (533 * ANIMATION_SPEED_FACTOR);

  private static final long SEAMLESS_NEXT_COLOR_DELAY = (long) (333 * ANIMATION_SPEED_FACTOR);
  private static final long SEAMLESS_DURATION_PER_COLOR = (long) (667 * ANIMATION_SPEED_FACTOR);

  private final Context context;

  // Drawing delegate object.
  private final LinearDrawingDelegate drawingDelegate;

  // Index of current displayed indicator color.
  private int indicatorColorIndex;

  // Animators.
  private Animator mainAnimator;
  private Animator mainAnimatorSeamless;

  // Animator properties in non-seamless mode.
  private float line1HeadFraction;
  private float line1TailFraction;
  private float line2HeadFraction;
  private float line2TailFraction;

  // Animator properties in seamless mode.
  private float lineConnectPoint1Fraction;
  private float lineConnectPoint2Fraction;

  // For IndeterminateAnimatorControl.
  boolean mainAnimatorCompleteEndRequested = false;
  AnimationCallback mainAnimatorCompleteCallback = null;

  LinearIndeterminateDrawable(
      @NonNull Context context, @NonNull ProgressIndicator progressIndicator) {
    super(progressIndicator);

    drawingDelegate = new LinearDrawingDelegate();
    this.context = context;

    // Initializes Paint object.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);

    initializeAnimators();
  }

  // ******************* Initialization *******************

  private void initializeAnimators() {
    initializeMainAnimatorSeamless();
    initializeMainAnimator();

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

    startMainAnimator();
  }

  private void initializeMainAnimatorSeamless() {
    ObjectAnimator connectPoint1Animator =
        ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_1_FRACTION, 0f, 1f);
    connectPoint1Animator.setDuration(SEAMLESS_DURATION_PER_COLOR);
    connectPoint1Animator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    connectPoint1Animator.setRepeatCount(ValueAnimator.INFINITE);
    connectPoint1Animator.setRepeatMode(ValueAnimator.RESTART);
    connectPoint1Animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
            rotateIndicatorColor();
          }
        });

    ObjectAnimator connectPoint2StayAtZeroAnimator =
        ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_2_FRACTION, 0f, 0f);
    connectPoint2StayAtZeroAnimator.setDuration(SEAMLESS_NEXT_COLOR_DELAY);

    ObjectAnimator connectPoint2Animator =
        ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_2_FRACTION, 0f, 1f);
    connectPoint2Animator.setDuration(SEAMLESS_DURATION_PER_COLOR);
    connectPoint2Animator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    connectPoint2Animator.setRepeatCount(ValueAnimator.INFINITE);
    connectPoint2Animator.setRepeatMode(ValueAnimator.RESTART);
    connectPoint2Animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
            rotateIndicatorColor();
          }
        });

    AnimatorSet connectPoint2AnimatorSet = new AnimatorSet();
    connectPoint2AnimatorSet.playSequentially(
        connectPoint2StayAtZeroAnimator, connectPoint2Animator);

    AnimatorSet set = new AnimatorSet();
    set.playTogether(connectPoint1Animator, connectPoint2AnimatorSet);

    mainAnimatorSeamless = set;
  }

  private void initializeMainAnimator() {
    ObjectAnimator line1HeadAnimator = ObjectAnimator.ofFloat(this, LINE_1_HEAD_FRACTION, 0f, 1f);
    line1HeadAnimator.setDuration(MAIN_LINE_1_HEAD_DURATION);
    line1HeadAnimator.setInterpolator(
        AnimationUtilsCompat.loadInterpolator(
            context, R.animator.linear_indeterminate_line1_head_interpolator));

    ObjectAnimator line1TailAnimator = ObjectAnimator.ofFloat(this, LINE_1_TAIL_FRACTION, 0f, 1f);
    line1TailAnimator.setStartDelay(MAIN_LINE_1_TAIL_DELAY);
    line1TailAnimator.setDuration(MAIN_LINE_1_TAIL_DURATION);
    line1TailAnimator.setInterpolator(
        AnimationUtilsCompat.loadInterpolator(
            context, R.animator.linear_indeterminate_line1_tail_interpolator));

    ObjectAnimator line2HeadAnimator = ObjectAnimator.ofFloat(this, LINE_2_HEAD_FRACTION, 0f, 1f);
    line2HeadAnimator.setStartDelay(MAIN_LINE_2_HEAD_DELAY);
    line2HeadAnimator.setDuration(MAIN_LINE_2_HEAD_DURATION);
    line2HeadAnimator.setInterpolator(
        AnimationUtilsCompat.loadInterpolator(
            context, R.animator.linear_indeterminate_line2_head_interpolator));

    ObjectAnimator line2TailAnimator = ObjectAnimator.ofFloat(this, LINE_2_TAIL_FRACTION, 0f, 1f);
    line2TailAnimator.setStartDelay(MAIN_LINE_2_TAIL_DELAY);
    line2TailAnimator.setDuration(MAIN_LINE_2_TAIL_DURATION);
    line2TailAnimator.setInterpolator(
        AnimationUtilsCompat.loadInterpolator(
            context, R.animator.linear_indeterminate_line2_tail_interpolator));

    AnimatorSet set = new AnimatorSet();
    set.playTogether(line1HeadAnimator, line1TailAnimator, line2HeadAnimator, line2TailAnimator);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            if (mainAnimatorCompleteEndRequested) {
              mainAnimatorCompleteCallback.onAnimationEnd(LinearIndeterminateDrawable.this);
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
  }

  // ******************* Overridden methods *******************

  /**
   * Sets the visibility of this drawable. It calls the {@link
   * DrawableWithAnimatedVisibilityChange#setVisible(boolean, boolean)} to start the show/hide
   * animation properly. If it's requested to be visible, the main animator will be started. If it's
   * required to be invisible, the main animator will be canceled unless the hide animation is
   * required.
   *
   * @param visible Whether to make the drawable visible.
   * @param animationDesired Whether to change the visibility with animation.
   */
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

  /** Draws the graphics based on the progress indicator's properties and the animation states. */
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

    if (progressIndicator.isLinearSeamless()) {
      float lineConnectPointLeftFraction =
          Math.min(getLineConnectPoint1Fraction(), getLineConnectPoint2Fraction());
      float lineConnectPointRightFraction =
          Math.max(getLineConnectPoint1Fraction(), getLineConnectPoint2Fraction());
      int indicatorLeftColorIndex =
          MathUtils.floorMod(indicatorColorIndex + 2, combinedIndicatorColorArray.length);
      int indicatorCentralColorIndex =
          MathUtils.floorMod(indicatorColorIndex + 1, combinedIndicatorColorArray.length);
      // Draws the left line.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedIndicatorColorArray[indicatorLeftColorIndex],
          0f,
          lineConnectPointLeftFraction,
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
      // Draws the central line.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedIndicatorColorArray[indicatorCentralColorIndex],
          lineConnectPointLeftFraction,
          lineConnectPointRightFraction,
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
      // Draws the right line.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedIndicatorColorArray[indicatorColorIndex],
          lineConnectPointRightFraction,
          1f,
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
    } else {
      // Draws the track.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedTrackColor,
          0f,
          1f,
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
      // Draws line 1.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedIndicatorColorArray[indicatorColorIndex],
          getLine1TailFraction(),
          getLine1HeadFraction(),
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
      // Draws line 2.
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          combinedIndicatorColorArray[indicatorColorIndex],
          getLine2TailFraction(),
          getLine2HeadFraction(),
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
    }
  }

  // ******************* Animation control *******************

  @Override
  public void startMainAnimator() {
    if (progressIndicator.isLinearSeamless()) {
      mainAnimatorSeamless.start();
    } else {
      mainAnimator.start();
    }
  }

  @Override
  public void resetMainAnimatorPropertiesForEnd() {
    resetMainAnimatorPropertiesForNextCycle();

    lineConnectPoint1Fraction = 0f;
    lineConnectPoint2Fraction = 0f;
    indicatorColorIndex = 0;
  }

  @Override
  public void resetMainAnimatorPropertiesForNextCycle() {
    line1HeadFraction = 0f;
    line1TailFraction = 0f;
    line2HeadFraction = 0f;
    line2TailFraction = 0f;
  }

  @Override
  public void cancelMainAnimatorImmediately() {
    mainAnimator.cancel();
    mainAnimatorSeamless.cancel();
  }

  @Override
  public void requestCancelMainAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (mainAnimatorCompleteEndRequested) {
      return;
    }

    if (!isVisible()) {
      cancelMainAnimatorImmediately();
      return;
    }

    // Main animator can only be ended with complete cycle if it's non seamless.
    if (!progressIndicator.isLinearSeamless()) {
      mainAnimatorCompleteEndRequested = true;
    }
  }

  @Override
  public void registerMainAnimatorCompleteEndCallback(AnimationCallback callback) {
    mainAnimatorCompleteCallback = callback;
  }

  // ******************* Helper methods *******************

  /** Rotates color index in indicator color index. */
  private void rotateIndicatorColor() {
    indicatorColorIndex = (indicatorColorIndex + 1) % combinedIndicatorColorArray.length;
  }

  // ******************* Getters and setters *******************

  private float getLine1HeadFraction() {
    return line1HeadFraction;
  }

  void setLine1HeadFraction(float line1HeadFraction) {
    this.line1HeadFraction = line1HeadFraction;
    invalidateSelf();
  }

  private float getLine1TailFraction() {
    return line1TailFraction;
  }

  void setLine1TailFraction(float line1TailFraction) {
    this.line1TailFraction = line1TailFraction;
    invalidateSelf();
  }

  private float getLine2HeadFraction() {
    return line2HeadFraction;
  }

  void setLine2HeadFraction(float line2HeadFraction) {
    this.line2HeadFraction = line2HeadFraction;
    invalidateSelf();
  }

  private float getLine2TailFraction() {
    return line2TailFraction;
  }

  void setLine2TailFraction(float line2TailFraction) {
    this.line2TailFraction = line2TailFraction;
    invalidateSelf();
  }

  private float getLineConnectPoint1Fraction() {
    return lineConnectPoint1Fraction;
  }

  void setLineConnectPoint1Fraction(float lineConnectPoint1Fraction) {
    this.lineConnectPoint1Fraction = lineConnectPoint1Fraction;
    invalidateSelf();
  }

  private float getLineConnectPoint2Fraction() {
    return lineConnectPoint2Fraction;
  }

  void setLineConnectPoint2Fraction(float lineConnectPoint2Fraction) {
    this.lineConnectPoint2Fraction = lineConnectPoint2Fraction;
    invalidateSelf();
  }

  // ******************* Properties *******************

  /**
   * The property controlled by the main animator for non seamless mode. It indicates the ratio to
   * the total track width of the distance between the left (right when inverse) end of the track
   * and the right (left) end of the first line.
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_1_HEAD_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "line1HeadFraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLine1HeadFraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLine1HeadFraction(value);
        }
      };

  /**
   * The property controlled by the main animator for non seamless mode. It indicates the ratio to
   * the total track width of the distance between the left (right when inverse) end of the track
   * and the left (right) end of the first line.
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_1_TAIL_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "line1TailFraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLine1TailFraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLine1TailFraction(value);
        }
      };

  /**
   * The property controlled by the main animator for non seamless mode. It indicates the ratio to
   * the total track width of the distance between the left (right when inverse) end of the track
   * and the right (left) end of the second line.
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_2_HEAD_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "line2HeadFraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLine2HeadFraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLine2HeadFraction(value);
        }
      };

  /**
   * The property controlled by the main animator for non seamless mode. It indicates the ratio to
   * the total track width of the distance between the left (right when inverse) end of the track
   * and the left (right) end of the second line.
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_2_TAIL_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "line2TailFraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLine2TailFraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLine2TailFraction(value);
        }
      };

  /**
   * The property controlled by the main animator for seamless mode. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the connecting position of one side line and the central line.
   *
   * @see #LINE_CONNECT_POINT_2_FRACTION
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_CONNECT_POINT_1_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "lineConnectPoint1Fraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLineConnectPoint1Fraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLineConnectPoint1Fraction(value);
        }
      };

  /**
   * The property controlled by the main animator for seamless mode. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the connecting position of the other side line to the central line.
   *
   * @see #LINE_CONNECT_POINT_1_FRACTION
   */
  private static final Property<LinearIndeterminateDrawable, Float> LINE_CONNECT_POINT_2_FRACTION =
      new Property<LinearIndeterminateDrawable, Float>(Float.class, "lineConnectPoint2Fraction") {
        @Override
        public Float get(LinearIndeterminateDrawable drawable) {
          return drawable.getLineConnectPoint2Fraction();
        }

        @Override
        public void set(LinearIndeterminateDrawable drawable, Float value) {
          drawable.setLineConnectPoint2Fraction(value);
        }
      };
}
