/*
 * Copyright 2019 The Android Open Source Project
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
import android.os.Build.VERSION_CODES;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link Visibility} {@link android.transition.Transition} that provides a fade out or in
 * depending on whether or not the target view is appearing or disappearing.
 */
@RequiresApi(VERSION_CODES.KITKAT)
public class FadeThrough extends Visibility {

  static final float PROGRESS_THRESHOLD = 0.35f;

  @NonNull
  @Override
  public Animator onAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return createFadeThroughAnimator(
        view,
        /* startValue= */ 0f,
        /* endValue= */ 1f,
        /* startFraction= */ PROGRESS_THRESHOLD,
        /* endFraction= */ 1f);
  }

  @NonNull
  @Override
  public Animator onDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
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
