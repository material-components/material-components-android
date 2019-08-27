/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.testapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.testapp.base.BaseTestActivity;

/** Test activity that has the different variations of the Exposed Dropdown Menu. */
public class ExposedDropdownMenuActivity extends BaseTestActivity {
  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.exposed_dropdown_menu;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            this,
            R.layout.exposed_dropdown_popup_item,
            getResources().getStringArray(R.array.exposed_dropdown_content));

    AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.edittext_filled);
    editTextFilledExposedDropdown.setAdapter(adapter);

    AutoCompleteTextView editTextFilledEditableExposedDropdown =
        findViewById(R.id.edittext_filled_editable);
    editTextFilledEditableExposedDropdown.setAdapter(adapter);
  }
}
