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

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.carousel.KeylineState.Keyline;

/**
 * Configuration class responsible for creating a {@link KeylineState} used by {@link Carousel} to
 * mask and offset views as they move along a scrolling axis.
 *
 * <p>An implementation of {@link CarouselConfiguration} is responsible for overriding {@link
 * #onFirstChildMeasuredWithMargins(View)} and constructing a {@link KeylineState}.
 */
abstract class CarouselConfiguration {

  private final Carousel carousel;

  CarouselConfiguration(@NonNull Carousel carousel) {
    this.carousel = carousel;
  }

  /**
   * Calculates a keyline arrangement and returns a constructed {@link KeylineState}.
   *
   * <p>This method should handle:
   *
   * <p>1) How large a child should be. This can be based on the measured width of the {@code
   * child}, a division of the total available space, or any other strategy a subclass might prefer.
   * Carousel will lay out all items end-to-end, each using this size, and then offset/mask/animate
   * children as they move between points long the scroll axis called {@link Keyline}s.
   *
   * <p>2) Points and ranges along the scrolling axis at which items should be masked by a set
   * percentage. These points and ranges (a.k.a. keylines and keyline ranges) can be inside or
   * outside the bounds of the visible scroll window. As a child moves along the scrolling axis, it
   * will mask and unmask itself according to the points ({@link Keyline}s) it is moving between.
   *
   * <p>3) Create and return a {@link KeylineState}. Use the full child size [1] and points/ranges
   * [2] from above to build a {@link KeylineState}. This object is everything the layout manager
   * needs to offset and mask items as they move along the scroll axis.
   *
   * @param child The first measured view from the carousel, use this view to determine the max size
   *     that all items in the carousel will be given.
   * @return A {@link KeylineState} to be used by the layout manager to offset and mask children
   *     along the scrolling axis.
   */
  protected abstract KeylineState onFirstChildMeasuredWithMargins(@NonNull View child);

  /** Gets the {@link Carousel} associated with this configuration. */
  @NonNull
  protected final Carousel getCarousel() {
    return carousel;
  }
}
