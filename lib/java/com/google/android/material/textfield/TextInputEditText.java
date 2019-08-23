/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.textfield;

import com.google.android.material.R;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * A special sub-class of {@link android.widget.EditText} designed for use as a child of {@link
 * com.google.android.material.textfield.TextInputLayout}.
 *
 * <p>Using this class allows us to display a hint in the IME when in 'extract' mode and provides
 * accessibility support for {@link com.google.android.material.textfield.TextInputLayout}.
 */
public class TextInputEditText extends AppCompatEditText {

  public TextInputEditText(Context context) {
    this(context, null);
  }

  public TextInputEditText(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.editTextStyle);
  }

  public TextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Meizu devices expect TextView#mHintLayout to be non-null if TextView#getHint() is non-null.
    // In order to avoid crashing, we force the creation of the layout by setting an empty non-null
    // hint.
    TextInputLayout layout = getTextInputLayout();
    if (layout != null
        && layout.isProvidingHint()
        && super.getHint() == null
        && Build.MANUFACTURER.equalsIgnoreCase("Meizu")) {
      setHint("");
    }
  }

  @Nullable
  @Override
  public CharSequence getHint() {
    // Certain test frameworks expect the actionable element to expose its hint as a label. When
    // TextInputLayout is providing our hint, retrieve it from the parent layout.
    TextInputLayout layout = getTextInputLayout();
    if (layout != null && layout.isProvidingHint()) {
      return layout.getHint();
    }
    return super.getHint();
  }

  @Nullable
  @Override
  public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
    final InputConnection ic = super.onCreateInputConnection(outAttrs);
    if (ic != null && outAttrs.hintText == null) {
      // If we don't have a hint and our parent is a TextInputLayout, use its hint for the
      // EditorInfo. This allows us to display a hint in 'extract mode'.
      outAttrs.hintText = getHintFromLayout();
    }
    return ic;
  }

  @Nullable
  private TextInputLayout getTextInputLayout() {
    ViewParent parent = getParent();
    while (parent instanceof View) {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  @Nullable
  private CharSequence getHintFromLayout() {
    TextInputLayout layout = getTextInputLayout();
    return (layout != null) ? layout.getHint() : null;
  }
}
