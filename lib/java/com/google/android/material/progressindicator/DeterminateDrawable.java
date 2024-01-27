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
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.progressindicator.DrawingDelegate.ActiveIndicator;

/** This class draws the graphics for determinate mode. */
public final class DeterminateDrawable<S extends BaseProgressIndicatorSpec>
    extends DrawableWithAnimatedVisibilityChange {
  // Constants for drawing progress.
  private static final int MAX_DRAWABLE_LEVEL = 10000;
  // The constant for spring force stiffness.
  private static final float SPRING_FORCE_STIFFNESS = SpringForce.STIFFNESS_VERY_LOW;
  // If the progress is less than 1%, the gap will be proportional to the progress. So that, it
  // draws a full track at 0%.
  static final float GAP_RAMP_DOWN_THRESHOLD = 0.01f;

  // Drawing delegate object.
  private DrawingDelegate<S> drawingDelegate;

  // Animation.
  private final SpringForce springForce;
  private final SpringAnimation springAnimation;
  // Active indicator for the progress.
  private final ActiveIndicator activeIndicator;
  // Whether to skip the spring animation on level change event.
  private boolean skipAnimationOnLevelChange = false;

  DeterminateDrawable(
      @NonNull Context context,
      @NonNull BaseProgressIndicatorSpec baseSpec,
      @NonNull DrawingDelegate<S> drawingDelegate) {
    super(context, baseSpec);

    setDrawingDelegate(drawingDelegate);
    activeIndicator = new ActiveIndicator();

    springForce = new SpringForce();

    springForce.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
    springForce.setStiffness(SPRING_FORCE_STIFFNESS);

    springAnimation = new SpringAnimation(this, INDICATOR_LENGTH_IN_LEVEL);
    springAnimation.setSpring(springForce);

    setGrowFraction(1f);
  }

  /**
   * Creates an instance of {@link DeterminateDrawable} for {@link LinearProgressIndicator} with
   * {@link LinearProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the linear indicator.
   */
  @NonNull
  public static DeterminateDrawable<LinearProgressIndicatorSpec> createLinearDrawable(
      @NonNull Context context, @NonNull LinearProgressIndicatorSpec spec) {
    return createLinearDrawable(context, spec, new LinearDrawingDelegate(spec));
  }

  /**
   * Creates an instance of {@link DeterminateDrawable} for {@link LinearProgressIndicator} with
   * {@link LinearProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the linear indicator.
   * @param drawingDelegate The LinearDrawingDelegate object.
   */
  @NonNull
  static DeterminateDrawable<LinearProgressIndicatorSpec> createLinearDrawable(
      @NonNull Context context,
      @NonNull LinearProgressIndicatorSpec spec,
      @NonNull LinearDrawingDelegate drawingDelegate) {
    return new DeterminateDrawable<>(context, /* baseSpec= */ spec, drawingDelegate);
  }

  /**
   * Creates an instance of {@link DeterminateDrawable} for {@link CircularProgressIndicator} with
   * {@link CircularProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the circular indicator.
   */
  @NonNull
  public static DeterminateDrawable<CircularProgressIndicatorSpec> createCircularDrawable(
      @NonNull Context context, @NonNull CircularProgressIndicatorSpec spec) {
    return createCircularDrawable(context, spec, new CircularDrawingDelegate(spec));
  }

  /**
   * Creates an instance of {@link DeterminateDrawable} for {@link CircularProgressIndicator} with
   * {@link CircularProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the circular indicator.
   * @param drawingDelegate The CircularDrawingDelegate object.
   */
  @NonNull
  static DeterminateDrawable<CircularProgressIndicatorSpec> createCircularDrawable(
      @NonNull Context context,
      @NonNull CircularProgressIndicatorSpec spec,
      @NonNull CircularDrawingDelegate drawingDelegate) {
    return new DeterminateDrawable<>(context, /* baseSpec= */ spec, drawingDelegate);
  }

  public void addSpringAnimationEndListener(
      @NonNull DynamicAnimation.OnAnimationEndListener listener) {
    springAnimation.addEndListener(listener);
  }

  public void removeSpringAnimationEndListener(
      @NonNull DynamicAnimation.OnAnimationEndListener listener) {
    springAnimation.removeEndListener(listener);
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
   * @param animate Whether to change the visibility with animation. The spring animation for
   *     changing progress only depends on system animator duration scale. Use {@link
   *     BaseProgressIndicator#setProgressCompat(int, boolean)} to change the progress without
   *     animation.
   * @return {@code true}, if the visibility changes or will change after the animation; {@code
   *     false}, otherwise.
   */
  @Override
  boolean setVisibleInternal(boolean visible, boolean restart, boolean animate) {
    boolean changed = super.setVisibleInternal(visible, restart, animate);

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
    // on the completion of progress animation) and end the animation immediately.
    springAnimation.skipToEnd();
    setIndicatorFraction((float) getLevel() / MAX_DRAWABLE_LEVEL);
  }

  /**
   * When progress is updated, it changes the level of drawable's level and calls this method
   * afterward. It sets the new progress value to animation and starts the animation.
   *
   * @param level New progress level.
   */
  @Override
  protected boolean onLevelChange(int level) {
    if (skipAnimationOnLevelChange) {
      springAnimation.skipToEnd();
      setIndicatorFraction((float) level / MAX_DRAWABLE_LEVEL);
    } else {
      springAnimation.setStartValue(getIndicatorFraction() * MAX_DRAWABLE_LEVEL);
      springAnimation.animateToFinalPosition(level);
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

  // ******************* Drawing methods *******************

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect clipBounds = new Rect();

    if (getBounds().isEmpty() || !isVisible() || !canvas.getClipBounds(clipBounds)) {
      // Escape if bounds are empty, clip bounds are empty, or currently hidden.
      return;
    }

    canvas.save();
    drawingDelegate.validateSpecAndAdjustCanvas(
        canvas, getBounds(), getGrowFraction(), isShowing(), isHiding());

    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);

    activeIndicator.color = baseSpec.indicatorColors[0];
    if (baseSpec.indicatorTrackGapSize > 0) {
      // Recalculates the gap size, so that it's proportional to the progress when the progress is
      // below the threshold. For the linear type, this calculation is handled in the
      // LinearDrawingDelegate.
      int gapSize =
          drawingDelegate instanceof LinearDrawingDelegate
              ? baseSpec.indicatorTrackGapSize
              : (int)
                  (baseSpec.indicatorTrackGapSize
                      * MathUtils.clamp(getIndicatorFraction(), 0f, GAP_RAMP_DOWN_THRESHOLD)
                      / GAP_RAMP_DOWN_THRESHOLD);
      drawingDelegate.fillTrack(
          canvas,
          paint,
          getIndicatorFraction(),
          /* endFraction= */ 1f,
          baseSpec.trackColor,
          getAlpha(),
          gapSize);
    } else {
      drawingDelegate.fillTrack(
          canvas,
          paint,
          /* startFraction= */ 0f,
          /* endFraction= */ 1f,
          baseSpec.trackColor,
          getAlpha(),
          /* gapSize= */ 0);
    }
    drawingDelegate.fillIndicator(canvas, paint, activeIndicator, getAlpha());
    drawingDelegate.drawStopIndicator(canvas, paint, baseSpec.indicatorColors[0], getAlpha());
    canvas.restore();
  }

  // ******************* Getters and setters *******************

  private float getIndicatorFraction() {
    return activeIndicator.endFraction;
  }

  private void setIndicatorFraction(float indicatorFraction) {
    activeIndicator.endFraction = indicatorFraction;
    invalidateSelf();
  }

  @NonNull
  DrawingDelegate<S> getDrawingDelegate() {
    return drawingDelegate;
  }

  void setDrawingDelegate(@NonNull DrawingDelegate<S> drawingDelegate) {
    this.drawingDelegate = drawingDelegate;
  }

  // ******************* Properties *******************

  private static final FloatPropertyCompat<DeterminateDrawable<?>> INDICATOR_LENGTH_IN_LEVEL =
      new FloatPropertyCompat<DeterminateDrawable<?>>("indicatorLevel") {
        @Override
        public float getValue(DeterminateDrawable<?> drawable) {
          return drawable.getIndicatorFraction() * MAX_DRAWABLE_LEVEL;
        }

        @Override
        public void setValue(DeterminateDrawable<?> drawable, float value) {
          drawable.setIndicatorFraction(value / MAX_DRAWABLE_LEVEL);
        }
      };
}
