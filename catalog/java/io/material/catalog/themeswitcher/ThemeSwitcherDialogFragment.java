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
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import dagger.android.support.DaggerAppCompatDialogFragment;
import javax.inject.Inject;

/**
 * Theme switcher dialog that allows the user to change different theming aspects like colors and
 * shapes. Each group in the dialog has a set of possible style values that are used as theme
 * overlays, overriding attributes in the base theme like shape appearances or color attributes.
 */
public class ThemeSwitcherDialogFragment extends DaggerAppCompatDialogFragment {

  private static final int PRIMARY_COLOR_INDEX = 0;
  private static final int SECONDARY_COLOR_INDEX = 1;
  private static final int SHAPE_CORNER_FAMILY_INDEX = 2;

  private enum RadioButtonType {
    DEFAULT,
    XML,
  }

  private enum ThemingType {
    COLOR(RadioButtonType.DEFAULT),
    SHAPE_CORNER_FAMILY(RadioButtonType.XML);

    private final RadioButtonType radioButtonType;

    ThemingType(RadioButtonType type) {
      radioButtonType = type;
    }
  }

  @StyleableRes
  private static final int[] PRIMARY_THEME_OVERLAY_ATTRS = {
    R.attr.colorPrimary, R.attr.colorPrimaryDark
  };

  @StyleableRes private static final int[] SECONDARY_THEME_OVERLAY_ATTRS = {R.attr.colorSecondary};

