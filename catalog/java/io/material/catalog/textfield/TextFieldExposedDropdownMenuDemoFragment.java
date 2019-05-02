/*
 * Copyright 2019 The Android Open Source Project
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

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * A fragment that displays the filled text field demos with start and end icons and controls for
 * the Catalog app.
 */
public class TextFieldExposedDropdownMenuDemoFragment extends TextFieldControllableDemoFragment {

  @Override
  public void onChangeTextFieldColors(TextInputLayout textfield, int color) {
    if (textfield.getBoxBackgroundMode() == TextInputLayout.BOX_BACKGROUND_FILLED) {
      textfield.setBoxBackgroundColor(color);
    } else {
      textfield.setBoxStrokeColor(color);
    }
  }

  @Override
  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_exposed_dropdown_menu_content;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = super.onCreateDemoView(layoutInflater, viewGroup, bundle);

    ArrayAdapter<CharSequence> adapter =
        new ArrayAdapter<>(
            getContext(),
            R.layout.cat_exposed_dropdown_popup_item,
            getResources().getStringArray(R.array.cat_textfield_exposed_dropdown_content));

    AutoCompleteTextView editTextFilledExposedDropdown =
        view.findViewById(R.id.filled_exposed_dropdown);
    editTextFilledExposedDropdown.setAdapter(adapter);

    AutoCompleteTextView editTextFilledEditableExposedDropdown =
        view.findViewById(R.id.filled_exposed_dropdown_editable);
    editTextFilledEditableExposedDropdown.setAdapter(adapter);

    AutoCompleteTextView editTextOutlinedExposedDropdown =
        view.findViewById(R.id.outlined_exposed_dropdown);
    editTextOutlinedExposedDropdown.setAdapter(adapter);

    AutoCompleteTextView editTextOutlinedEditableExposedDropdown =
        view.findViewById(R.id.outlined_exposed_dropdown_editable);
    editTextOutlinedEditableExposedDropdown.setAdapter(adapter);

    return view;
  }
}
