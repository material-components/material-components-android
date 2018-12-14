/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.application.legacymultidex;

import android.content.Context;
import androidx.multidex.MultiDex;
import io.material.catalog.application.CatalogApplication;

/**
 * A version of {@link CatalogApplication} for development builds on older phones that uses the
 * multidex support library for allowing multiple dex files.
 */
public class LegacyMultidexCatalogApplication extends CatalogApplication {
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }
}
