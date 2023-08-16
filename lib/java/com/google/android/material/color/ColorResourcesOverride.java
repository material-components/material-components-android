/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.color;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import java.util.Map;

/**
 * The interface class that hides the detailed implementation of color resources override at
 * runtime. (e.g. with Resources Loader implementation pre-U.)
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface ColorResourcesOverride {
  /**
   * Overrides the color resources to the given context, returns {@code true} if new color values
   * have been applied.
   *
   * @param context The target context.
   * @param colorResourceIdsToColorValues The mapping from the color resources id to the updated
   *     color value.
   */
  // TODO(b/255834202): Using SparseIntArray here to store the mapping to save memory.
  boolean applyIfPossible(
      @NonNull Context context, @NonNull Map<Integer, Integer> colorResourceIdsToColorValues);

  /**
   * Wraps the given Context with the theme overlay where color resources are updated at runtime. If
   * not possible, the original Context will be returned.
   *
   * @param context The target context.
   * @param colorResourceIdsToColorValues The mapping from the color resources id to the updated
   *     color value.
   */
  @NonNull
  Context wrapContextIfPossible(
      @NonNull Context context, @NonNull Map<Integer, Integer> colorResourceIdsToColorValues);

  @Nullable
  static ColorResourcesOverride getInstance() {
    if (VERSION_CODES.R <= VERSION.SDK_INT && VERSION.SDK_INT <= VERSION_CODES.TIRAMISU) {
      return ResourcesLoaderColorResourcesOverride.getInstance();
    } else if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      // TODO(b/255833419): Replace with FabricatedOverlayColorResourcesOverride() when available
      // for U+.
      return ResourcesLoaderColorResourcesOverride.getInstance();
    }
    return null;
  }
}
