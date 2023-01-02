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

import com.google.android.material.R;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import java.util.Map;

/**
 * The detailed implementation of color resources overriding at runtime with Resources Loader
 * implementation.
 */
@RequiresApi(api = VERSION_CODES.R)
class ResourcesLoaderColorResourcesOverride implements ColorResourcesOverride {

  private ResourcesLoaderColorResourcesOverride() {}

  /**
   * Overrides the color resources to the given context, returns {@code true} if new color values
   * have been applied.
   *
   * @param context The target context.
   * @param colorResourceIdsToColorValues The mapping from the color resources id to the updated
   *     color value.
   */
  @Override
  public boolean applyIfPossible(
      Context context, Map<Integer, Integer> colorResourceIdsToColorValues) {
    if (ResourcesLoaderUtils.addResourcesLoaderToContext(context, colorResourceIdsToColorValues)) {
      ThemeUtils.applyThemeOverlay(context, R.style.ThemeOverlay_Material3_PersonalizedColors);
      return true;
    }
    return false;
  }

  static ColorResourcesOverride getInstance() {
    return ResourcesLoaderColorResourcesOverrideSingleton.INSTANCE;
  }

  private static class ResourcesLoaderColorResourcesOverrideSingleton {
    private static final ResourcesLoaderColorResourcesOverride INSTANCE =
        new ResourcesLoaderColorResourcesOverride();
  }
}
