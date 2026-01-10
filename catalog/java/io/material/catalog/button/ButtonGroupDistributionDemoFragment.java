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
package io.material.catalog.button;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButtonGroup;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment that displays a button group demo for different button distribution options in the
 * Catalog app.
 */
public class ButtonGroupDistributionDemoFragment extends DemoFragment {

  /** Create a Demo View with different types of {@link MaterialButtonGroup}. */
  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_buttons_group_distribution_fragment, viewGroup, /* attachToRoot= */ false);

    return view;
  }
}
