/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * @deprecated Use {@link android.view.ViewOverlay} instead.
 * @hide
 */
@Deprecated
@RestrictTo(LIBRARY_GROUP)
public interface ViewOverlayImpl {

  /**
   * Adds a Drawable to the overlay. The bounds of the drawable should be relative to the host view.
   * Any drawable added to the overlay should be removed when it is no longer needed or no longer
   * visible.
   *
   * @param drawable The Drawable to be added to the overlay. This drawable will be drawn when the
   *     view redraws its overlay.
   * @see #remove(Drawable)
   */
  void add(@NonNull Drawable drawable);

  /**
   * Removes the specified Drawable from the overlay.
   *
   * @param drawable The Drawable to be removed from the overlay.
   * @see #add(Drawable)
   */
  void remove(@NonNull Drawable drawable);
}
