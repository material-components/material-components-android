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
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import com.google.android.material.animation.AnimationUtils;
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
  private static final int GROW_DURATION = 500;

  // The current context this drawable is running in.
  final Context context;
  // The spec of the component being served.
  final ProgressIndicatorSpec spec;
  // Utils class.
  AnimatorDurationScaleProvider animatorDurationScaleProvider;

  // ValueAnimator used for show animation.
  private ValueAnimator showAnimator;
  // ValueAnimator used for hide animation.
  private ValueAnimator hideAnimator;

  // List of AnimationCallback to be called at the end of show/hide animation.
  private List<AnimationCallback> animationCallbacks;
  // An internal AnimationCallback which is executed before the user's animation callbacks.
  private AnimationCallback internalAnimationCallback;

  // A fraction from 0 to 1 indicating the ratio used in drawing, controlled by show/hide animator.
  private float growFraction;

  final Paint paint = new Paint();
  private int totalAlpha;

  // ******************* Constructor *******************

  DrawableWithAnimatedVisibilityChange(
      @NonNull Context context, @NonNull ProgressIndicatorSpec spec) {
    this.context = context;
    this.spec = spec;
    animatorDurationScaleProvider = new AnimatorDurationScaleProvider();

    setAlpha(255);
  }

  // ******************* Animator initialization *******************

  private void maybeInitializeAnimators() {
    if (showAnimator == null) {
      showAnimator = ObjectAnimator.ofFloat(this, GROW_FRACTION, 0f, 1f);
      showAnimator.setDuration(GROW_DURATION);
      showAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      setShowAnimator(showAnimator);
    }
    if (hideAnimator == null) {
      hideAnimator = ObjectAnimator.ofFloat(this, GROW_FRACTION, 1f, 0f);
      hideAnimator.setDuration(GROW_DURATION);
      hideAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      setHideAnimator(hideAnimator);
    }
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

  /**
   * Sets the internal {@code AnimationCallback}, which will be executed before other callbacks.
   *
   * @param callback New internal animation callback.
   */
  public void setInternalAnimationCallback(@NonNull AnimationCallback callback) {
    internalAnimationCallback = callback;
  }

  /** Invokes all {@code onAnimationStart()} functions in the animation callbacks. */
  private void dispatchAnimationStart() {
    if (internalAnimationCallback != null) {
      internalAnimationCallback.onAnimationStart(this);
    }
    if (animationCallbacks != null) {
      for (AnimationCallback callback : animationCallbacks) {
        callback.onAnimationStart(this);
      }
    }
  }

  /** Invokes all {@code onAnimationEnd()} functions in the animation callbacks. */
  private void dispatchAnimationEnd() {
    if (internalAnimationCallback != null) {
      internalAnimationCallback.onAnimationEnd(this);
    }
    if (animationCallbacks != null) {
      for (AnimationCallback callback : animationCallbacks) {
        callback.onAnimationEnd(this);
      }
    }
  }

  // ******************* Visibility control *******************

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

  /** Hides the drawable immediately */
  public boolean hideNow(){
    return setVisible(/*visible=*/ false, /*restart=*/ false, /*animationDesired=*/ false);
  }

  /**
   * Sets the visibility with/without animation based on system animator duration scale.
   *
   * @see #setVisible(boolean, boolean, boolean)
   */
  @Override
  public boolean setVisible(boolean visible, boolean restart) {
    float systemAnimatorDurationScale =
        animatorDurationScaleProvider.getSystemAnimatorDurationScale(context.getContentResolver());
    // Only show/hide the drawable with animations if system animator duration scale is not off and
    // some grow mode is used.
    return setVisible(visible, restart, systemAnimatorDurationScale > 0);
  }

  /**
   * Show or hide the drawable with/without animation effects.
   *
   * @param visible Whether to make the drawable visible.
   * @param restart Whether to force starting the animation from the beginning.
   * @param animationDesired Whether to change the visibility with animation.
   * @return {@code true}, if the visibility changes or will change after the animation; {@code
   *     false}, otherwise.
   */
  public boolean setVisible(boolean visible, boolean restart, boolean animationDesired) {
    if (!isVisible() && !visible) {
      // Early return if trying to hide a hidden drawable.
      return false;
    }

    maybeInitializeAnimators();

    if (animationDesired && (visible ? showAnimator : hideAnimator).isRunning()) {
      // Show/hide animation should not be reset while being played.
      return false;
    }

    ValueAnimator animationInAction = visible ? showAnimator : hideAnimator;

    // If requests to show, sets the drawable visible. If requests to hide, the visibility is
    // controlled by the animation listener attached to hide animation.
    boolean changed = !visible || super.setVisible(visible, DEFAULT_DRAWABLE_RESTART);
    animationDesired &= spec.growMode != ProgressIndicator.GROW_MODE_NONE;
    if (!animationDesired) {
      // This triggers onAnimationStart() callbacks for showing and onAnimationEnd() callbacks for
      // hiding. It also fast-forwards the animator properties to the end state.
      animationInAction.end();
      return changed;
    }

    if (restart || VERSION.SDK_INT < 19 || !animationInAction.isPaused()) {
      // Starts/restarts the animator if requested or not eligible to resume.
      animationInAction.start();
    } else {
      animationInAction.resume();
    }
    return changed;
  }

  // ******************* Getters and setters *******************

  @Override
  public void setAlpha(int alpha) {
    totalAlpha = alpha;
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
    if (spec.growMode == ProgressIndicator.GROW_MODE_NONE) {
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
