/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.topappbar;

import androidx.annotation.LayoutRes;

import io.material.catalog.R;

/** A fragment that displays a large collapsing Top App Bar demo for the Catalog app. */
public class TopAppBarCollapsingLargeMiddleEllipsizeDemoFragment extends BaseTopAppBarCollapsingDemoFragment {

  @LayoutRes
  protected int getCollapsingToolbarLayoutResId() {
    return R.layout.cat_topappbar_collapsing_large_middle_ellipsize_fragment;
  }
}
