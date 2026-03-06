/*
 * Copyright 2025 The Android Open Source Project
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

import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

import android.content.Context;
import android.os.Build;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.material.catalog.R;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/**
 * Cutout mode preference to change the cutout mode to default, short edges, never or always.
 */
public class CutoutModePreference extends CatalogPreference {
  private static final int OPTION_ID_DEFAULT = 1;
  private static final int OPTION_ID_SHORT_EDGES = 2;
  private static final int OPTION_ID_NEVER = 3;
  private static final int OPTION_ID_ALWAYS = 4;

  private static final SparseIntArray OPTION_ID_TO_CUTOUT_MODE = new SparseIntArray();
  static {
    OPTION_ID_TO_CUTOUT_MODE.append(OPTION_ID_DEFAULT, LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT);
    OPTION_ID_TO_CUTOUT_MODE.append(OPTION_ID_SHORT_EDGES,
        LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES);
    OPTION_ID_TO_CUTOUT_MODE.append(OPTION_ID_NEVER, LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER);
    OPTION_ID_TO_CUTOUT_MODE.append(OPTION_ID_ALWAYS, LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
  }

  private static final Option DEFAULT_OPTION =
      new Option(OPTION_ID_DEFAULT, 0, R.string.cutout_mode_preference_option_default);

  private static final ImmutableList<Option> OPTIONS;
  static {
    List<Option> list = new ArrayList<>();
    Collections.addAll(list,
        DEFAULT_OPTION,
        new Option(OPTION_ID_SHORT_EDGES, 0, R.string.cutout_mode_preference_option_short_edges),
        new Option(OPTION_ID_NEVER, 0, R.string.cutout_mode_preference_option_never));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      list.add(new Option(OPTION_ID_ALWAYS, 0, R.string.cutout_mode_preference_option_always));
    }
    OPTIONS = new ImmutableList.Builder<Option>()
        .addAll(list)
        .build();
  }

  public CutoutModePreference() {
    super(R.string.cutout_mode_preference_description);
  }

  @Override
  protected boolean isEnabled() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
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
    new WindowPreferencesManager(context).setCutoutMode(
        OPTION_ID_TO_CUTOUT_MODE.get(selectedOption.id));
  }

  @Override
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return true;
  }
}
