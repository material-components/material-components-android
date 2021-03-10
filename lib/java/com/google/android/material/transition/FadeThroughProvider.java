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

import static com.google.android.material.transition.TransitionUtils.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that configures and is able to provide an {@link Animator} that fades out or in a view.
 *
 * <p>FadeThroughProvider differs from FadeProvider in that it fades out and in views sequentially.
 */
public final class FadeThroughProvider implements VisibilityAnimatorProvider {

  static final float FADE_THROUGH_THRESHOLD = 0.35F;

  private float progressThreshold = FADE_THROUGH_THRESHOLD;

  /**
   * Get the point at which a disappearing target finishes fading out and an appearing target begins
   * to fade in.
   *
   * @see #setProgressThreshold(float)
   */
  public float getProgressThreshold() {
    return progressThreshold;
  }

  /**
   * Set the point, between 0 and 1, at which a disappearing target finishes fading out and an
   * appearing target begins to fade in.
   *
   * @param progressThreshold A float between 0 and 1.
   */
  public void setProgressThreshold(@FloatRange(from = 0.0F, to = 1F) float progressThreshold) {
    this.progressThreshold = progressThreshold;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    float originalAlpha = view.getAlpha() == 0F ? 1F : view.getAlpha();
    return createFadeThroughAnimator(
        view,
        /* startValue= */ 0F,
        /* endValue= */ originalAlpha,
        /* startFraction= */ progressThreshold,
        /* endFraction= */ 1F,
        originalAlpha);
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    float originalAlpha = view.getAlpha() == 0F ? 1F : view.getAlpha();
    return createFadeThroughAnimator(
        view,
        /* startValue= */ originalAlpha,
        /* endValue= */ 0F,
        /* startFraction= */ 0F,
        /* endFraction= */ progressThreshold,
        originalAlpha);
  }

  private static Animator createFadeThroughAnimator(
      final View view,
      final float startValue,
      final float endValue,
      final @FloatRange(from = 0.0, to = 1.0) float startFraction,
      final @FloatRange(from = 0.0, to = 1.0) float endFraction,
      final float originalAlpha) {
    ValueAnimator animator = ValueAnimator.ofFloat(0F, 1F);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(lerp(startValue, endValue, startFraction, endFraction, progress));
          }
        });
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            // Restore the view's alpha back to its original value.
            view.setAlpha(originalAlpha);
          }
        });
    return animator;
  }
}
