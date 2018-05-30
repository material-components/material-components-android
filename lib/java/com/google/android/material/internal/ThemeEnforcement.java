/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.internal;

import com.google.android.material.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleRes;
import android.support.annotation.StyleableRes;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;

/**
 * Utility methods to check Theme compatibility with components.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class ThemeEnforcement {

  private static final int[] APPCOMPAT_CHECK_ATTRS = {R.attr.colorPrimary};
  private static final String APPCOMPAT_THEME_NAME = "Theme.AppCompat";

  private static final int[] MATERIAL_CHECK_ATTRS = {R.attr.colorSecondary};
  private static final String MATERIAL_THEME_NAME = "Theme.MaterialComponents";

  private ThemeEnforcement() {}

  /**
   * Safely retrieve styled attribute information in this Context's theme, after checking whether
   * the theme is compatible with the component's given style.
   *
   * <p>Set a component's {@link R.attr#enforceMaterialTheme enforceMaterialTheme} attribute to
   * <code>true</code> to ensure that the Context's theme inherits from {@link
   * R.style#Theme_MaterialComponents Theme.MaterialComponents}. For example, you'll want to do this
   * if the component uses a new attribute defined in <code>Theme.MaterialComponents</code> like
   * {@link R.attr#colorSecondary colorSecondary}.
   */
  public static TypedArray obtainStyledAttributes(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    return obtainStyledAttributes(context, set, attrs, defStyleAttr, defStyleRes, false);
  }

  /**
   * Safely retrieve styled attribute information in this Context's theme, after checking whether
   * the theme is compatible with the component's given style. Also checks whether a textAppearance
   * is set on the component if <code>enforceTextAppearance</code> is set to <code>true</code>.
   *
   * <p>Set a component's {@link R.attr#enforceMaterialTheme enforceMaterialTheme} attribute to
   * <code>true</code> to ensure that the Context's theme inherits from {@link
   * R.style#Theme_MaterialComponents Theme.MaterialComponents}. For example, you'll want to do this
   * if the component uses a new attribute defined in <code>Theme.MaterialComponents</code> like
   * {@link R.attr#colorSecondary colorSecondary}.
   */
  public static TypedArray obtainStyledAttributes(
      Context context,
      AttributeSet set,
      int[] attrs,
      int defStyleAttr,
      int defStyleRes,
      boolean enforceTextAppearance) {

    // First, check for a compatible theme.
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    // Then, check that a textAppearance is set if enforceTextAppearance is true
    checkTextAppearance(context, set, defStyleAttr, defStyleRes);

    // Then, safely retrieve the styled attribute information.
    return context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Safely retrieve styled attribute information in this Context's theme using {@link
   * android.support.v7.widget.TintTypedArray}, after checking whether the theme is compatible with
   * the component's given style.
   *
   * <p>Set a component's {@link R.attr#enforceMaterialTheme enforceMaterialTheme} attribute to
   * <code>true</code> to ensure that the Context's theme inherits from {@link
   * R.style#Theme_MaterialComponents Theme.MaterialComponents}. For example, you'll want to do this
   * if the component uses a new attribute defined in <code>Theme.MaterialComponents</code> like
   * {@link R.attr#colorSecondary colorSecondary}.
   *
   * <p>New components should prefer to use {@link #obtainStyledAttributes(Context, AttributeSet,
   * int[], int, int)}, and use {@link com.google.android.material.resources.MaterialResources} as a
   * replacement for the functionality in {@link android.support.v7.widget.TintTypedArray}.
   */
  public static TintTypedArray obtainTintedStyledAttributes(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    // First, check for a compatible theme.
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    // Then, check that a textAppearance is set if enforceTextAppearance is true
    checkTextAppearance(context, set, defStyleAttr, defStyleRes);

    // Then, safely retrieve the styled attribute information.
    return TintTypedArray.obtainStyledAttributes(context, set, attrs, defStyleAttr, defStyleRes);
  }

  private static void checkCompatibleTheme(
      Context context, AttributeSet set, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes);
    boolean enforceMaterialTheme =
        a.getBoolean(R.styleable.ThemeEnforcement_enforceMaterialTheme, false);
    a.recycle();

    if (enforceMaterialTheme) {
      checkMaterialTheme(context);
    }
    checkAppCompatTheme(context);
  }

  private static void checkTextAppearance(
      Context context, AttributeSet set, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes);
    boolean enforceTextAppearance =
        a.getBoolean(R.styleable.ThemeEnforcement_enforceTextAppearance, false);

    if (enforceTextAppearance) {
      int textAppearanceResId =
          a.getResourceId(R.styleable.ThemeEnforcement_android_textAppearance, -1);
      a.recycle();

      if (textAppearanceResId == -1) {
        throw new IllegalArgumentException(
            "This component requires that you specify a valid android:textAppearance attribute.");
      }
    } else {
      a.recycle();
    }
  }

  public static void checkAppCompatTheme(Context context) {
    checkTheme(context, APPCOMPAT_CHECK_ATTRS, APPCOMPAT_THEME_NAME);
  }

  public static void checkMaterialTheme(Context context) {
    checkTheme(context, MATERIAL_CHECK_ATTRS, MATERIAL_THEME_NAME);
  }

  public static boolean isAppCompatTheme(Context context) {
    return isTheme(context, APPCOMPAT_CHECK_ATTRS);
  }

  public static boolean isMaterialTheme(Context context) {
    return isTheme(context, MATERIAL_CHECK_ATTRS);
  }

  private static boolean isTheme(Context context, int[] themeAttributes) {
    TypedArray a = context.obtainStyledAttributes(themeAttributes);
    final boolean success = a.hasValue(0);
    a.recycle();

    return success;
  }

  private static void checkTheme(Context context, int[] themeAttributes, String themeName) {
    if (!isTheme(context, themeAttributes)) {
      throw new IllegalArgumentException(
          "The style on this component requires your app theme to be "
              + themeName
              + " (or a descendant).");
    }
  }
}
