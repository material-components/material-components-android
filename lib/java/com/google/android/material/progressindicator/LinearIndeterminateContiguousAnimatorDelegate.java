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
import android.animation.ValueAnimator;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.math.MathUtils;

/**
 * This is the implementation class for drawing progress indicator in the linear contiguous
 * indeterminate mode.
 */
final class LinearIndeterminateContiguousAnimatorDelegate
    extends IndeterminateAnimatorDelegate<AnimatorSet> {

  // Constants for animation timing.
  private static final int NEXT_COLOR_DELAY = 333;
  private static final int DURATION_PER_COLOR = 667;

  // The general spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // The animator controls contiguous linear indeterminate animation.
  private AnimatorSet animatorSet;

  // Internal parameters controlled by the animator.
  private int referenceSegmentColorIndex;
  private float lineConnectPoint1Fraction;
  private float lineConnectPoint2Fraction;

  public LinearIndeterminateContiguousAnimatorDelegate(@NonNull LinearProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 3);

    baseSpec = spec;
  }

  // ******************* Animation control *******************

  @Override
  public void startAnimator() {
    maybeInitializeAnimators();

    animatorSet.start();
  }

  private void maybeInitializeAnimators() {
    if (animatorSet == null) {
      // Instantiates the animator.
      ObjectAnimator connectPoint1Animator =
          ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_1_FRACTION, 0f, 1f);
      connectPoint1Animator.setDuration(DURATION_PER_COLOR);
      connectPoint1Animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      connectPoint1Animator.setRepeatCount(ValueAnimator.INFINITE);
      connectPoint1Animator.setRepeatMode(ValueAnimator.RESTART);
      connectPoint1Animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
              super.onAnimationRepeat(animation);
              if (getLineConnectPoint2Fraction() > 0 && getLineConnectPoint2Fraction() < 1) {
                shiftSegmentColors();
              }
            }
          });

      ObjectAnimator connectPoint2StayAtZeroAnimator =
          ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_2_FRACTION, 0f, 0f);
      connectPoint2StayAtZeroAnimator.setDuration(NEXT_COLOR_DELAY);

      ObjectAnimator connectPoint2Animator =
          ObjectAnimator.ofFloat(this, LINE_CONNECT_POINT_2_FRACTION, 0f, 1f);
      connectPoint2Animator.setDuration(DURATION_PER_COLOR);
      connectPoint2Animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      connectPoint2Animator.setRepeatCount(ValueAnimator.INFINITE);
      connectPoint2Animator.setRepeatMode(ValueAnimator.RESTART);
      connectPoint2Animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
              super.onAnimationRepeat(animation);
              if (getLineConnectPoint1Fraction() > 0 && getLineConnectPoint1Fraction() < 1) {
                shiftSegmentColors();
              }
            }
          });

      AnimatorSet connectPoint2AnimatorSet = new AnimatorSet();
      connectPoint2AnimatorSet.playSequentially(
          connectPoint2StayAtZeroAnimator, connectPoint2Animator);

      animatorSet = new AnimatorSet();
      animatorSet.playTogether(connectPoint1Animator, connectPoint2AnimatorSet);
    }
  }

  @Override
  public void resetPropertiesForNewStart() {
    setLineConnectPoint1Fraction(0f);
    setLineConnectPoint2Fraction(0f);
    resetSegmentColors();
  }

  @Override
  public void resetPropertiesForNextCycle() {
    // In contiguous type, there's no concept of cycle. This is left as blank in purpose.
  }

  @Override
  public void cancelAnimatorImmediately() {
    if (animatorSet != null) {
      animatorSet.cancel();
    }
  }

  @Override
  public void requestCancelAnimatorAfterCurrentCycle() {
    // In contiguous type, there's no concept of cycle. This is left as blank in purpose.
  }

  @Override
  public void invalidateSpecValues() {
    resetSegmentColors();
  }

  @Override
  public void registerAnimatorsCompleteCallback(@Nullable AnimationCallback callback) {
    // In contiguous type, indeterminate mode cannot be switched. This is left as blank in purpose.
  }

  @Override
  public void unregisterAnimatorsCompleteCallback() {
    // In contiguous type, indeterminate mode cannot be switched. This is left as blank in purpose.
  }

  // ******************* Helper methods *******************

  /** Shifts the color used in the segment colors to the next available one. */
  private void shiftSegmentColors() {
    referenceSegmentColorIndex = (referenceSegmentColorIndex + 1) % baseSpec.indicatorColors.length;
    updateSegmentColors();
  }

  /** Resets the segment colors to the first indicator color. */
  private void resetSegmentColors() {
    referenceSegmentColorIndex = 0;
    updateSegmentColors();
  }

  /** Updates the segment colors array based on current reference color index. */
  private void updateSegmentColors() {
    int leftSegmentColorIndex =
        MathUtils.floorMod(referenceSegmentColorIndex + 2, baseSpec.indicatorColors.length);
    int centralSegmentColorIndex =
        MathUtils.floorMod(referenceSegmentColorIndex + 1, baseSpec.indicatorColors.length);
    segmentColors[0] =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[leftSegmentColorIndex], drawable.getAlpha());
    segmentColors[1] =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[centralSegmentColorIndex], drawable.getAlpha());
    segmentColors[2] =
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[referenceSegmentColorIndex], drawable.getAlpha());
  }

  /**
   * Updates the segment positions array based on current {@link #lineConnectPoint1Fraction} and
   * {@link #lineConnectPoint2Fraction};
   */
  private void updateSegmentPositions() {
    segmentPositions[0] = 0f;
    segmentPositions[1] =
        segmentPositions[2] =
            Math.min(getLineConnectPoint1Fraction(), getLineConnectPoint2Fraction());
    segmentPositions[3] =
        segmentPositions[4] =
            Math.max(getLineConnectPoint1Fraction(), getLineConnectPoint2Fraction());
    segmentPositions[5] = 1f;
  }

  // ******************* Getters and setters *******************

  private float getLineConnectPoint1Fraction() {
    return lineConnectPoint1Fraction;
  }

  @VisibleForTesting
  void setLineConnectPoint1Fraction(float lineConnectPoint1Fraction) {
    this.lineConnectPoint1Fraction = lineConnectPoint1Fraction;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  private float getLineConnectPoint2Fraction() {
    return lineConnectPoint2Fraction;
  }

  @VisibleForTesting
  void setLineConnectPoint2Fraction(float lineConnectPoint2Fraction) {
    this.lineConnectPoint2Fraction = lineConnectPoint2Fraction;
    updateSegmentPositions();
    drawable.invalidateSelf();
  }

  // ******************* Properties *******************

  /**
   * The property controlled by the main animator in contiguous type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the connecting position of one side line and the central line.
   *
   * @see #LINE_CONNECT_POINT_2_FRACTION
   */
  private static final Property<LinearIndeterminateContiguousAnimatorDelegate, Float>
      LINE_CONNECT_POINT_1_FRACTION =
          new Property<LinearIndeterminateContiguousAnimatorDelegate, Float>(
              Float.class, "lineConnectPoint1Fraction") {
            @Override
            public Float get(LinearIndeterminateContiguousAnimatorDelegate drawable) {
              return drawable.getLineConnectPoint1Fraction();
            }

            @Override
            public void set(LinearIndeterminateContiguousAnimatorDelegate drawable, Float value) {
              drawable.setLineConnectPoint1Fraction(value);
            }
          };

  /**
   * The property controlled by the main animator in contiguous type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the connecting position of the other side line to the central line.
   *
   * @see #LINE_CONNECT_POINT_1_FRACTION
   */
  private static final Property<LinearIndeterminateContiguousAnimatorDelegate, Float>
      LINE_CONNECT_POINT_2_FRACTION =
          new Property<LinearIndeterminateContiguousAnimatorDelegate, Float>(
              Float.class, "lineConnectPoint2Fraction") {
            @Override
            public Float get(LinearIndeterminateContiguousAnimatorDelegate drawable) {
              return drawable.getLineConnectPoint2Fraction();
            }

            @Override
            public void set(LinearIndeterminateContiguousAnimatorDelegate drawable, Float value) {
              drawable.setLineConnectPoint2Fraction(value);
            }
          };
}
