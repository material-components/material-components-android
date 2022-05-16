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

import io.material.catalog.R;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColors.Precondition;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.google.common.collect.ImmutableList;

/** Dynamic color preference to enable/disable dynamic colors. */
public class DynamicColorPreference extends CatalogPreference {
  protected static final int OPTION_ID_ON = 1;
  protected static final int OPTION_ID_OFF = 2;

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_ON,
          R.drawable.ic_dynamic_color_24px,
          R.string.dynamic_color_preference_option_on);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          DEFAULT_OPTION,
          new Option(
              OPTION_ID_OFF,
              0,
              R.string.dynamic_color_preference_option_off));

  private boolean isApplied;
  private boolean isOptionOn;

  private final Precondition precondition = (activity, theme) -> isOptionOn;

  public DynamicColorPreference() {
    super(R.string.dynamic_color_preference_description);
  }

  @Override
  protected boolean isEnabled() {
    return DynamicColors.isDynamicColorAvailable();
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
    isOptionOn = selectedOption.id == OPTION_ID_ON;
    if (isOptionOn && !isApplied) {
      isApplied = true;
      // TODO(b/221246424): Add preference option to turn on/off the color harmonization.
      applyDynamicColorsWithMaterialDefaultHarmonization(context);
    }
  }

  @Override
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return true;
  }

  private void applyDynamicColorsWithMaterialDefaultHarmonization(@NonNull Context context) {
    DynamicColors.applyToActivitiesIfAvailable(
        (Application) context.getApplicationContext(),
        new DynamicColorsOptions.Builder()
            .setPrecondition(precondition)
            .setOnAppliedCallback(
                activity -> {
                  if ((activity instanceof BaseCatalogActivity)
                      && ((BaseCatalogActivity) activity).isColorHarmonizationEnabled()) {
                    HarmonizedColors.applyToContextIfAvailable(
                        activity, HarmonizedColorsOptions.createMaterialDefaults());
                  }
                })
            .build());
  }
}