  @Inject ThemeSwitcherResourceProvider resourceProvider;
  private RadioGroup primaryColorGroup;
  private RadioGroup secondaryColorGroup;
  private RadioGroup shapeCornerFamilyGroup;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle bundle) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder
        .setTitle(R.string.mtrl_theme_switcher_title)
        .setView(onCreateDialogView(getActivity().getLayoutInflater()))
        .setPositiveButton(
            R.string.mtrl_theme_switcher_save,
            (dialog, which) -> {
              dialog.dismiss();
              applyThemeOverlays();
            })
        .setNegativeButton(R.string.mtrl_theme_switcher_cancel, null)
        .setNeutralButton(
            R.string.mtrl_theme_switcher_reset,
            (dialog, which) -> {
              dialog.dismiss();
              ThemeOverlayUtils.setThemeOverlays(getActivity(), 0, 0);
            });
    return builder.create();
  }

  private View onCreateDialogView(LayoutInflater layoutInflater) {
    View view = layoutInflater.inflate(R.layout.mtrl_theme_switcher_dialog, null);

    int[] currentThemeOverlays = ThemeOverlayUtils.getThemeOverlays();

    primaryColorGroup = view.findViewById(R.id.primary_colors);
    initializeThemingValues(
        primaryColorGroup,
        resourceProvider.getPrimaryColors(),
        resourceProvider.getPrimaryColorsContentDescription(),
        PRIMARY_THEME_OVERLAY_ATTRS,
        currentThemeOverlays.length >= 2 ? currentThemeOverlays[PRIMARY_COLOR_INDEX] : 0,
        ThemingType.COLOR);

    secondaryColorGroup = view.findViewById(R.id.secondary_colors);
    initializeThemingValues(
        secondaryColorGroup,
        resourceProvider.getSecondaryColors(),
        resourceProvider.getSecondaryColorsContentDescription(),
        SECONDARY_THEME_OVERLAY_ATTRS,
        currentThemeOverlays.length >= 2 ? currentThemeOverlays[SECONDARY_COLOR_INDEX] : 0,
        ThemingType.COLOR);

    shapeCornerFamilyGroup = view.findViewById(R.id.shape_families);
    initializeThemingValues(
        shapeCornerFamilyGroup,
        resourceProvider.getShapes(),
        resourceProvider.getShapesContentDescription(),
        currentThemeOverlays.length >= 3 ? currentThemeOverlays[SHAPE_CORNER_FAMILY_INDEX] : 0,
        ThemingType.SHAPE_CORNER_FAMILY);

    return view;
  }

  private void applyThemeOverlays() {
    ThemeOverlayUtils.setThemeOverlays(
        getActivity(),
        getThemeOverlayResId(primaryColorGroup),
        getThemeOverlayResId(secondaryColorGroup),
        getThemeOverlayResId(shapeCornerFamilyGroup));
  }

  private int getThemeOverlayResId(RadioGroup radioGroup) {
    if (radioGroup.getCheckedRadioButtonId() == View.NO_ID) {
      return 0;
    } else {
      ThemeAttributeValues overlayFeature =
          (ThemeAttributeValues)
              getDialog().findViewById(radioGroup.getCheckedRadioButtonId()).getTag();
      return overlayFeature.themeOverlay;
    }
  }

  private void initializeThemingValues(
      RadioGroup group,
      @ArrayRes int overlays,
      @ArrayRes int contentDescriptions,
      @StyleRes int currentThemeOverlay,
      ThemingType themingType) {
    initializeThemingValues(
        group, overlays, contentDescriptions, new int[] {}, currentThemeOverlay, themingType);
  }

  private void initializeThemingValues(
      RadioGroup group,
      @ArrayRes int overlays,
      @ArrayRes int contentDescriptions,
      @StyleableRes int[] themeOverlayAttrs,
      @StyleRes int currentThemeOverlay,
      ThemingType themingType) {
    TypedArray themingValues = getResources().obtainTypedArray(overlays);
    TypedArray contentDescriptionArray = getResources().obtainTypedArray(contentDescriptions);
    if (themingValues.length() != contentDescriptionArray.length()) {
      throw new IllegalArgumentException(
          "Feature array length doesn't match its content description array length.");
    }

    for (int i = 0; i < themingValues.length(); i++) {
      @StyleRes int valueThemeOverlay = themingValues.getResourceId(i, 0);
      ThemeAttributeValues themeAttributeValues = null;
      // Create RadioButtons for themeAttributeValues values
      switch (themingType) {
        case COLOR:
          themeAttributeValues = new ColorPalette(valueThemeOverlay, themeOverlayAttrs);
          break;
        case SHAPE_CORNER_FAMILY:
          themeAttributeValues = new ThemeAttributeValues(valueThemeOverlay);
          break;
      }

      // Expect the radio group to have a RadioButton as child for each themeAttributeValues value.
      AppCompatRadioButton button =
          themingType.radioButtonType == RadioButtonType.XML
              ? ((AppCompatRadioButton) group.getChildAt(i))
              : createCompatRadioButton(group, contentDescriptionArray.getString(i));

      button.setTag(themeAttributeValues);
      themeAttributeValues.customizeRadioButton(button);

      if (themeAttributeValues.themeOverlay == currentThemeOverlay) {
        group.check(button.getId());
      }
    }

    themingValues.recycle();
    contentDescriptionArray.recycle();
  }

  @NonNull
  private AppCompatRadioButton createCompatRadioButton(
      RadioGroup group, String contentDescription) {
    AppCompatRadioButton button = new AppCompatRadioButton(getContext());
    button.setContentDescription(contentDescription);
    group.addView(button);
    return button;
  }

  private static class ThemeAttributeValues {
    @StyleRes private final int themeOverlay;

    @SuppressLint("ResourceType")
    public ThemeAttributeValues(@StyleRes int themeOverlay) {
      this.themeOverlay = themeOverlay;
    }

    public void customizeRadioButton(AppCompatRadioButton button) {}
  }

  private class ColorPalette extends ThemeAttributeValues {
    @ColorInt private final int main;

    @SuppressLint("ResourceType")
    public ColorPalette(@StyleRes int themeOverlay, @StyleableRes int[] themeOverlayAttrs) {
      super(themeOverlay);
      TypedArray a = getContext().obtainStyledAttributes(themeOverlay, themeOverlayAttrs);
      main = a.getColor(0, Color.TRANSPARENT);

      a.recycle();
    }

    @Override
    public void customizeRadioButton(AppCompatRadioButton button) {
      CompoundButtonCompat.setButtonTintList(
          button, ColorStateList.valueOf(convertToDisplay(main)));
    }

    @ColorInt
    private int convertToDisplay(@ColorInt int color) {
      return color == Color.WHITE ? Color.BLACK : color;
    }
  }
}
