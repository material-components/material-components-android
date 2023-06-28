/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.android.material.carousel;

import android.graphics.RectF;
import android.view.View;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Interface for any view that can clip itself and all children to a percentage of its size. */
interface Maskable {

  /**
   * Set the percentage by which this {@link View} should mask itself along the x axis.
   *
   * <p>This method serves the same purpose as {@link #setMaskRectF(RectF)} but requires the
   * implementing view to calculate the correct rect given the mask percentage.
   *
   * @param percentage 0 when this view is fully unmasked. 1 when this view is fully masked.
   * @deprecated {@link CarouselLayoutManager} calculates its own mask percentages.
   */
  @Deprecated
  void setMaskXPercentage(@FloatRange(from = 0F, to = 1F) float percentage);

  /**
   * Gets the percentage by which this {@link View} should mask itself along the x axis.
   *
   * @return a float between 0 and 1 where 0 is fully unmasked and 1 is fully masked.
   * @deprecated {@link CarouselLayoutManager} calculates its own mask percentages.
   */
  @FloatRange(from = 0F, to = 1F)
  @Deprecated
  float getMaskXPercentage();

  /**
   * Sets a {@link RectF} that this {@link View} will mask itself by.
   *
   * @param maskRect a rect in the view's coordinates to mask by
   */
  void setMaskRectF(@NonNull RectF maskRect);

  /** Gets a {@link RectF} that this {@link View} is masking itself by. */
  @NonNull
  RectF getMaskRectF();

  /**
   * Sets an {@link OnMaskChangedListener}.
   *
   * @param listener a listener to receive callbacks for changes in the mask or null to clear the
   *     listener.
   */
  void setOnMaskChangedListener(@Nullable OnMaskChangedListener listener);
}
