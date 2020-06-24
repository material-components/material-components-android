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

  static final float PROGRESS_THRESHOLD = 0.35f;

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeThroughAnimator(
        view,
        /* startValue= */ 0f,
        /* endValue= */ 1f,
        /* startFraction= */ PROGRESS_THRESHOLD,
        /* endFraction= */ 1f);
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeThroughAnimator(
        view,
        /* startValue= */ 1f,
        /* endValue= */ 0f,
        /* startFraction= */ 0f,
        /* endFraction= */ PROGRESS_THRESHOLD);
  }

  private static Animator createFadeThroughAnimator(
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
