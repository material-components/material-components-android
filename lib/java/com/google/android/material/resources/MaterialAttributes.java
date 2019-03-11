/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.material.resources;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.TypedValue;
import android.view.View;

/**
 * Utility methods to work with attributes.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialAttributes {

  public static TypedValue resolveAttributeOrThrow(
      View componentView, @AttrRes int attributeResId) {
    return resolveAttributeOrThrow(
        componentView.getContext(), attributeResId, componentView.getClass().getCanonicalName());
  }

  public static boolean resolveBooleanAttributeOrThrow(
      Context context, @AttrRes int attributeResId, String errorMessageComponent) {
    return resolveAttributeOrThrow(context, attributeResId, errorMessageComponent).data != 0;
  }

  public static TypedValue resolveAttributeOrThrow(
      Context context, @AttrRes int attributeResId, String errorMessageComponent) {
    TypedValue typedValue = resolveAttribute(context, attributeResId);
    if (typedValue == null) {
      String errorMessage =
          "%1$s requires a value for the %2$s attribute to be set in your app theme. "
              + "You can either set the attribute in your theme or "
              + "update your theme to inherit from Theme.MaterialComponents (or a descendant).";
      throw new IllegalArgumentException(
          String.format(
              errorMessage,
              errorMessageComponent,
              context.getResources().getResourceName(attributeResId)));
    }
    return typedValue;
  }

  @Nullable
  public static TypedValue resolveAttribute(Context context, @AttrRes int attributeResId) {
    TypedValue typedValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
      return typedValue;
    }
    return null;
  }
}
