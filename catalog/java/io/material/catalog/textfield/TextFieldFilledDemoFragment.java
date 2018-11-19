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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** A fragment that displays the filled text field demos with controls for the Catalog app. */
public class TextFieldFilledDemoFragment extends TextFieldControllableDemoFragment {

  @Override
  public void onChangeTextFieldColors(TextInputLayout textfield, int color) {
    textfield.setBoxBackgroundColor(color);
  }

  @Override
  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_filled_content;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getTextFieldContent(), viewGroup, false);
    TextInputEditText startIconEditText = view.findViewById(R.id.edit_text_start_icon);
    Drawable startIcon =
        AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_search_24px);
    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
        startIconEditText, startIcon, null, null, null);
    return view;
  }
}
