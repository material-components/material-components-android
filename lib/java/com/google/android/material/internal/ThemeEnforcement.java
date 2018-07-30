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
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * Utility methods to check Theme compatibility with components.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class ThemeEnforcement {

  private static final int[] APPCOMPAT_CHECK_ATTRS = {R.attr.colorPrimary};
  private static final String APPCOMPAT_THEME_NAME = "Theme.AppCompat";

  private static final int[] MATERIAL_CHECK_ATTRS = {R.attr.colorPrimaryVariant};
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
   *
   * <p>If {@link R.attr#enforceTextAppearance} attribute is set to <code>true</code> and
   * textAppearanceResIndices parameter is specified and has non-negative values, this will also
   * check that a valid TextAppearance is set on this component for the text appearance resources
   * passed in.
   */
  public static TypedArray obtainStyledAttributes(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {

    // First, check for a compatible theme.
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    // Then, check that a textAppearance is set if enforceTextAppearance attribute is true
    checkTextAppearance(context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);

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
   * int[], int, int, int...)}, and use {@link com.google.android.material.resources.MaterialResources}
   * as a replacement for the functionality in {@link android.support.v7.widget.TintTypedArray}.
   *
   * <p>If {@link R.attr#enforceTextAppearance} attribute is set to <code>true</code> and
   * textAppearanceResIndices parameter is specified and has non-negative values, this will also
   * check that a valid TextAppearance is set on this component for the text appearance resources
   * passed in.
   */
  public static TintTypedArray obtainTintedStyledAttributes(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {

    // First, check for a compatible theme.
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    // Then, check that a textAppearance is set if enforceTextAppearance attribute is true
    checkTextAppearance(context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);

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
      TypedValue isMaterialTheme = new TypedValue();
      boolean resolvedValue =
          context.getTheme().resolveAttribute(R.attr.isMaterialTheme, isMaterialTheme, true);

      if (!resolvedValue
          || (isMaterialTheme.type == TypedValue.TYPE_INT_BOOLEAN && isMaterialTheme.data == 0)) {
        // If we were unable to resolve isMaterialTheme boolean attribute, or isMaterialTheme is
        // false, check for Material Theme color attributes
        checkMaterialTheme(context);
      }
    }
    checkAppCompatTheme(context);
  }

  private static void checkTextAppearance(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {
    TypedArray themeEnforcementAttrs =
        context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes);
    boolean enforceTextAppearance =
        themeEnforcementAttrs.getBoolean(R.styleable.ThemeEnforcement_enforceTextAppearance, false);

    if (!enforceTextAppearance) {
      themeEnforcementAttrs.recycle();
      return;
    }

    boolean validTextAppearance;

    if (textAppearanceResIndices == null || textAppearanceResIndices.length == 0) {
      // No custom TextAppearance attributes passed in, check android:textAppearance
      validTextAppearance =
          themeEnforcementAttrs.getResourceId(
                  R.styleable.ThemeEnforcement_android_textAppearance, -1)
              != -1;
    } else {
      // Check custom TextAppearances are valid
      validTextAppearance =
          isCustomTextAppearanceValid(
              context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);
    }

    themeEnforcementAttrs.recycle();

    if (!validTextAppearance) {
      throw new IllegalArgumentException(
          "This component requires that you specify a valid TextAppearance attribute. Update your "
              + "app theme to inherit from Theme.MaterialComponents (or a descendant).");
    }
  }

  private static boolean isCustomTextAppearanceValid(
      Context context,
      AttributeSet set,
      @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {
    TypedArray componentAttrs =
        context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
    for (int customTextAppearanceIndex : textAppearanceResIndices) {
      if (componentAttrs.getResourceId(customTextAppearanceIndex, -1) == -1) {
        componentAttrs.recycle();
        return false;
      }
    }
    componentAttrs.recycle();
    return true;
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
    for (int i = 0; i < themeAttributes.length; i++) {
      if (!a.hasValue(i)) {
        a.recycle();
        return false;
      }
    }
    a.recycle();
    return true;
  }

  private static void checkTheme(Context context, int[] themeAttributes, String themeName) {
    if (!isTheme(context, themeAttributes)) {
      throw new IllegalArgumentException(
          "The style on this component requires your app theme to be "
              + themeName
              + " (or a descendant).");
    }
  }

  /**
   * Ensures the context has theme information. This can be used in a constructor to allow the
   * defStyleAttr to use a theme overlay if one has been defined.
   */
  public static Context createThemedContext(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray a = context.obtainStyledAttributes(attrs, new int[] {android.R.attr.theme});
    int themeId = a.getResourceId(0 /* index */, 0 /* defaultVal */);
    a.recycle();

    if (themeId != 0) {
      // If the theme is set in the style of the view, just use that directly
      return context;
    }

    // Read the default style from the theme
    a = context.getTheme().obtainStyledAttributes(new int[] {defStyleAttr});
    int style = a.getResourceId(0 /* index */, 0 /* defaultVal */);
    a.recycle();

    if (style == 0) {
      // No style was set in the theme.
      return context;
    }

    // Read the android:theme attribute from the default style
    a = context.obtainStyledAttributes(style, new int[] {android.R.attr.theme});
    themeId = a.getResourceId(0 /* index */, 0 /* defaultVal */);
    a.recycle();

    if (themeId != 0 && (!(context instanceof ContextThemeWrapper)
        || ((ContextThemeWrapper) context).getThemeResId() != themeId)) {
      // If the context isn't a ContextThemeWrapper, or it is but does not have
      // the same theme as we need, wrap it in a new wrapper
      context = new ContextThemeWrapper(context, themeId);
    }
    return context;
  }
}
