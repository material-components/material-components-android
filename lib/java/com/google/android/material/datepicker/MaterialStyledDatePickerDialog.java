/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.datepicker;

import com.google.android.material.R;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;

/**
 * A Material version of {@link android.app.DatePickerDialog}
 *
 * @hide
 */
@RestrictTo({Scope.LIBRARY_GROUP, Scope.TESTS})
public class MaterialStyledDatePickerDialog extends DatePickerDialog {

  @AttrRes private static final int DEF_STYLE_ATTR = android.R.attr.datePickerStyle;

  @StyleRes
  private static final int DEF_STYLE_RES =
      R.style.MaterialAlertDialog_MaterialComponents_Picker_Date_Spinner;

  @NonNull private final Drawable background;
  @NonNull private final Rect backgroundInsets;

  public MaterialStyledDatePickerDialog(@NonNull Context context) {
    this(context, 0);
  }

  public MaterialStyledDatePickerDialog(@NonNull Context context, int themeResId) {
    this(context, themeResId, null, -1, -1, -1);
  }

  public MaterialStyledDatePickerDialog(
      @NonNull Context context,
      @Nullable OnDateSetListener listener,
      int year,
      int month,
      int dayOfMonth) {
    this(context, 0, listener, year, month, dayOfMonth);
  }

  public MaterialStyledDatePickerDialog(
      @NonNull Context context,
      int themeResId,
      @Nullable OnDateSetListener listener,
      int year,
      int monthOfYear,
      int dayOfMonth) {

    super(context, themeResId, listener, year, monthOfYear, dayOfMonth);
    context = getContext();

    int surfaceColor =
        MaterialAttributes.resolveOrThrow(
            getContext(), R.attr.colorSurface, getClass().getCanonicalName());

    MaterialShapeDrawable materialShapeDrawable =
        new MaterialShapeDrawable(context, null, DEF_STYLE_ATTR, DEF_STYLE_RES);
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(surfaceColor));

    backgroundInsets =
        MaterialDialogs.getDialogBackgroundInsets(context, DEF_STYLE_ATTR, DEF_STYLE_RES);
    background = MaterialDialogs.insetDrawable(materialShapeDrawable, backgroundInsets);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(background);
    getWindow()
        .getDecorView()
        .setOnTouchListener(new InsetDialogOnTouchListener(this, backgroundInsets));
  }
}
