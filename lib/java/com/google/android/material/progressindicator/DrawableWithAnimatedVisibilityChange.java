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
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class for drawables, which can be shown or hidden with animations. It will be
 * extended by drawable implementations for different progress indicator types.
 */
abstract class DrawableWithAnimatedVisibilityChange extends Drawable implements Animatable2Compat {

  // Argument restart used in Drawable setVisible() doesn't matter in implementation.
  private static final boolean DEFAULT_DRAWABLE_RESTART = false;

  // Animation duration for both show and hide animators.
  private static final long GROW_DURATION = (long) (500 * ProgressIndicator.ANIMATION_SPEED_FACTOR);

  // The component this drawable is serving.
  final ProgressIndicator progressIndicator;

  // ValueAnimator used for show animation.
  private ValueAnimator showAnimator;
  // ValueAnimator used for hide animation.
  private ValueAnimator hideAnimator;

  // List of AnimationCallback to be called at the end of show/hide animation.
  private List<AnimationCallback> animationCallbacks;

  // A fraction from 0 to 1 indicating the ratio used in drawing, controlled by show/hide animator.
  private float growFraction;

  // Colors multiplied with the totalAlpha.
  int combinedTrackColor;
  int[] combinedIndicatorColorArray;

  final Paint paint = new Paint();
  private int totalAlpha;

  // Whether to disable all animators for screenshot tests.
  boolean animatorsDisabledForTesting;

  // ******************* Constructor *******************

  DrawableWithAnimatedVisibilityChange(@NonNull ProgressIndicator progressIndicator) {
    this.progressIndicator = progressIndicator;

    setAlpha(255);

    initializeShowAnimator();
    initializeHideAnimator();
  }

  // ******************* Animator initialization *******************

  private void initializeShowAnimator() {
    showAnimator = ObjectAnimator.ofFloat(this, GROW_FRACTION, 0f, 1f);
    showAnimator.setDuration(GROW_DURATION);
    showAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    setShowAnimator(showAnimator);
  }

  private void initializeHideAnimator() {
    hideAnimator = ObjectAnimator.ofFloat(this, GROW_FRACTION, 1f, 0f);
    hideAnimator.setDuration(GROW_DURATION);
    hideAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    setHideAnimator(hideAnimator);
  }

  // ******************* Callbacks *******************

  /**
   * Registers a new {@code AnimationCallback} to this drawable. The {@code onAnimationStart()} will
   * be called at the start of show animation, or at the moment this drawable is shown, if there's
   * no show animation is used. The {@code onAnimationEnd()} will be called at the end of the hide
   * animation, or at the moment this drawable is hidden, if there's no hide animation is used.
   *
   * @param callback A new {@code AnimationCallback} to register.
   * @see AnimationCallback#onAnimationStart(Drawable)
   * @see AnimationCallback#onAnimationEnd(Drawable)
   */
  @Override
  public void registerAnimationCallback(@NonNull AnimationCallback callback) {
    if (animationCallbacks == null) {
      animationCallbacks = new ArrayList<>();
    }
    if (!animationCallbacks.contains(callback)) {
      animationCallbacks.add(callback);
    }
  }

  /**
   * Unregisters an {@code AnimationCallback} from this drawable.
   *
   * @param callback {@code AnimationCallback} to unregister.
   * @return {@code true}, if callback is successfully registered; {@code false}, otherwise.
   */
  @Override
  public boolean unregisterAnimationCallback(@NonNull AnimationCallback callback) {
    if (animationCallbacks != null && animationCallbacks.contains(callback)) {
      animationCallbacks.remove(callback);
      if (animationCallbacks.isEmpty()) {
        animationCallbacks = null;
      }
      return true;
    }
    return false;
  }

  /** Unregisters all {@code AnimationCallback} from this drawable. */
  @Override
  public void clearAnimationCallbacks() {
    animationCallbacks.clear();
    animationCallbacks = null;
  }

  /** Invokes all {@code onAnimationStart()} functions in the animation callbacks. */
  private void dispatchAnimationStart() {
    if (animationCallbacks != null) {
      for (AnimationCallback callback : animationCallbacks) {
        callback.onAnimationStart(this);
      }
    }
  }

  /** Invokes all {@code onAnimationEnd()} functions in the animation callbacks. */
  private void dispatchAnimationEnd() {
    if (animationCallbacks != null) {
      for (AnimationCallback callback : animationCallbacks) {
        callback.onAnimationEnd(this);
      }
    }
  }

  // ******************* Visibility control *******************

  @VisibleForTesting
  void disableAnimatorsForTesting() {
    animatorsDisabledForTesting = true;
  }

  /** The drawable will start with show animator as default. */
  @Override
  public void start() {
    setVisible(true, true);
  }

  /** The drawable will stop with hide animator as default. */
  @Override
  public void stop() {
    setVisible(false, true);
  }

  @Override
  public boolean isRunning() {
    return (showAnimator != null && showAnimator.isRunning())
        || (hideAnimator != null && hideAnimator.isRunning());
  }

