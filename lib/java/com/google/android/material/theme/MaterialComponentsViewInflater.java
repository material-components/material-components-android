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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;

/**
 * An extension of {@link AppCompatViewInflater} that replaces some framework widgets with Material
 * Components ones at inflation time, provided a Material Components theme is in use.
 */
@Keep // Make proguard keep this class as it's accessed reflectively by AppCompat
public class MaterialComponentsViewInflater extends AppCompatViewInflater {

  // Cached background resource ID used for workaround to not inflate MaterialButton in
  // API 23-25 FloatingToolbar. Technically 0 is the only invalid resource ID, but we are assuming
  // it's safe to use -1 as a sentinel here.
  private static int floatingToolbarItemBackgroundResId = -1;

  @NonNull
  @Override
  protected AppCompatButton createButton(@NonNull Context context, @NonNull AttributeSet attrs) {
    if (shouldInflateAppCompatButton(context, attrs)) {
      return new AppCompatButton(context, attrs);
    }

    return new MaterialButton(context, attrs);
  }

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  protected boolean shouldInflateAppCompatButton(
      @NonNull Context context, @NonNull AttributeSet attrs) {
    // Workaround for FloatingToolbar inflating floating_popup_menu_button.xml on API 23-25, which
    // should not have MaterialButton styling.

    if (!(VERSION.SDK_INT == VERSION_CODES.M
        || VERSION.SDK_INT == VERSION_CODES.N
        || VERSION.SDK_INT == VERSION_CODES.N_MR1)) {
      return false;
    }

    if (floatingToolbarItemBackgroundResId == -1) {
      floatingToolbarItemBackgroundResId =
          context
              .getResources()
              .getIdentifier("floatingToolbarItemBackgroundDrawable", "^attr-private", "android");
    }

    if (floatingToolbarItemBackgroundResId != 0 && floatingToolbarItemBackgroundResId != -1) {
      for (int i = 0; i < attrs.getAttributeCount(); i++) {
        if (attrs.getAttributeNameResource(i) == android.R.attr.background) {
          int backgroundResourceId = attrs.getAttributeListValue(i, null, 0);
          if (floatingToolbarItemBackgroundResId == backgroundResourceId) {
            return true;
          }
        }
      }
    }

    return false;
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

  @NonNull
  @Override
  protected AppCompatTextView createTextView(Context context, AttributeSet attrs) {
    return new MaterialTextView(context, attrs);
  }
}
