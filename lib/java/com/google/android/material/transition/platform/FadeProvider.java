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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import static com.google.android.material.transition.platform.TransitionUtils.lerp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.transition.TransitionValues;

/** A class that configures and is able to provide an {@link Animator} that fades a view. */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public class FadeProvider implements VisibilityAnimatorProvider {

  private float incomingEndThreshold = 1f;

  /**
   * Get the fraction at which the appearing fade animation will end between 0 and 1.
   *
   * @see #setIncomingEndThreshold(float)
   */
  public float getIncomingEndThreshold() {
    return incomingEndThreshold;
  }

  /**
   * Set the fraction by which an appearing fade animation should end between 0 and 1.
   *
   * <p>This can be used to stagger animations when this class' resulting animator is used inside an
   * AnimatorSet. If the containing AnimatorSet has a total duration of 100 milliseconds and
   * incomingEndThreshold is set to .75f, this class' animator will run and complete (the view will
   * be completely faded in) after the AnimatorSet has run for 75 milliseconds.
   */
  public void setIncomingEndThreshold(float incomingEndThreshold) {
    this.incomingEndThreshold = incomingEndThreshold;
  }

  @Nullable
  @Override
  public Animator createAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return createFadeAnimator(
        view,
        /* startValue= */ 0f,
        /* endValue= */ 1f,
        /* startFraction= */ 0F,
        /* endFraction=*/ incomingEndThreshold);
  }

  @Nullable
  @Override
  public Animator createDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return createFadeAnimator(
        view,
        /* startValue= */ 1f,
        /* endValue= */ 0f,
        /* startFraction= */ 0F,
        /* endFraction=*/ 1F);
  }

  private static Animator createFadeAnimator(
      final View view,
      final float startValue,
      final float endValue,
      final @FloatRange(from = 0.0, to = 1.0) float startFraction,
      final @FloatRange(from = 0.0, to = 1.0) float endFraction) {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(lerp(startValue, endValue, startFraction, endFraction, progress));
          }
        });
    return animator;
  }
}