  /**
   * Show or hide the drawable with/without animation effects.
   *
   * @param visible Whether to make the drawable visible.
   * @param animationDesired Whether to change the visibility with animation.
   * @return {@code true}, if the visibility changes or will change after the animation; {@code
   *     false}, otherwise.
   */
  @Override
  public boolean setVisible(boolean visible, boolean animationDesired) {
    // If the drawable is visible and not being hidden, prevents to start the show animation.
    if (visible && animationDesired && isVisible() && !hideAnimator.isRunning()) {
      return false;
    }
    // If the drawable is invisible, prevents to start the hide animation.
    if (!visible && animationDesired && !isVisible()) {
      return false;
    }

    boolean changed =
        (!visible && animationDesired) || super.setVisible(visible, DEFAULT_DRAWABLE_RESTART);
    boolean shouldAnimate =
        animationDesired && progressIndicator.getGrowMode() != ProgressIndicator.GROW_MODE_NONE;

    // We don't want to change visibility while show/hide animation is running. This also prevents
    // multiple invokes to cancel the grow animators for some Android versions.
    if ((showAnimator.isRunning() && visible) || hideAnimator.isRunning()) {
      return false;
    }

    // Cancels any running animations.
    showAnimator.cancel();
    hideAnimator.cancel();

    if (visible) {
      if (shouldAnimate) {
        // Resets properties as it's fully hidden at the beginning of show animation.
        resetToShow();
        showAnimator.start();
        return true;
      } else {
        // Resets properties as it's fully shown at the beginning of hide animation.
        resetToHide();
      }
    } else {
      if (shouldAnimate) {
        // Resets properties as it's fully shown at the beginning of hide animation.
        resetToHide();
        hideAnimator.start();
        return true;
      } else {
        // Resets properties as it's fully hidden at the beginning of show animation.
        resetToShow();
      }
    }

    return changed;
  }

  private void resetToShow() {
    growFraction = 0f;
  }

  private void resetToHide() {
    growFraction = 1f;
  }

  // ******************* Helper methods *******************

  void recalculateColors() {
    combinedTrackColor =
        MaterialColors.compositeARGBWithAlpha(progressIndicator.getTrackColor(), getAlpha());
    combinedIndicatorColorArray = progressIndicator.getIndicatorColors().clone();
    for (int i = 0; i < combinedIndicatorColorArray.length; i++) {
      combinedIndicatorColorArray[i] =
          MaterialColors.compositeARGBWithAlpha(combinedIndicatorColorArray[i], getAlpha());
    }
  }

  // ******************* Getters and setters *******************

  @Override
  public void setAlpha(int alpha) {
    totalAlpha = alpha;
    recalculateColors();
    invalidateSelf();
  }

  @Override
  public int getAlpha() {
    return totalAlpha;
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  /**
   * Sets a value animator for show animation. This only takes effect when the drawable is not
   * currently being shown.
   *
   * @param showAnimator A ValueAnimator used for show animation.
   */
  private void setShowAnimator(@NonNull ValueAnimator showAnimator) {
    if (this.showAnimator != null && this.showAnimator.isRunning()) {
      throw new IllegalArgumentException(
          "Cannot set showAnimator while the current showAnimator is running.");
    }
    this.showAnimator = showAnimator;

    // Adds a listener to dispatch animation callbacks at the end.
    showAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);

            dispatchAnimationStart();
          }
        });
  }

  @NonNull
  ValueAnimator getHideAnimator() {
    return hideAnimator;
  }

  /**
   * Sets a value animator for hide animation. This only takes effect when the drawable is not
   * currently being hidden.
   *
   * @param hideAnimator A ValueAnimator used for hide animation.
   */
  private void setHideAnimator(@NonNull ValueAnimator hideAnimator) {
    if (this.hideAnimator != null && this.hideAnimator.isRunning()) {
      throw new IllegalArgumentException(
          "Cannot set hideAnimator while the current hideAnimator is running.");
    }

    this.hideAnimator = hideAnimator;

    // Adds a listener to set visibility false and dispatch animation callbacks at the end.
    hideAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            // Sets visibility to false.
            DrawableWithAnimatedVisibilityChange.super.setVisible(
                false, DEFAULT_DRAWABLE_RESTART);

            dispatchAnimationEnd();
          }
        });
  }

  float getGrowFraction() {
    return growFraction;
  }

  void setGrowFraction(float growFraction) {
    // If no show/hide animation is needed, the growFraction is always 1.
    if (progressIndicator.getGrowMode() == ProgressIndicator.GROW_MODE_NONE) {
      growFraction = 1f;
    }
    if (this.growFraction != growFraction) {
      this.growFraction = growFraction;
      invalidateSelf();
    }
  }

  // ******************* Properties *******************

  private static final Property<DrawableWithAnimatedVisibilityChange, Float> GROW_FRACTION =
      new Property<DrawableWithAnimatedVisibilityChange, Float>(Float.class, "growFraction") {
        @Override
        public Float get(DrawableWithAnimatedVisibilityChange drawable) {
          return drawable.getGrowFraction();
        }

        @Override
        public void set(DrawableWithAnimatedVisibilityChange drawable, Float value) {
          drawable.setGrowFraction(value);
        }
      };
}
