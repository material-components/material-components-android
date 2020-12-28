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

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;

/** This class draws the graphics for indeterminate mode. */
public final class IndeterminateDrawable<S extends BaseProgressIndicatorSpec>
    extends DrawableWithAnimatedVisibilityChange {

  // Drawing delegate object.
  private DrawingDelegate<S> drawingDelegate;
  // Animator delegate object.
  private IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate;

  IndeterminateDrawable(
      @NonNull Context context,
      @NonNull BaseProgressIndicatorSpec baseSpec,
      @NonNull DrawingDelegate<S> drawingDelegate,
      @NonNull IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate) {
    super(context, baseSpec);

    setDrawingDelegate(drawingDelegate);
    setAnimatorDelegate(animatorDelegate);
  }

  /**
   * Creates an instance of {@link IndeterminateDrawable} for {@link LinearProgressIndicator} with
   * {@link LinearProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the linear indicator.
   */
  @NonNull
  public static IndeterminateDrawable<LinearProgressIndicatorSpec> createLinearDrawable(
      @NonNull Context context, @NonNull LinearProgressIndicatorSpec spec) {
    return new IndeterminateDrawable<>(
        context,
        /*baseSpec=*/ spec,
        new LinearDrawingDelegate(spec),
        spec.indeterminateAnimationType
                == LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS
            ? new LinearIndeterminateContiguousAnimatorDelegate(spec)
            : new LinearIndeterminateDisjointAnimatorDelegate(context, spec));
  }

  /**
   * Creates an instance of {@link IndeterminateDrawable} for {@link CircularProgressIndicator} with
   * {@link CircularProgressIndicatorSpec}.
   *
   * @param context The current context.
   * @param spec The spec for the circular indicator.
   */
  @NonNull
  public static IndeterminateDrawable<CircularProgressIndicatorSpec> createCircularDrawable(
      @NonNull Context context, @NonNull CircularProgressIndicatorSpec spec) {
    return new IndeterminateDrawable<>(
        context,
        /*baseSpec=*/ spec,
        new CircularDrawingDelegate(spec),
        new CircularIndeterminateAnimatorDelegate(spec));
  }

  // ******************* Overridden methods *******************

  /**
   * Sets the visibility of this drawable. It calls the {@link
   * DrawableWithAnimatedVisibilityChange#setVisible(boolean, boolean, boolean)} to start the
   * show/hide animation properly. The indeterminate animation will be started if animation is
   * requested.
   *
   * @param visible Whether to make the drawable visible.
   * @param restart Whether to force starting the animation from the beginning.
   * @param animate Whether to change the visibility with animation.
   * @return {@code true}, if the visibility changes or will change after the animation; {@code
   *     false}, otherwise.
   */
  @Override
  boolean setVisibleInternal(boolean visible, boolean restart, boolean animate) {
    boolean changed = super.setVisibleInternal(visible, restart, animate);

    // Unless it's showing or hiding, cancels and resets main animator.
    if (!isRunning()) {
      animatorDelegate.cancelAnimatorImmediately();
      animatorDelegate.resetPropertiesForNewStart();
    }
    // Restarts the main animator if it's visible and needs to be animated.
    float systemAnimatorDurationScale =
        animatorDurationScaleProvider.getSystemAnimatorDurationScale(context.getContentResolver());
    if (visible
        && (animate
            || (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP && systemAnimatorDurationScale > 0))) {
      animatorDelegate.startAnimator();
    }

    return changed;
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

  /** Draws the graphics based on the progress indicator's properties and the animation states. */
  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect clipBounds = new Rect();

    if (getBounds().isEmpty() || !isVisible() || !canvas.getClipBounds(clipBounds)) {
      // Escape if bounds are empty, clip bounds are empty, or currently hidden.
      return;
    }

    canvas.save();
    drawingDelegate.validateSpecAndAdjustCanvas(canvas, getGrowFraction());

    // Draws the track.
    drawingDelegate.fillTrack(canvas, paint);
    // Draws the indicators.
    for (int segmentIndex = 0;
        segmentIndex < animatorDelegate.segmentColors.length;
        segmentIndex++) {
      drawingDelegate.fillIndicator(
          canvas,
          paint,
          animatorDelegate.segmentPositions[2 * segmentIndex],
          animatorDelegate.segmentPositions[2 * segmentIndex + 1],
          animatorDelegate.segmentColors[segmentIndex]);
    }
    canvas.restore();
  }

  // ******************* Setter and getter *******************

  @NonNull
  IndeterminateAnimatorDelegate<AnimatorSet> getAnimatorDelegate() {
    return animatorDelegate;
  }

  void setAnimatorDelegate(@NonNull IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate) {
    this.animatorDelegate = animatorDelegate;
    animatorDelegate.registerDrawable(this);

    // Sets the end action of the internal callback to cancel indeterminate animator after hidden.
    setInternalAnimationCallback(
        new AnimationCallback() {
          @Override
          public void onAnimationEnd(Drawable drawable) {
            super.onAnimationEnd(drawable);
            IndeterminateDrawable.this.animatorDelegate.cancelAnimatorImmediately();
            IndeterminateDrawable.this.animatorDelegate.resetPropertiesForNewStart();
          }
        });

    setGrowFraction(1f);
  }

  @NonNull
  DrawingDelegate<S> getDrawingDelegate() {
    return drawingDelegate;
  }

  void setDrawingDelegate(@NonNull DrawingDelegate<S> drawingDelegate) {
    this.drawingDelegate = drawingDelegate;
    drawingDelegate.registerDrawable(this);
  }
}

