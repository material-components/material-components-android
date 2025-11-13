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

import static java.lang.Math.min;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.progressindicator.DrawingDelegate.ActiveIndicator;

/** This class draws the graphics for determinate mode. */
public final class DeterminateDrawable<S extends BaseProgressIndicatorSpec>
    extends DrawableWithAnimatedVisibilityChange {
  // Constants for drawing progress.
  static final int MAX_DRAWABLE_LEVEL = 10000;
  // Constants for amplitude animation.
  static final float FULL_AMPLITUDE_PROGRESS_MIN = 0.1f;
  static final float FULL_AMPLITUDE_PROGRESS_MAX = 0.9f;

  // The constant for spring force stiffness.
  private static final float SPRING_FORCE_STIFFNESS = SpringForce.STIFFNESS_VERY_LOW;
  // If the progress is less than 1%, the gap will be proportional to the progress. So that, it
  // draws a full track at 0%.
  static final float GAP_RAMP_DOWN_THRESHOLD = 0.01f;
  // The duration of repeated initial phase animation in ms. It can be any positive values.
  private static final int PHASE_ANIMATION_DURATION_MS = 1000;
  // The duration of amplitude ramping animation in ms.
  private static final int AMPLITUDE_ANIMATION_DURATION_MS = 500;

  // Drawing delegate object.
  private DrawingDelegate<S> drawingDelegate;

  // Animation.
  private final SpringAnimation springAnimation;
  // Active indicator for the progress.
  private final ActiveIndicator activeIndicator;
  // Fraction of displayed amplitude.
  private float targetAmplitudeFraction;
  // Whether to skip the spring animation on level change event.
  private boolean skipAnimationOnLevelChange = false;

  @NonNull private final ValueAnimator phaseAnimator;
  @NonNull private ValueAnimator amplitudeAnimator;
  private TimeInterpolator amplitudeInterpolator;
  @NonNull private TimeInterpolator amplitudeOnInterpolator;
  @NonNull private TimeInterpolator amplitudeOffInterpolator;

  DeterminateDrawable(
      @NonNull Context context,
      @NonNull BaseProgressIndicatorSpec baseSpec,
      @NonNull DrawingDelegate<S> drawingDelegate) {
    super(context, baseSpec);

    setDrawingDelegate(drawingDelegate);
    activeIndicator = new ActiveIndicator();
    activeIndicator.isDeterminate = true;

    // Initializes a spring animator for progress animation.
    springAnimation = new SpringAnimation(this, INDICATOR_LENGTH_IN_LEVEL);
    springAnimation.setSpring(
        new SpringForce()
            .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
            .setStiffness(SPRING_FORCE_STIFFNESS));

    // Initializes a linear animator to enforce phase animation when progress is unchanged.
    phaseAnimator = new ValueAnimator();
    phaseAnimator.setDuration(PHASE_ANIMATION_DURATION_MS);
    phaseAnimator.setFloatValues(0, 1);
    phaseAnimator.setRepeatCount(ValueAnimator.INFINITE);
    phaseAnimator.addUpdateListener(
        animation -> {
          if (baseSpec.hasWavyEffect(/* isDeterminate= */ true)
              && baseSpec.waveSpeed != 0
              && isVisible()) {
            invalidateSelf();
          }
        });
    if (baseSpec.hasWavyEffect(/* isDeterminate= */ true) && baseSpec.waveSpeed != 0) {
      phaseAnimator.start();
    }

    setGrowFraction(1f);
  }

  private void maybeInitializeAmplitudeAnimator() {
    if (amplitudeAnimator != null) {
      return;
    }
    // Initializes a linear animator to turn on/off wave amplitude.
    amplitudeOnInterpolator =
        MotionUtils.resolveThemeInterpolator(
            context, R.attr.motionEasingStandardInterpolator, AnimationUtils.LINEAR_INTERPOLATOR);
    amplitudeOffInterpolator =
        MotionUtils.resolveThemeInterpolator(
            context,
            R.attr.motionEasingEmphasizedAccelerateInterpolator,
            AnimationUtils.LINEAR_INTERPOLATOR);
    amplitudeAnimator = new ValueAnimator();
    amplitudeAnimator.setDuration(AMPLITUDE_ANIMATION_DURATION_MS);
    amplitudeAnimator.setFloatValues(0, 1);
    amplitudeAnimator.setInterpolator(null);
    amplitudeAnimator.addUpdateListener(
        animation -> {
          activeIndicator.amplitudeFraction =
              amplitudeInterpolator.getInterpolation(amplitudeAnimator.getAnimatedFraction());
        });
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
      springAnimation
          .getSpring()
          .setStiffness(SPRING_FORCE_STIFFNESS / systemAnimatorDurationScale);
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
    float nextAmplitudeFraction = getAmplitudeFractionFromLevel(level);
    if (skipAnimationOnLevelChange) {
      springAnimation.skipToEnd();
      setIndicatorFraction((float) level / MAX_DRAWABLE_LEVEL);
      setAmplitudeFraction(nextAmplitudeFraction);
    } else {
      // Update min visible change to the recommended value.
      updateSpringMinVisibleChange();
      springAnimation.setStartValue(getIndicatorFraction() * MAX_DRAWABLE_LEVEL);
      springAnimation.animateToFinalPosition(level);
    }
    return true;
  }

  /**
   * Updates the minimum visible change of the spring animation controlling the indicator length
   * (progress) to the recommended value based on the track's length.
   */
  private void updateSpringMinVisibleChange() {
    int width = getBounds().width();
    int height = getBounds().height();
    if (width <= 0 || height <= 0) {
      return;
    }
    if (drawingDelegate instanceof LinearDrawingDelegate) {
      // Track length is the width of the drawable.
      springAnimation.setMinimumVisibleChange((float) MAX_DRAWABLE_LEVEL / width);
    } else {
      // Track length is the perimeter of the circle fit in the drawable.
      springAnimation.setMinimumVisibleChange(
          (float) (MAX_DRAWABLE_LEVEL / (min(height, width) * Math.PI)));
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return drawingDelegate.getPreferredWidth();
  }

  @Override
  public int getIntrinsicHeight() {
    return drawingDelegate.getPreferredHeight();
  }

  /** Returns the spring force of the spring animation for the progress. */
  @NonNull
  public SpringForce getSpringForce() {
    return springAnimation.getSpring();
  }

  /** Sets the spring force of the spring animation for the progress. */
  public void setSpringForce(@NonNull SpringForce spring) {
    springAnimation.setSpring(spring);
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

  private float getAmplitudeFractionFromLevel(int level) {
    return level >= baseSpec.waveAmplitudeRampProgressMin * MAX_DRAWABLE_LEVEL
            && level <= baseSpec.waveAmplitudeRampProgressMax * MAX_DRAWABLE_LEVEL
        ? 1f
        : 0f;
  }

  private void maybeStartAmplitudeAnimator(int level) {
    if (!baseSpec.hasWavyEffect(/* isDeterminate= */ true)) {
      return;
    }
    maybeInitializeAmplitudeAnimator();
    float newAmplitudeFraction = getAmplitudeFractionFromLevel(level);
    if (newAmplitudeFraction != targetAmplitudeFraction) {
      if (amplitudeAnimator.isRunning()) {
        amplitudeAnimator.cancel();
      }
      targetAmplitudeFraction = newAmplitudeFraction;
      if (targetAmplitudeFraction == 1f) {
        amplitudeInterpolator = amplitudeOnInterpolator;
        amplitudeAnimator.start();
      } else {
        amplitudeInterpolator = amplitudeOffInterpolator;
        amplitudeAnimator.reverse();
      }
    } else if (!amplitudeAnimator.isRunning()) {
      setAmplitudeFraction(newAmplitudeFraction);
    }
  }

  // ******************* Drawing methods *******************

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (getBounds().isEmpty() || !isVisible() || !canvas.getClipBounds(clipBounds)) {
      // Escape if bounds are empty, clip bounds are empty, or currently hidden.
      return;
    }

    canvas.save();
    drawingDelegate.validateSpecAndAdjustCanvas(
        canvas, getBounds(), getGrowFraction(), isShowing(), isHiding());

    activeIndicator.phaseFraction = getPhaseFraction();

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

  private void setAmplitudeFraction(float amplitudeFraction) {
    this.activeIndicator.amplitudeFraction = amplitudeFraction;
    invalidateSelf();
  }

  @NonNull
  DrawingDelegate<S> getDrawingDelegate() {
    return drawingDelegate;
  }

  void setDrawingDelegate(@NonNull DrawingDelegate<S> drawingDelegate) {
    this.drawingDelegate = drawingDelegate;
  }

  void setEnforcedDrawing(boolean enforced) {
    if (enforced && !phaseAnimator.isRunning()) {
      phaseAnimator.start();
    } else if (!enforced && phaseAnimator.isRunning()) {
      phaseAnimator.cancel();
    }
  }

  void setWaveAmplitudeRampProgressMin(float progress) {
    baseSpec.waveAmplitudeRampProgressMin = progress;
    invalidateSelf();
  }

  void setWaveAmplitudeRampProgressMax(float progress) {
    baseSpec.waveAmplitudeRampProgressMax = progress;
    invalidateSelf();
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
          drawable.maybeStartAmplitudeAnimator((int) value);
        }
      };
}
