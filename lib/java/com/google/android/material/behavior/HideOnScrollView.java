/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.material.behavior;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class for constants and {@code IntDefs} to be shared between the different {@link
 * HideViewOnScrollBehavior} variations.
 */
final class HideOnScrollView {

  /** The sheet slides out from the right edge of the screen. */
  static final int EDGE_RIGHT = 0;

  /** The sheet slides out from the bottom edge of the screen. */
  static final int EDGE_BOTTOM = 1;

  /** The sheet slides out from the left edge of the screen. */
  static final int EDGE_LEFT = 2;

  /**
   * The edge of the screen that a sheet slides out from.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({EDGE_RIGHT, EDGE_BOTTOM, EDGE_LEFT})
  @Retention(RetentionPolicy.SOURCE)
  @interface ViewEdge {}

  private HideOnScrollView() {}
}
