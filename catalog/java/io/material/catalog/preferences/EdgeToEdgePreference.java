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

import io.material.catalog.R;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/** Edge to edge preference to enable/disable edge to edge mode. */
public class EdgeToEdgePreference extends CatalogPreference {
  protected static final int OPTION_ID_ON = 1;
  protected static final int OPTION_ID_OFF = 2;

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_ON,
          R.drawable.ic_edge_to_edge_enable_24dp,
          R.string.dynamic_color_preference_option_on);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          DEFAULT_OPTION,
          new Option(
              OPTION_ID_OFF,
              R.drawable.ic_edge_to_edge_disable_24dp,
              R.string.dynamic_color_preference_option_off));

  @Nullable private WindowPreferencesManager windowPreferencesManager;

  public EdgeToEdgePreference() {
    super(R.string.edge_to_edge_preference_description);
  }

  @Override
  @NonNull
  protected ImmutableList<Option> getOptions() {
    return OPTIONS;
  }

  @Override
  @NonNull
  protected Option getDefaultOption() {
    return DEFAULT_OPTION;
  }

  @Override
  protected void apply(@NonNull Context context, @NonNull Option selectedOption) {
    boolean isOptionOn = selectedOption.id == OPTION_ID_ON;
    WindowPreferencesManager windowPreferencesManager = getWindowPreferencesManager(context);

    if (isOptionOn != windowPreferencesManager.isEdgeToEdgeEnabled()) {
      windowPreferencesManager.toggleEdgeToEdgeEnabled();
    }
  }

  @Override
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return true;
  }

  @NonNull
  private WindowPreferencesManager getWindowPreferencesManager(@NonNull Context context) {
    if (windowPreferencesManager == null) {
      windowPreferencesManager = new WindowPreferencesManager(context);
    }
    return windowPreferencesManager;
  }
}
