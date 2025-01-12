/*
 * Copyright 2024 The Android Open Source Project
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
package io.material.catalog.progressindicator;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.material.catalog.feature.DemoFragment;

/**
 * Base class that provides a structure for Progress Indicator demos with optional controls for the
 * Catalog app.
 */
public abstract class ProgressIndicatorDemoFragment extends DemoFragment {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_fragment, viewGroup, false /* attachToRoot */);

    // Inflate the demo content layout.
    ViewGroup content = view.findViewById(R.id.content);
    content.addView(
        layoutInflater.inflate(
            getProgressIndicatorContentLayout(), content, /* attachToRoot= */ false));

    initDemoContents(view);

    // Inflate the demo controls layout.
    @LayoutRes int demoControls = getProgressIndicatorDemoControlLayout();
    if (demoControls != 0) {
      ViewGroup control = view.findViewById(R.id.control);
      control.addView(
          layoutInflater.inflate(
              getProgressIndicatorDemoControlLayout(), control, /* attachToRoot= */ false));
    }

    initDemoControls(view);

    return view;
  }

  public void initDemoContents(@NonNull View view) {}

  public void initDemoControls(@NonNull View view) {}

  @LayoutRes
  public abstract int getProgressIndicatorContentLayout();

  @LayoutRes
  public int getProgressIndicatorDemoControlLayout() {
    return 0;
  }
}
