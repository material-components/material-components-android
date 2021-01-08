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
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import com.google.android.material.color.MaterialColors;
import java.util.Arrays;

/**
 * This is the implementation class for drawing progress indicator in the linear contiguous
 * indeterminate mode.
 */
final class LinearIndeterminateContiguousAnimatorDelegate
    extends IndeterminateAnimatorDelegate<ObjectAnimator> {

  // Constants for animation timing.
  private static final int TOTAL_DURATION_IN_MS = 667;
  private static final int DURATION_PER_CYCLE_IN_MS = 333;

  private ObjectAnimator animator;
  private FastOutSlowInInterpolator interpolator;

  // The base spec.
  private final BaseProgressIndicatorSpec baseSpec;

  // Internal parameters controlled by the animator.
  private int newIndicatorColorIndex = 1;
  private boolean dirtyColors;
  private float animationFraction;

  public LinearIndeterminateContiguousAnimatorDelegate(@NonNull LinearProgressIndicatorSpec spec) {
    super(/*segmentCount=*/ 3);

    baseSpec = spec;

    interpolator = new FastOutSlowInInterpolator();
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
      animator.setDuration(DURATION_PER_CYCLE_IN_MS);
      animator.setInterpolator(null);
      animator.setRepeatCount(ValueAnimator.INFINITE);
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
              super.onAnimationRepeat(animation);
              newIndicatorColorIndex =
                  (newIndicatorColorIndex + 1) % baseSpec.indicatorColors.length;
              dirtyColors = true;
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
    // This function is used to cancel animator after a complete cycle when switching to determinate
    // type. In contiguous type, the switching is not supported. This is left in purpose.
  }

  @Override
  public void invalidateSpecValues() {
    resetPropertiesForNewStart();
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

  /** Updates the segment position array based on current playtime. */
  private void updateSegmentPositions(int playtime) {
    segmentPositions[0] = 0f;
    float fraction = getFractionInRange(playtime, 0, TOTAL_DURATION_IN_MS);
    segmentPositions[1] = segmentPositions[2] = interpolator.getInterpolation(fraction);
    fraction += (float) DURATION_PER_CYCLE_IN_MS / TOTAL_DURATION_IN_MS;
    segmentPositions[3] = segmentPositions[4] = interpolator.getInterpolation(fraction);
    segmentPositions[5] = 1f;
  }

  /** Updates the segment colors array based on the updated color index. */
  private void maybeUpdateSegmentColors() {
    if (dirtyColors && segmentPositions[3] < 1f) {
      segmentColors[2] = segmentColors[1];
      segmentColors[1] = segmentColors[0];
      segmentColors[0] =
          MaterialColors.compositeARGBWithAlpha(
              baseSpec.indicatorColors[newIndicatorColorIndex], drawable.getAlpha());
      dirtyColors = false;
    }
  }

  @VisibleForTesting
  void resetPropertiesForNewStart() {
    dirtyColors = true;
    newIndicatorColorIndex = 1;
    Arrays.fill(
        segmentColors,
        MaterialColors.compositeARGBWithAlpha(baseSpec.indicatorColors[0], drawable.getAlpha()));
  }

  // ******************* Getters and setters *******************

  private float getAnimationFraction() {
    return animationFraction;
  }

  @VisibleForTesting
  void setAnimationFraction(float value) {
    animationFraction = value;
    int playtime = (int) (animationFraction * DURATION_PER_CYCLE_IN_MS);
    updateSegmentPositions(playtime);
    maybeUpdateSegmentColors();
    drawable.invalidateSelf();
  }

  // ******************* Properties *******************

  private static final Property<LinearIndeterminateContiguousAnimatorDelegate, Float>
      ANIMATION_FRACTION =
          new Property<LinearIndeterminateContiguousAnimatorDelegate, Float>(
              Float.class, "animationFraction") {
            @Override
            public Float get(LinearIndeterminateContiguousAnimatorDelegate delegate) {
              return delegate.getAnimationFraction();
            }

            @Override
            public void set(LinearIndeterminateContiguousAnimatorDelegate delegate, Float value) {
              delegate.setAnimationFraction(value);
            }
          };
}
