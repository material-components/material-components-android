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

import static java.lang.Math.max;
import static java.lang.Math.min;

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
import com.google.android.material.color.MaterialColors;
import java.util.Arrays;

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
  private final Interpolator[] interpolatorArray;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // For animator control.
  private int indicatorColorIndex = 0;
  private boolean dirtyColors;
  private float animationFraction;
  private boolean animatorCompleteEndRequested;
  AnimationCallback animatorCompleteCallback = null;

  public LinearIndeterminateDisjointAnimatorDelegate(
      @NonNull Context context, @NonNull LinearProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 2);

    baseSpec = spec;

    interpolatorArray =
        new Interpolator[] {
          AnimationUtilsCompat.loadInterpolator(
              context, R.animator.linear_indeterminate_line1_head_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.animator.linear_indeterminate_line1_tail_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.animator.linear_indeterminate_line2_head_interpolator),
          AnimationUtilsCompat.loadInterpolator(
              context, R.animator.linear_indeterminate_line2_tail_interpolator)
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
      animator.setDuration(TOTAL_DURATION_IN_MS);
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

            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              if (animatorCompleteEndRequested) {
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animatorCompleteCallback.onAnimationEnd(drawable);
                animatorCompleteEndRequested = false;
              }
            }
          });
    }
  }

  @Override
  public void cancelAnimatorImmediately() {
    if (animator != null) {
      animator.cancel();
    }
  }

  @Override
  public void requestCancelAnimatorAfterCurrentCycle() {
    if (drawable.isVisible()) {
      animatorCompleteEndRequested = true;
      if (animator != null) {
        animator.setRepeatCount(0);
      }
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
    for (int i = 0; i < 4; i++) {
      float fraction =
          getFractionInRange(
              playtime, DELAY_TO_MOVE_SEGMENT_ENDS[i], DURATION_TO_MOVE_SEGMENT_ENDS[i]);
      float segmentPosition = interpolatorArray[i].getInterpolation(fraction);
      segmentPositions[i] = max(0f, min(1f, segmentPosition));
    }
  }

  /** Updates the segment color array based on the updated color index. */
  private void maybeUpdateSegmentColors() {
    if (dirtyColors) {
      Arrays.fill(
          segmentColors,
          MaterialColors.compositeARGBWithAlpha(
              baseSpec.indicatorColors[indicatorColorIndex], drawable.getAlpha()));
      dirtyColors = false;
    }
  }

  @VisibleForTesting
  void resetPropertiesForNewStart() {
    indicatorColorIndex = 0;
    int indicatorColor =
        MaterialColors.compositeARGBWithAlpha(baseSpec.indicatorColors[0], drawable.getAlpha());
    segmentColors[0] = indicatorColor;
    segmentColors[1] = indicatorColor;
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
