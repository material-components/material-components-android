/*
 * Copyright 2022 The Android Open Source Project
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

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import javax.inject.Inject;

/** This configures activity-level options/preferences. */
public class BaseCatalogActivity extends AppCompatActivity implements HasAndroidInjector {

  @Inject DispatchingAndroidInjector<Object> androidInjector;

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    safeInject();
    super.onCreate(bundle);
  }

  /** Returns {@code true} if preferences option is enabled. */
  public boolean isPreferencesEnabled() {
    return false;
  }

  /** Returns {@code true} if color harmonization is enabled. */
  public boolean isColorHarmonizationEnabled() {
    return true;
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }

  @SuppressWarnings("CatchingUnchecked")
  private void safeInject() {
    try {
      AndroidInjection.inject(this);
    } catch (Exception e) {
      // Ignore exception, not all DemoActivity subclasses need to inject
    }
  }
}
