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

/**
 * Interface for any view that can clip itself and all children to a percentage of its size.
 *
 * <p>TODO(b/238614892) Investigate moving to a ViewOutlineProvider.
 */
interface Maskable {

  /**
   * Set the percentage by which this {@link View} should mask itself along the x axis.
   *
   * @param percentage 0 when this view is fully unmasked. 1 when this view is fully masked.
   */
  void setMaskXPercentage(@FloatRange(from = 0F, to = 1F) float percentage);

  /**
   * Gets the percentage by which this {@link View} should mask itself along the x axis.
   *
   * @return a float between 0 and 1 where 0 is fully unmasked and 1 is fully masked.
   */
  @FloatRange(from = 0F, to = 1F)
  float getMaskXPercentage();

  /** Gets a {@link RectF} that this {@link View} is masking itself by. */
  @NonNull
  RectF getMaskRect();

  /** Adds an {@link OnMaskChangedListener}. */
  void addOnMaskChangedListener(@NonNull OnMaskChangedListener listener);

  /** Removes an {@link OnMaskChangedListener}. */
  void removeOnMaskChangedListener(@NonNull OnMaskChangedListener listener);
}
