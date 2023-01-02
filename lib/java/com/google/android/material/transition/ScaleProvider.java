/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that configures and is able to provide an {@link Animator} that scales a view.
 *
 * <p>{@code ScaleProvider}'s constructor optionally takes a {@code growing} parameter. By default,
 * this is set to true and will increase the size of the target both when appearing and
 * disappearing. This is useful when pairing two animating targets, one appearing and one
 * disappearing, that should both be either growing or shrinking to create a visual relationship.
 */
public final class ScaleProvider implements VisibilityAnimatorProvider {

  private float outgoingStartScale = 1f;
  private float outgoingEndScale = 1.1f;
  private float incomingStartScale = 0.8f;
  private float incomingEndScale = 1f;

  private boolean growing;
  private boolean scaleOnDisappear = true;

  public ScaleProvider() {
    this(true);
  }

  public ScaleProvider(boolean growing) {
    this.growing = growing;
  }

  /** Whether or not this animation's target will grow or shrink in size. */
  public boolean isGrowing() {
    return growing;
  }

  /** Set whether or not this animation's target will grow or shrink in size. */
  public void setGrowing(boolean growing) {
    this.growing = growing;
  }

  /**
   * Whether or not a scale animation will be run on this animation's target when disappearing.
   *
   * @see #setScaleOnDisappear(boolean)
   */
  public boolean isScaleOnDisappear() {
    return scaleOnDisappear;
  }

  /**
   * Set whether or not a scale animation will be run on this animation's target when disappearing.
   *
   * <p>This is useful when using a single {@code ScaleProvider} that runs on multiple targets and
   * only appearing targets should be animated.
   */
  public void setScaleOnDisappear(boolean scaleOnDisappear) {
    this.scaleOnDisappear = scaleOnDisappear;
  }

  /**
   * The scale x and scale y value which an appearing and shrinking target will scale to and a
   * disappearing and growing target will scale from.
   */
  public float getOutgoingStartScale() {
    return outgoingStartScale;
  }

  /**
   * Set the scale x and scale y value which an appearing and shrinking target should scale to and a
   * disappearing and growing target should scale from.
   */
  public void setOutgoingStartScale(float outgoingStartScale) {
    this.outgoingStartScale = outgoingStartScale;
  }

  /**
   * The scale x and scale y value which an appearing and shrinking target will scale from and a
   * disappearing and growing target will scale to.
   */
  public float getOutgoingEndScale() {
    return outgoingEndScale;
  }

  /**
   * Set the scale x and scale y value which an appearing and shrinking target should scale from and
   * a disappearing and growing target should scale to.
   */
  public void setOutgoingEndScale(float outgoingEndScale) {
    this.outgoingEndScale = outgoingEndScale;
  }

  /**
   * The scale x and scale y value which an appearing and growing target will scale from and a
   * disappearing and shrinking target will scale to.
   */
  public float getIncomingStartScale() {
    return incomingStartScale;
  }

  /**
   * Set the scale x and scale y value which an appearing and growing target should scale from and a
   * disappearing and shrinking target should scale to.
   */
  public void setIncomingStartScale(float incomingStartScale) {
    this.incomingStartScale = incomingStartScale;
  }

  /**
   * The scale x and scale y value which an appearing and growing target will scale to and a
   * disappearing and shrinking target will scale from.
   */
  public float getIncomingEndScale() {
    return incomingEndScale;
  }

  /**
   * Set the scale x and scale y value which an appearing and growing target should scale to and a
   * disappearing and shrinking target should scale from.
   */
  public void setIncomingEndScale(float incomingEndScale) {
    this.incomingEndScale = incomingEndScale;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    if (growing) {
      return createScaleAnimator(view, incomingStartScale, incomingEndScale);
    } else {
      return createScaleAnimator(view, outgoingEndScale, outgoingStartScale);
    }
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    if (!scaleOnDisappear) {
      return null;
    }

    if (growing) {
      return createScaleAnimator(view, outgoingStartScale, outgoingEndScale);
    } else {
      return createScaleAnimator(view, incomingEndScale, incomingStartScale);
    }
  }

  private static Animator createScaleAnimator(final View view, float startScale, float endScale) {
    final float originalScaleX = view.getScaleX();
    final float originalScaleY = view.getScaleY();
    ObjectAnimator animator =
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(
                View.SCALE_X, originalScaleX * startScale, originalScaleX * endScale),
            PropertyValuesHolder.ofFloat(
                View.SCALE_Y, originalScaleY * startScale, originalScaleY * endScale));
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setScaleX(originalScaleX);
            view.setScaleY(originalScaleY);
          }
        });
    return animator;
  }
}
