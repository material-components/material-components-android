/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.preferences;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;

/**
 * Base class to provide injectable preferences list for different Catalog versions.
 */
public abstract class BaseCatalogPreferences {
  protected static final ImmutableList<CatalogPreference> COMMON_PREFERENCES =
      ImmutableList.of(
          new ThemePreference(),
          new ShapeCornerFamilyPreference(),
          new ShapeCornerSizePreference(),
          new EdgeToEdgePreference());

  public final void applyPreferences(Context context) {
    for (CatalogPreference preference : getPreferences()) {
      preference.apply(context);
    }
  }

  /**
   * Implement this method to return available {@link CatalogPreference} list on
   * the targeting Catalog version.
   */
  @NonNull
  protected abstract ImmutableList<CatalogPreference> getPreferences();
}
