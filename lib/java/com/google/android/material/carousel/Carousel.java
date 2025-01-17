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

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.carousel.CarouselLayoutManager.Alignment;

/** An interface that defines a widget that can be configured as a Carousel. */
@RestrictTo(Scope.LIBRARY_GROUP)
public interface Carousel {

  /** Gets the width of the carousel container. */
  int getContainerWidth();

  /** Gets the height of the carousel container. */
  int getContainerHeight();

  /** Whether or not the orientation is horizontal.  */
  boolean isHorizontal();

  /** Gets the alignment of the carousel.  */
  @Alignment int getCarouselAlignment();

  /** Gets the number of items in the carousel. */
  int getItemCount();
}
