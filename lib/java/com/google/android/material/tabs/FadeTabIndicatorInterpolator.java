/*
 * Copyright (C) 2021 The Android Open Source Project
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
import androidx.annotation.NonNull;

/**
 * An implementation of {@link TabIndicatorInterpolator} that sequentially fades out the selected
 * tab indicator from the current destination and fades it back in at its new destination.
 */
class FadeTabIndicatorInterpolator extends TabIndicatorInterpolator {

  // When the indicator will disappear from the current tab and begin to reappear at the newly
  // selected tab.
  private static final float FADE_THRESHOLD = 0.5F;

  @Override
  void updateIndicatorForOffset(
      TabLayout tabLayout,
      View startTitle,
      View endTitle,
      float offset,
      @NonNull Drawable indicator) {
    View tab = offset < FADE_THRESHOLD ? startTitle : endTitle;
    RectF bounds = calculateIndicatorWidthForTab(tabLayout, tab);
    float alpha = offset < FADE_THRESHOLD
        ? lerp(1F, 0F, 0F, FADE_THRESHOLD, offset)
        : lerp(0F, 1F, FADE_THRESHOLD, 1F, offset);

    indicator.setBounds(
        (int) bounds.left,
        indicator.getBounds().top,
        (int) bounds.right,
        indicator.getBounds().bottom
    );
    indicator.setAlpha((int) (alpha * 255F));
  }
}
