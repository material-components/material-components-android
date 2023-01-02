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

package com.google.android.material.tabs;

import static com.google.android.material.animation.AnimationUtils.lerp;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

/**
 * An implementation of {@link TabIndicatorInterpolator} that translates the left and right sides of
 * a selected tab indicator independently to make the indicator grow and shrink between
 * destinations.
 */
class ElasticTabIndicatorInterpolator extends TabIndicatorInterpolator {

  /** Fit a linear 0F - 1F curve to an ease out sine (decelerating) curve. */
  private static float decInterp(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    // Ease out sine
    return (float) Math.sin((fraction * Math.PI) / 2.0);
  }

  /** Fit a linear 0F - 1F curve to an ease in sine (accelerating) curve. */
  private static float accInterp(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    // Ease in sine
    return (float) (1.0 - Math.cos((fraction * Math.PI) / 2.0));
  }

  @Override
  void updateIndicatorForOffset(
      TabLayout tabLayout,
      View startTitle,
      View endTitle,
      float offset,
      @NonNull Drawable indicator) {
      // The indicator should be positioned somewhere between start and end title. Override the
      // super implementation and adjust the indicator's left and right bounds independently.
      RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, startTitle);
      RectF endIndicator = calculateIndicatorWidthForTab(tabLayout, endTitle);

      float leftFraction;
      float rightFraction;

      final boolean isMovingRight = startIndicator.left < endIndicator.left;
      // If the selection indicator should grow and shrink during the animation, interpolate
      // the left and right bounds of the indicator using separate easing functions.
      // The side in which the indicator is moving should always be the accelerating
      // side.
      if (isMovingRight) {
        leftFraction = accInterp(offset);
        rightFraction = decInterp(offset);
      } else {
        leftFraction = decInterp(offset);
        rightFraction = accInterp(offset);
      }
      indicator.setBounds(
          lerp((int) startIndicator.left, (int) endIndicator.left, leftFraction),
          indicator.getBounds().top,
          lerp((int) startIndicator.right, (int) endIndicator.right, rightFraction),
          indicator.getBounds().bottom);
  }
}
