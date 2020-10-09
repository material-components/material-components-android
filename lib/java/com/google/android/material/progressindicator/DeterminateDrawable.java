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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.color.MaterialColors;

/**
 * This class draws the graphics for determinate modes by applying different {@link DrawingDelegate}
 * for different indicator types.
 */
public final class DeterminateDrawable extends DrawableWithAnimatedVisibilityChange {

  // Constants for drawing progress.
  private static final int MAX_DRAWABLE_LEVEL = 10000;
  // The constant for spring force stiffness.
  private static final float SPRING_FORCE_STIFFNESS = SpringForce.STIFFNESS_VERY_LOW;
  // Current progress indicator spec.
  private final ProgressIndicatorSpec spec;
  // Drawing delegate object.
  private final DrawingDelegate drawingDelegate;
  // Animation.
  private final SpringForce springForce;
  private final SpringAnimation springAnimator;
  // Fraction of displayed indicator in the total width.
  private float indicatorFraction;
  // Whether to skip the spring animation on level change event.
  private boolean skipAnimationOnLevelChange = false;

  public DeterminateDrawable(
      @NonNull Context context,
      @NonNull ProgressIndicatorSpec spec,
      @NonNull DrawingDelegate drawingDelegate) {
    super(context, /*animatedVisibilityChangeBehavior=*/ spec);

    this.spec = spec;
    this.drawingDelegate = drawingDelegate;
    drawingDelegate.registerDrawable(this);

    springForce = new SpringForce();

    springForce.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
    springForce.setStiffness(SPRING_FORCE_STIFFNESS);

    springAnimator = new SpringAnimation(this, INDICATOR_LENGTH_IN_LEVEL);
    springAnimator.setSpring(springForce);

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

  /**
   * Sets the visibility of this drawable. It calls the {@link
   * DrawableWithAnimatedVisibilityChange#setVisible(boolean, boolean)} to start the show/hide
   * animation properly. The spring animation will be skipped when the level changes, if animation
   * is not requested.
   *
   * @param visible Whether to make the drawable visible.
   * @param restart Whether to force starting the animation from the beginning. Doesn't apply to the
   *     spring animation for changing progress.
   * @param animationDesired Whether to change the visibility with animation. The spring animation
   *     for changing progress only depends on system animator duration scale. Use {@link
   *     ProgressIndicator#setProgress(int, boolean)} to change the progress without animation.
   * @return {@code true}, if the visibility changes or will change after the animation; {@code
   *     false}, otherwise.
   */
  @Override
  public boolean setVisible(boolean visible, boolean restart, boolean animationDesired) {
    boolean changed = super.setVisible(visible, restart, animationDesired);

    float systemAnimatorDurationScale =
        animatorDurationScaleProvider.getSystemAnimatorDurationScale(context.getContentResolver());
    if (systemAnimatorDurationScale == 0) {
      skipAnimationOnLevelChange = true;
    } else {
      skipAnimationOnLevelChange = false;
      springForce.setStiffness(SPRING_FORCE_STIFFNESS / systemAnimatorDurationScale);
    }

    return changed;
  }

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
    if (skipAnimationOnLevelChange) {
      springAnimator.cancel();
      setIndicatorFraction((float) level / MAX_DRAWABLE_LEVEL);
    } else {
      springAnimator.setStartValue(getIndicatorFraction() * MAX_DRAWABLE_LEVEL);
      springAnimator.animateToFinalPosition(level);
    }
    return true;
  }

  @Override
  public int getIntrinsicWidth() {
    return drawingDelegate.getPreferredWidth();
  }

  @Override
  public int getIntrinsicHeight() {
    return drawingDelegate.getPreferredHeight();
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
    drawingDelegate.adjustCanvas(canvas, getGrowFraction());

    // Draws the track.
    drawingDelegate.fillTrack(canvas, paint);
    // Draws the indicator.
    int indicatorColor = MaterialColors.compositeARGBWithAlpha(spec.indicatorColors[0], getAlpha());
    drawingDelegate.fillIndicator(canvas, paint, 0f, getIndicatorFraction(), indicatorColor);
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

  public DrawingDelegate getDrawingDelegate() {
    return drawingDelegate;
  }

  // ******************* Properties *******************

  private static final FloatPropertyCompat<DeterminateDrawable> INDICATOR_LENGTH_IN_LEVEL =
      new FloatPropertyCompat<DeterminateDrawable>("indicatorLevel") {
        @Override
        public float getValue(DeterminateDrawable drawable) {
          return drawable.getIndicatorFraction() * MAX_DRAWABLE_LEVEL;
        }

        @Override
        public void setValue(DeterminateDrawable drawable, float value) {
          drawable.setIndicatorFraction(value / MAX_DRAWABLE_LEVEL);
        }
      };
}
