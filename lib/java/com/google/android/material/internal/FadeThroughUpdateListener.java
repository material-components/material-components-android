/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Facilitates a fade out and then a fade in of the two input views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class FadeThroughUpdateListener implements AnimatorUpdateListener {

  @Nullable private final View fadeOutView;
  @Nullable private final View fadeInView;
  private final float[] alphas;

  public FadeThroughUpdateListener(@Nullable View fadeOutView, @Nullable View fadeInView) {
    this.fadeOutView = fadeOutView;
    this.fadeInView = fadeInView;
    this.alphas = new float[2];
  }

  @Override
  public void onAnimationUpdate(@NonNull ValueAnimator animation) {
    float progress = (float) animation.getAnimatedValue();
    FadeThroughUtils.calculateFadeOutAndInAlphas(progress, alphas);
    if (fadeOutView != null) {
      fadeOutView.setAlpha(alphas[0]);
    }
    if (fadeInView != null) {
      fadeInView.setAlpha(alphas[1]);
    }
  }
}
