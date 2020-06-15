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

import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * This class draws the graphics for determinate modes by applying different {@link DrawingDelegate}
 * for different indicator types.
 */
class DeterminateDrawable extends DrawableWithAnimatedVisibilityChange {

  // Constants for drawing progress.
  private static final int MAX_DRAWABLE_LEVEL = 10000;
  // Drawing delegate object.
  private final DrawingDelegate drawingDelegate;
  // Animation.
  private SpringAnimation springAnimator;
  // Fraction of displayed indicator in the total width.
  private float indicatorFraction;

  DeterminateDrawable(
      @NonNull ProgressIndicator progressIndicator, @NonNull DrawingDelegate drawingDelegate) {
    super(progressIndicator);

    this.drawingDelegate = drawingDelegate;

    initializeAnimator();
  }

  // ******************* Initialization *******************

  private void initializeAnimator() {
    SpringForce springForce = new SpringForce();
    springForce.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
    springForce.setStiffness(50f);

    springAnimator = new SpringAnimation(this, INDICATOR_LENGTH_FRACTION);
    springAnimator.setSpring(springForce);
    springAnimator.addUpdateListener(
        new OnAnimationUpdateListener() {
          @Override
          public void onAnimationUpdate(DynamicAnimation animation,
              float value, float velocity) {
            setIndicatorFraction(value / MAX_DRAWABLE_LEVEL);
          }
        });

    setGrowFraction(1f);
  }

  /**
   * Sets the drawable level with a fraction [0,1] of the progress. Note: this function is not used
   * to force updating the level in opposite to the automatic level updates by framework {@link
   * ProgressBar}.
   *
   * @param fraction Progress in fraction, in [0, 1].
   */
  void setLevelByFraction(float fraction) {
    setLevel((int) (MAX_DRAWABLE_LEVEL * fraction));
  }

  // ******************* Overridden methods *******************

  /** Skips the animation of changing indicator length, directly displays the target progress. */
  @Override
  public void jumpToCurrentState() {
    // Set spring target value to the current level (may not be same as the desired level, depends
    // on the completion of progress animator) and end the animator immediately.
    springAnimator.cancel();
    setIndicatorFraction((float) getLevel() / MAX_DRAWABLE_LEVEL);
  }

  /**
   * When ProgressBar updates progress, it changes the level of drawable's level and calls this
   * method afterward. It sets the new progress value to animator and starts the animator.
   *
   * @param level New progress level.
   */
  @Override
  protected boolean onLevelChange(int level) {
    // Jumps to the level directly if tests request to disable animations, otherwise, updates and
    // starts the spring animation.
    if (!animatorsDisabledForTesting) {
      springAnimator.setStartValue(getIndicatorFraction() * MAX_DRAWABLE_LEVEL);
      springAnimator.animateToFinalPosition(level);
    } else {
      jumpToCurrentState();
    }
    return true;
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
        progressIndicator.getTrackColor(),
        0f,
        1f,
        displayedIndicatorWidth,
        displayedRoundedCornerRadius);
    // Draws the indicator.
    drawingDelegate.fillTrackWithColor(
        canvas,
        paint,
        combinedIndicatorColorArray[0],
        0f,
        getIndicatorFraction(),
        displayedIndicatorWidth,
        displayedRoundedCornerRadius);
    canvas.restore();
  }

  // ******************* Getters and setters *******************

  private float getIndicatorFraction() {
    return indicatorFraction;
  }

  private void setIndicatorFraction(float indicatorFraction) {
    this.indicatorFraction = indicatorFraction;
    invalidateSelf();
  }

  // ******************* Properties *******************

  private static final FloatPropertyCompat<DeterminateDrawable> INDICATOR_LENGTH_FRACTION =
      new FloatPropertyCompat<DeterminateDrawable>("indicatorFraction") {
        @Override
        public float getValue(DeterminateDrawable drawable) {
          return drawable.getIndicatorFraction();
        }

        @Override
        public void setValue(DeterminateDrawable drawable, float value) {
          drawable.setIndicatorFraction(value);
        }
      };
}
