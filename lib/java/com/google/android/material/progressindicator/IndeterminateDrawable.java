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
import android.animation.AnimatorSet;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;

/**
 * This class draws the graphics for indeterminate modes by applying different {@link
 * DrawingDelegate} and {@link IndeterminateAnimatorDelegate} for different indicator types.
 */
public final class IndeterminateDrawable extends DrawableWithAnimatedVisibilityChange {

  // Drawing delegate object.
  private final DrawingDelegate drawingDelegate;
  // Animator delegate object.
  private IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate;

  public IndeterminateDrawable(
      @NonNull ProgressIndicator progressIndicator,
      @NonNull DrawingDelegate drawingDelegate,
      @NonNull IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate) {
    super(progressIndicator);

    this.drawingDelegate = drawingDelegate;
    setAnimatorDelegate(animatorDelegate);
  }

  // ******************* Overridden methods *******************

  /**
   * Sets the visibility of this drawable. It calls the {@link
   * DrawableWithAnimatedVisibilityChange#setVisible(boolean, boolean)} to start the show/hide
   * animation properly. If it's requested to be visible, the main animator will be started. If it's
   * required to be invisible, the main animator will be canceled unless the hide animation is
   * required.
   *
   * @param visible Whether to make the drawable visible.
   * @param animationDesired Whether to change the visibility with animation.
   */
  @Override
  public boolean setVisible(boolean visible, boolean animationDesired) {
    boolean changed = super.setVisible(visible, animationDesired);

    // Unless it's showing or hiding, cancels and resets main animator.
    if (!isRunning()) {
      animatorDelegate.cancelAnimatorImmediately();
      animatorDelegate.resetPropertiesForNewStart();
    }
    // Restarts the main animator if it's visible and needs to be animated.
    if (visible && animationDesired) {
      animatorDelegate.startAnimator();
    }

    return changed;
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
    drawingDelegate.adjustCanvas(canvas, progressIndicator, getGrowFraction());

    float displayedIndicatorWidth = progressIndicator.getIndicatorWidth() * getGrowFraction();
    float displayedRoundedCornerRadius =
        progressIndicator.getIndicatorCornerRadius() * getGrowFraction();

    // Draws the track first as the bottom layer.
    drawingDelegate.fillTrackWithColor(
        canvas,
        paint,
        combinedTrackColor,
        0f,
        1f,
        displayedIndicatorWidth,
        displayedRoundedCornerRadius);
    // Draws the indicators.
    for (int segmentIndex = 0;
        segmentIndex < animatorDelegate.segmentColors.length;
        segmentIndex++) {
      drawingDelegate.fillTrackWithColor(
          canvas,
          paint,
          animatorDelegate.segmentColors[segmentIndex],
          animatorDelegate.segmentPositions[2 * segmentIndex],
          animatorDelegate.segmentPositions[2 * segmentIndex + 1],
          displayedIndicatorWidth,
          displayedRoundedCornerRadius);
    }
  }

  // ******************* Setter and getter *******************

  public IndeterminateAnimatorDelegate<AnimatorSet> getAnimatorDelegate() {
    return animatorDelegate;
  }

  public void setAnimatorDelegate(
      @NonNull IndeterminateAnimatorDelegate<AnimatorSet> animatorDelegate) {
    this.animatorDelegate = animatorDelegate;
    animatorDelegate.registerDrawable(this);

    // Adds a listener to cancel indeterminate animator after hidden.
    getHideAnimator()
        .addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                IndeterminateDrawable.this.animatorDelegate.cancelAnimatorImmediately();
                IndeterminateDrawable.this.animatorDelegate.resetPropertiesForNewStart();
              }
            });
    setGrowFraction(1f);
  }

  @NonNull
  public DrawingDelegate getDrawingDelegate() {
    return drawingDelegate;
  }
}

