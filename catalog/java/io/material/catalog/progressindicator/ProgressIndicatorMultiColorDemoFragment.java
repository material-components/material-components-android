/*
 * Copyright 2023 The Android Open Source Project
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

import androidx.annotation.LayoutRes;

/**
 * This is the fragment to demo using multiple indicator colors in {@link LinearProgressIndicator}
 * and {@link CircularProgressIndicator}.
 */
public class ProgressIndicatorMultiColorDemoFragment extends ProgressIndicatorDemoFragment {

  @Override
  @LayoutRes
  public int getProgressIndicatorContentLayout() {
    return R.layout.cat_progress_indicator_multi_color_content;
  }
}
