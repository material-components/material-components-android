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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import static com.google.android.material.transition.platform.MaterialContainerTransform.FIT_MODE_AUTO;
import static com.google.android.material.transition.platform.MaterialContainerTransform.FIT_MODE_HEIGHT;
import static com.google.android.material.transition.platform.MaterialContainerTransform.FIT_MODE_WIDTH;
import static com.google.android.material.transition.platform.TransitionUtils.lerp;

import android.graphics.RectF;
import com.google.android.material.transition.platform.MaterialContainerTransform.FitMode;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class FitModeEvaluators {

  private static final FitModeEvaluator WIDTH =
      new FitModeEvaluator() {
        @Override
        public FitModeResult evaluate(
            float progress,
            float scaleStartFraction,
            float scaleEndFraction,
            float startWidth,
            float startHeight,
            float endWidth,
            float endHeight) {
          // Use same width for start/end views; calculate heights using respective aspect ratios.
          float currentWidth =
              lerp(
                  startWidth,
                  endWidth,
                  scaleStartFraction,
                  scaleEndFraction,
                  progress,
                  /* allowOvershoot= */ true);
          float startScale = currentWidth / startWidth;
          float endScale = currentWidth / endWidth;
          float currentStartHeight = startHeight * startScale;
          float currentEndHeight = endHeight * endScale;
          return new FitModeResult(
              startScale,
              endScale,
              currentWidth,
              currentStartHeight,
              currentWidth,
              currentEndHeight);
        }

        @Override
        public boolean shouldMaskStartBounds(FitModeResult fitModeResult) {
          return fitModeResult.currentStartHeight > fitModeResult.currentEndHeight;
        }

        @Override
        public void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult) {
          float currentHeightDiff =
              Math.abs(fitModeResult.currentEndHeight - fitModeResult.currentStartHeight);
          maskBounds.bottom -= currentHeightDiff * maskMultiplier;
        }
      };

  private static final FitModeEvaluator HEIGHT =
      new FitModeEvaluator() {
        @Override
        public FitModeResult evaluate(
            float progress,
            float scaleStartFraction,
            float scaleEndFraction,
            float startWidth,
            float startHeight,
            float endWidth,
            float endHeight) {
          // Use same height for start/end views; calculate widths using respective aspect ratios.
          float currentHeight =
              lerp(
                  startHeight,
                  endHeight,
                  scaleStartFraction,
                  scaleEndFraction,
                  progress,
                  /* allowOvershoot= */ true);
          float startScale = currentHeight / startHeight;
          float endScale = currentHeight / endHeight;
          float currentStartWidth = startWidth * startScale;
          float currentEndWidth = endWidth * endScale;
          return new FitModeResult(
              startScale,
              endScale,
              currentStartWidth,
              currentHeight,
              currentEndWidth,
              currentHeight);
        }

        @Override
        public boolean shouldMaskStartBounds(FitModeResult fitModeResult) {
          return fitModeResult.currentStartWidth > fitModeResult.currentEndWidth;
        }

        @Override
        public void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult) {
          float currentWidthDiff =
              Math.abs(fitModeResult.currentEndWidth - fitModeResult.currentStartWidth);
          maskBounds.left += currentWidthDiff / 2 * maskMultiplier;
          maskBounds.right -= currentWidthDiff / 2 * maskMultiplier;
        }
      };

  static FitModeEvaluator get(
      @FitMode int fitMode, boolean entering, RectF startBounds, RectF endBounds) {
    switch (fitMode) {
      case FIT_MODE_AUTO:
        return shouldAutoFitToWidth(entering, startBounds, endBounds) ? WIDTH : HEIGHT;
      case FIT_MODE_WIDTH:
        return WIDTH;
      case FIT_MODE_HEIGHT:
        return HEIGHT;
      default:
        throw new IllegalArgumentException("Invalid fit mode: " + fitMode);
    }
  }

  private static boolean shouldAutoFitToWidth(
      boolean entering, RectF startBounds, RectF endBounds) {
    float startWidth = startBounds.width();
    float startHeight = startBounds.height();
    float endWidth = endBounds.width();
    float endHeight = endBounds.height();

    float endHeightFitToWidth = endHeight * startWidth / endWidth;
    float startHeightFitToWidth = startHeight * endWidth / startWidth;
    return entering ? endHeightFitToWidth >= startHeight : startHeightFitToWidth >= endHeight;
  }

  private FitModeEvaluators() {}
}
