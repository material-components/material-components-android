/*
 * Copyright 2018 The Android Open Source Project
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

package com.google.android.material.theme;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import android.support.v7.app.AppCompatViewInflater;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

/**
 * An extension of {@link AppCompatViewInflater} that replaces some framework widgets with Material
 * Components ones at inflation time, provided a Material Components theme is in use.
 */
@Keep // Make proguard keep this class as it's accessed reflectively by AppCompat
public class MaterialComponentsViewInflater extends AppCompatViewInflater {

  @NonNull
  @Override
  protected AppCompatButton createButton(Context context, AttributeSet attrs) {
    return new MaterialButton(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatCheckBox createCheckBox(Context context, AttributeSet attrs) {
    return new MaterialCheckBox(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatRadioButton createRadioButton(Context context, AttributeSet attrs) {
    return new MaterialRadioButton(context, attrs);
  }
}
