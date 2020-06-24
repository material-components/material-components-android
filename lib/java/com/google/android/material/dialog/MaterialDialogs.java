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
package com.google.android.material.dialog;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.internal.ThemeEnforcement;

/**
 * Utility methods for handling Dialog Windows
 * @hide
 *
 **/
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDialogs {

  private MaterialDialogs() {};

  @NonNull
  public static InsetDrawable insetDrawable(
      @Nullable Drawable drawable, @NonNull Rect backgroundInsets) {
    return new InsetDrawable(
        drawable,
        backgroundInsets.left,
        backgroundInsets.top,
        backgroundInsets.right,
        backgroundInsets.bottom);
  }

  @NonNull
  public static Rect getDialogBackgroundInsets(
      @NonNull Context context, @AttrRes int defaultStyleAttribute, int defaultStyleResource) {
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            null,
            R.styleable.MaterialAlertDialog,
            defaultStyleAttribute,
            defaultStyleResource);

    int backgroundInsetStart =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetStart,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_start));
    int backgroundInsetTop =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetTop,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_top));

    int backgroundInsetEnd =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetEnd,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_end));
    int backgroundInsetBottom =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetBottom,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_bottom));

    attributes.recycle();

    int backgroundInsetLeft = backgroundInsetStart;
    int backgroundInsetRight = backgroundInsetEnd;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      int layoutDirection = context.getResources().getConfiguration().getLayoutDirection();
      if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        backgroundInsetLeft = backgroundInsetEnd;
        backgroundInsetRight = backgroundInsetStart;
      }
    }

    return new Rect(
        backgroundInsetLeft, backgroundInsetTop, backgroundInsetRight, backgroundInsetBottom);
  }
}
