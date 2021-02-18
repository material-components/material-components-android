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

package io.material.catalog.themeswitcher;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.widget.LinearLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;

/**
 * Theme switcher dialog that allows the user to change different theming aspects like colors and
 * shapes. Each group in the dialog has a set of possible style values that are used as theme
 * overlays, overriding attributes in the base theme like shape appearances or color attributes.
 */
public class ThemeAttributeValuesCreator {

  @NonNull
  public ThemeAttributeValues createColorPalette(
      @NonNull Context context,
      @StyleRes int themeOverlay,
      @NonNull @StyleableRes int[] themeOverlayAttrs) {
    return new ColorPalette(context, themeOverlay, themeOverlayAttrs);
  }

  @NonNull
  public ThemeAttributeValues createThemeAttributeValuesWithContentDescription(
      @StyleRes int themeOverlay, @NonNull String contentDescription) {
    return new ThemeAttributeValuesWithContentDescription(themeOverlay, contentDescription);
  }

  @NonNull
  public ThemeAttributeValues createThemeAttributeValues(
      @StyleRes int themeOverlay) {
    return new ThemeAttributeValues(themeOverlay);
  }

  /**
   * Class for customizing radio buttons in the theme switcher.
   */
  public static class ThemeAttributeValues {
    @StyleRes final int themeOverlay;

    @SuppressLint("ResourceType")
    public ThemeAttributeValues(@StyleRes int themeOverlay) {
      this.themeOverlay = themeOverlay;
    }

    public void customizeRadioButton(@NonNull AppCompatRadioButton button) {}
  }

  private static class ThemeAttributeValuesWithContentDescription extends ThemeAttributeValues {
    private final String contentDescription;

    @SuppressLint("ResourceType")
    public ThemeAttributeValuesWithContentDescription(
        @StyleRes int themeOverlay, @NonNull String contentDescription) {
      super(themeOverlay);
      this.contentDescription = contentDescription;
    }

    @Override
    public void customizeRadioButton(@NonNull AppCompatRadioButton button) {
      button.setText(contentDescription);
      LinearLayout.LayoutParams size =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      MarginLayoutParamsCompat.setMarginEnd(
          size, button.getResources().getDimensionPixelSize(R.dimen.theme_switcher_radio_spacing));
      button.setLayoutParams(size);
    }
  }

  private class ColorPalette extends ThemeAttributeValues {
    @ColorInt private final int main;

    @SuppressLint("ResourceType")
    public ColorPalette(
        Context context,
        @StyleRes int themeOverlay,
        @NonNull @StyleableRes int[] themeOverlayAttrs) {
      super(themeOverlay);
      TypedArray a = context.obtainStyledAttributes(themeOverlay, themeOverlayAttrs);
      main = a.getColor(0, Color.TRANSPARENT);

      a.recycle();
    }

    @Override
    public void customizeRadioButton(@NonNull AppCompatRadioButton button) {
      CompoundButtonCompat.setButtonTintList(
          button, ColorStateList.valueOf(convertToDisplay(main)));
    }

    @ColorInt
    private int convertToDisplay(@ColorInt int color) {
      return color == Color.WHITE ? Color.BLACK : color;
    }
  }
}
