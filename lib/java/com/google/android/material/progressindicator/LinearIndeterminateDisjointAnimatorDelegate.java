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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat;
import com.google.android.material.color.MaterialColors;
import java.util.Arrays;

/**
 * This is the implementation class for drawing progress indicator in the linear disjoint
 * indeterminate mode.
 */
final class LinearIndeterminateDisjointAnimatorDelegate
    extends IndeterminateAnimatorDelegate<AnimatorSet> {

  // Constants for animation timing.
  private static final int MAIN_LINE_1_HEAD_DURATION = 750;
  private static final int MAIN_LINE_1_TAIL_DELAY = 333;
  private static final int MAIN_LINE_1_TAIL_DURATION = 850;
  private static final int MAIN_LINE_2_HEAD_DELAY = 1000;
  private static final int MAIN_LINE_2_HEAD_DURATION = 567;
  private static final int MAIN_LINE_2_TAIL_DELAY = 1267;
  private static final int MAIN_LINE_2_TAIL_DURATION = 533;

  // The current context which the animation is running on.
  private final Context context;
  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // The animator controls disjoint linear indeterminate animation.
  private AnimatorSet animatorSet;

  // Internal parameters controlled by the animator.
  private int displayedSegmentColorIndex;
  private float line1HeadFraction;
  private float line1TailFraction;
  private float line2HeadFraction;
  private float line2TailFraction;

  // For animator control.
  boolean animatorCompleteEndRequested = false;
  AnimationCallback animatorCompleteCallback = null;

  public LinearIndeterminateDisjointAnimatorDelegate(
      @NonNull Context context, @NonNull LinearProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 2);

    this.context = context;
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

      animatorSet = new AnimatorSet();
      animatorSet.playTogether(
          line1HeadAnimator, line1TailAnimator, line2HeadAnimator, line2TailAnimator);
      animatorSet.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);

              if (animatorCompleteEndRequested) {
                animatorCompleteEndRequested = false;
                animatorCompleteCallback.onAnimationEnd(drawable);
                resetPropertiesForNewStart();
              } else {
                // If the drawable is still visible, continues the main animator by restarting.
                if (drawable.isVisible()) {
                  resetPropertiesForNextCycle();
                  startAnimator();
                } else {
                  resetPropertiesForNewStart();
                }
              }
            }
          });
    }
  }

  @Override
  public void resetPropertiesForNewStart() {
    resetPropertiesForNextCycle();
    resetSegmentColors();
  }

  @Override
  public void resetPropertiesForNextCycle() {
    setLine1HeadFraction(0f);
    setLine1TailFraction(0f);
    setLine2HeadFraction(0f);
    setLine2TailFraction(0f);
    rotateSegmentColors();
  }

  @Override
  public void cancelAnimatorImmediately() {
    if (animatorSet != null) {
      animatorSet.cancel();
    }
  }

  @Override
  public void requestCancelAnimatorAfterCurrentCycle() {
    // Do nothing if main animator complete end has been requested.
    if (animatorCompleteEndRequested) {
      return;
    }

    if (!drawable.isVisible()) {
      cancelAnimatorImmediately();
      return;
    }

    animatorCompleteEndRequested = true;
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

  /** Shifts the color used in the segment colors to the next available one. */
  private void rotateSegmentColors() {
    displayedSegmentColorIndex = (displayedSegmentColorIndex + 1) % baseSpec.indicatorColors.length;
    Arrays.fill(
        segmentColors,
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[displayedSegmentColorIndex], drawable.getAlpha()));
  }

  /** Resets the segment colors to the first indicator color. */
  private void resetSegmentColors() {
    displayedSegmentColorIndex = 0;
    Arrays.fill(
        segmentColors,
        MaterialColors.compositeARGBWithAlpha(
            baseSpec.indicatorColors[displayedSegmentColorIndex], drawable.getAlpha()));
  }

  // ******************* Getters and setters *******************

  private float getLine1HeadFraction() {
    return line1HeadFraction;
  }

  @VisibleForTesting
  void setLine1HeadFraction(float line1HeadFraction) {
    this.line1HeadFraction = line1HeadFraction;
    this.segmentPositions[3] = line1HeadFraction;
    drawable.invalidateSelf();
  }

  private float getLine1TailFraction() {
    return line1TailFraction;
  }

  @VisibleForTesting
  void setLine1TailFraction(float line1TailFraction) {
    this.line1TailFraction = line1TailFraction;
    this.segmentPositions[2] = line1TailFraction;
    drawable.invalidateSelf();
  }

  private float getLine2HeadFraction() {
    return line2HeadFraction;
  }

  @VisibleForTesting
  void setLine2HeadFraction(float line2HeadFraction) {
    this.line2HeadFraction = line2HeadFraction;
    this.segmentPositions[1] = line2HeadFraction;
    drawable.invalidateSelf();
  }

  private float getLine2TailFraction() {
    return line2TailFraction;
  }

  @VisibleForTesting
  void setLine2TailFraction(float line2TailFraction) {
    this.line2TailFraction = line2TailFraction;
    this.segmentPositions[0] = line2TailFraction;
    drawable.invalidateSelf();
  }

  // ******************* Properties *******************

  /**
   * The property controlled by the main animator in disjoint type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the right (left) end of the first line.
   */
  private static final Property<LinearIndeterminateDisjointAnimatorDelegate, Float>
      LINE_1_HEAD_FRACTION =
          new Property<LinearIndeterminateDisjointAnimatorDelegate, Float>(
              Float.class, "line1HeadFraction") {
            @Override
            public Float get(LinearIndeterminateDisjointAnimatorDelegate drawable) {
              return drawable.getLine1HeadFraction();
            }

            @Override
            public void set(LinearIndeterminateDisjointAnimatorDelegate drawable, Float value) {
              drawable.setLine1HeadFraction(value);
            }
          };

  /**
   * The property controlled by the main animator in disjoint type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the left (right) end of the first line.
   */
  private static final Property<LinearIndeterminateDisjointAnimatorDelegate, Float>
      LINE_1_TAIL_FRACTION =
          new Property<LinearIndeterminateDisjointAnimatorDelegate, Float>(
              Float.class, "line1TailFraction") {
            @Override
            public Float get(LinearIndeterminateDisjointAnimatorDelegate drawable) {
              return drawable.getLine1TailFraction();
            }

            @Override
            public void set(LinearIndeterminateDisjointAnimatorDelegate drawable, Float value) {
              drawable.setLine1TailFraction(value);
            }
          };

  /**
   * The property controlled by the main animator in disjoint type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the right (left) end of the second line.
   */
  private static final Property<LinearIndeterminateDisjointAnimatorDelegate, Float>
      LINE_2_HEAD_FRACTION =
          new Property<LinearIndeterminateDisjointAnimatorDelegate, Float>(
              Float.class, "line2HeadFraction") {
            @Override
            public Float get(LinearIndeterminateDisjointAnimatorDelegate drawable) {
              return drawable.getLine2HeadFraction();
            }

            @Override
            public void set(LinearIndeterminateDisjointAnimatorDelegate drawable, Float value) {
              drawable.setLine2HeadFraction(value);
            }
          };

  /**
   * The property controlled by the main animator in disjoint type. It indicates the ratio to the
   * total track width of the distance between the left (right when inverse) end of the track and
   * the left (right) end of the second line.
   */
  private static final Property<LinearIndeterminateDisjointAnimatorDelegate, Float>
      LINE_2_TAIL_FRACTION =
          new Property<LinearIndeterminateDisjointAnimatorDelegate, Float>(
              Float.class, "line2TailFraction") {
            @Override
            public Float get(LinearIndeterminateDisjointAnimatorDelegate drawable) {
              return drawable.getLine2TailFraction();
            }

            @Override
            public void set(LinearIndeterminateDisjointAnimatorDelegate drawable, Float value) {
              drawable.setLine2TailFraction(value);
            }
          };
}
