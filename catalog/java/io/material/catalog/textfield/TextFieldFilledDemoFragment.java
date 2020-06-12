/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.textfield;

import io.material.catalog.R;

import androidx.annotation.LayoutRes;

/** A fragment that displays the filled text field demos with controls for the Catalog app. */
public class TextFieldFilledDemoFragment extends TextFieldControllableDemoFragment {

  @Override
  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_filled_content;
  }
}
