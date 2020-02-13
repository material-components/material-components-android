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

import static com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_CROSS;
import static com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_IN;
import static com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_OUT;
import static com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_THROUGH;
import static com.google.android.material.transition.TransitionUtils.lerp;

import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.google.android.material.transition.MaterialContainerTransform.FadeMode;

@RequiresApi(VERSION_CODES.LOLLIPOP)
class FadeModeEvaluators {

  private static final FadeModeEvaluator IN =
      new FadeModeEvaluator() {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction) {
          int startAlpha = 255;
          int endAlpha = lerp(0, 255, fadeStartFraction, fadeEndFraction, progress);
          return FadeModeResult.endOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator OUT =
      new FadeModeEvaluator() {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction) {
          int startAlpha = lerp(255, 0, fadeStartFraction, fadeEndFraction, progress);
          int endAlpha = 255;
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator CROSS =
      new FadeModeEvaluator() {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction) {
          int startAlpha = lerp(255, 0, fadeStartFraction, fadeEndFraction, progress);
          int endAlpha = lerp(0, 255, fadeStartFraction, fadeEndFraction, progress);
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator THROUGH =
      new FadeModeEvaluator() {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction) {
          float fadeFractionDiff = fadeEndFraction - fadeStartFraction;
          float fadeFractionThreshold =
              fadeStartFraction + fadeFractionDiff * FadeThrough.PROGRESS_THRESHOLD;
          int startAlpha = lerp(255, 0, fadeStartFraction, fadeFractionThreshold, progress);
          int endAlpha = lerp(0, 255, fadeFractionThreshold, fadeEndFraction, progress);
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  static FadeModeEvaluator get(@FadeMode int fadeMode, boolean entering) {
    switch (fadeMode) {
      case FADE_MODE_IN:
        return entering ? IN : OUT;
      case FADE_MODE_OUT:
        return entering ? OUT : IN;
      case FADE_MODE_CROSS:
        return CROSS;
      case FADE_MODE_THROUGH:
        return THROUGH;
      default:
        throw new IllegalArgumentException("Invalid fade mode: " + fadeMode);
    }
  }

  private FadeModeEvaluators() {}
}
