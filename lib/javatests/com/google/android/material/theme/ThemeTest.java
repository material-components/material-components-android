/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.theme;

import com.google.android.material.R;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.testing.ResourceNameLookup;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for the Material themes. */
@RunWith(ParameterizedRobolectricTestRunner.class)
@DoNotInstrument
public class ThemeTest {

  /** These attributes pairs should be same in Material Light theme. */
  private static final ImmutableList<int[]> MATERIAL_INTRA_LIGHT_THEME_ATTRIBUTES =
      ImmutableList.of(
          new int[] {R.attr.colorPrimarySurface, R.attr.colorPrimary},
          new int[] {R.attr.colorOnPrimarySurface, R.attr.colorOnPrimary});
  /** These attributes pairs should be same in Material Dark theme. */
  private static final ImmutableList<int[]> MATERIAL_INTRA_DARK_THEME_ATTRIBUTES =
      ImmutableList.of(
          new int[] {R.attr.colorPrimarySurface, R.attr.colorSurface},
          new int[] {R.attr.colorOnPrimarySurface, R.attr.colorOnSurface});

  /**
   * These are color attributes that all M3 themes should have.
   *
   * <p>All M3 Light themes should match {@link R.style#Theme_Material3_Light} and all M3 Dark
   * themes should match {@link R.style#Theme_Material3_Dark}.
   */
  private static final ImmutableList<Integer> M3_ACCENT_COLOR_ATTRIBUTES =
      ImmutableList.of(
          // Primary colors.
          R.attr.colorPrimary,
          R.attr.colorPrimaryDark,
          R.attr.colorOnPrimary,
          R.attr.colorPrimaryInverse,
          R.attr.colorPrimaryContainer,
          R.attr.colorOnPrimaryContainer,
          // Secondary colors.
          R.attr.colorSecondary,
          R.attr.colorOnSecondary,
          R.attr.colorSecondaryContainer,
          R.attr.colorOnSecondaryContainer,
          // Tertiary colors.
          R.attr.colorTertiary,
          R.attr.colorOnTertiary,
          R.attr.colorTertiaryContainer,
          R.attr.colorOnTertiaryContainer);

  /**
   * These are color attributes that all themes and theme overlays should have.
   *
   * <p>All M3 Light themes should match {@link R.style#Theme_Material3_Light} and all M3 Dark
   * themes should match {@link R.style#Theme_Material3_Dark}.
   */
  private static final ImmutableList<Integer> M3_ADDITIONAL_COLOR_ATTRIBUTES =
      ImmutableList.of(
          // Background colors.
          android.R.attr.colorBackground,
          R.attr.colorOnBackground,
          // Surface colors.
          R.attr.colorSurface,
          R.attr.colorOnSurface,
          R.attr.colorSurfaceVariant,
          R.attr.colorOnSurfaceVariant,
          R.attr.colorSurfaceInverse,
          R.attr.colorOnSurfaceInverse,
          // Outline color.
          R.attr.colorOutline,
          // Error colors.
          R.attr.colorError,
          R.attr.colorOnError,
          R.attr.colorErrorContainer,
          R.attr.colorOnErrorContainer);

  /**
   * These are deprecated color attributes in M3. We only have them in themes but not theme
   * overlays.
   */
  private static final ImmutableList<Integer> M3_DEPRECATED_COLOR_ATTRIBUTES =
      ImmutableList.of(R.attr.colorPrimaryVariant, R.attr.colorSecondaryVariant);

  private static final ImmutableList<Integer> M3_FULL_COLOR_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(M3_ACCENT_COLOR_ATTRIBUTES)
          .addAll(M3_ADDITIONAL_COLOR_ATTRIBUTES)
          .addAll(M3_DEPRECATED_COLOR_ATTRIBUTES)
          .build();

  private static final ImmutableList<Integer> M3_ACTIVE_TYPOGRAPHY_ATTRIBUTES =
      ImmutableList.of(
          R.attr.textAppearanceDisplayLarge,
          R.attr.textAppearanceDisplayMedium,
          R.attr.textAppearanceDisplaySmall,
          R.attr.textAppearanceHeadlineLarge,
          R.attr.textAppearanceHeadlineMedium,
          R.attr.textAppearanceHeadlineSmall,
          R.attr.textAppearanceTitleLarge,
          R.attr.textAppearanceTitleMedium,
          R.attr.textAppearanceTitleSmall,
          R.attr.textAppearanceBodyLarge,
          R.attr.textAppearanceBodyMedium,
          R.attr.textAppearanceBodySmall,
          R.attr.textAppearanceLabelLarge,
          R.attr.textAppearanceLabelMedium,
          R.attr.textAppearanceLabelSmall);

  private static final ImmutableList<Integer> M3_DEPRECATED_TYPOGRAPHY_ATTRIBUTES =
      ImmutableList.of(
          R.attr.textAppearanceHeadline1,
          R.attr.textAppearanceHeadline2,
          R.attr.textAppearanceHeadline3,
          R.attr.textAppearanceHeadline4,
          R.attr.textAppearanceHeadline5,
          R.attr.textAppearanceHeadline6,
          R.attr.textAppearanceSubtitle1,
          R.attr.textAppearanceSubtitle2,
          R.attr.textAppearanceBody1,
          R.attr.textAppearanceBody2,
          R.attr.textAppearanceCaption,
          R.attr.textAppearanceButton,
          R.attr.textAppearanceOverline);

  private static final ImmutableList<Integer> DEFAULT_FRAMEWORK_TEXT_STYLE_ATTRIBUTES =
      ImmutableList.of(
          android.R.attr.textAppearance,
          android.R.attr.textAppearanceInverse,
          android.R.attr.textAppearanceLarge,
          android.R.attr.textAppearanceLargeInverse,
          android.R.attr.textAppearanceMedium,
          android.R.attr.textAppearanceMediumInverse,
          android.R.attr.textAppearanceSmall,
          android.R.attr.textAppearanceSmallInverse);

  /** These are typography styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_FULL_TYPOGRAPHY_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(M3_ACTIVE_TYPOGRAPHY_ATTRIBUTES)
          .addAll(M3_DEPRECATED_TYPOGRAPHY_ATTRIBUTES)
          .addAll(DEFAULT_FRAMEWORK_TEXT_STYLE_ATTRIBUTES)
          .build();

  /** These are motion styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_MOTION_STYLE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.motionEasingStandard,
          R.attr.motionEasingEmphasized,
          R.attr.motionEasingDecelerated,
          R.attr.motionEasingAccelerated,
          R.attr.motionEasingLinear,
          R.attr.motionDurationShort1,
          R.attr.motionDurationShort2,
          R.attr.motionDurationMedium1,
          R.attr.motionDurationMedium2,
          R.attr.motionDurationLong1,
          R.attr.motionDurationLong2,
          R.attr.motionPath);

  /** These are widget styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_COMMON_WIDGET_STYLE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.appBarLayoutStyle,
          R.attr.badgeStyle,
          R.attr.borderlessButtonStyle,
          R.attr.bottomAppBarStyle,
          R.attr.bottomNavigationStyle,
          R.attr.buttonBarButtonStyle,
          R.attr.checkboxStyle,
          R.attr.chipStyle,
          R.attr.chipGroupStyle,
          R.attr.chipStandaloneStyle,
          R.attr.circularProgressIndicatorStyle,
          R.attr.collapsingToolbarLayoutStyle,
          R.attr.collapsingToolbarLayoutMediumStyle,
          R.attr.collapsingToolbarLayoutLargeStyle,
          R.attr.extendedFloatingActionButtonStyle,
          R.attr.extendedFloatingActionButtonPrimaryStyle,
          R.attr.extendedFloatingActionButtonSecondaryStyle,
          R.attr.extendedFloatingActionButtonTertiaryStyle,
          R.attr.extendedFloatingActionButtonSurfaceStyle,
          R.attr.floatingActionButtonStyle,
          R.attr.floatingActionButtonPrimaryStyle,
          R.attr.floatingActionButtonSecondaryStyle,
          R.attr.floatingActionButtonTertiaryStyle,
          R.attr.floatingActionButtonSurfaceStyle,
          R.attr.floatingActionButtonLargeStyle,
          R.attr.floatingActionButtonLargePrimaryStyle,
          R.attr.floatingActionButtonLargeSecondaryStyle,
          R.attr.floatingActionButtonLargeTertiaryStyle,
          R.attr.floatingActionButtonLargeSurfaceStyle,
          R.attr.linearProgressIndicatorStyle,
          R.attr.materialButtonOutlinedStyle,
          R.attr.materialButtonStyle,
          R.attr.materialCardViewStyle,
          R.attr.materialCardViewOutlinedStyle,
          R.attr.materialCardViewElevatedStyle,
          R.attr.radioButtonStyle,
          R.attr.sliderStyle,
          R.attr.snackbarStyle,
          R.attr.snackbarButtonStyle,
          R.attr.snackbarTextViewStyle,
          R.attr.switchStyle,
          R.attr.tabStyle,
          R.attr.tabSecondaryStyle,
          R.attr.textInputStyle,
          R.attr.textInputOutlinedStyle,
          R.attr.textInputFilledStyle,
          R.attr.textInputOutlinedDenseStyle,
          R.attr.textInputFilledDenseStyle,
          R.attr.textInputOutlinedExposedDropdownMenuStyle,
          R.attr.textInputFilledExposedDropdownMenuStyle,
          R.attr.toolbarStyle,
          R.attr.toolbarSurfaceStyle);

  /**
   * These are all the attributes where full themes should match {@link
   * R.style#Theme_Material3_Light} or {@link R.style#Theme_Material3_Dark}.
   */
  private static final ImmutableList<Integer> M3_FULL_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(M3_FULL_COLOR_ATTRIBUTES)
          .addAll(M3_FULL_TYPOGRAPHY_ATTRIBUTES)
          .addAll(M3_MOTION_STYLE_ATTRIBUTES)
          .addAll(M3_COMMON_WIDGET_STYLE_ATTRIBUTES)
          .build();

  @Parameters(name = "{0}")
  public static ImmutableList<Object[]> getTestData() {
    return ImmutableList.<Object[]>builder()
        // Attributes inside the Material Light theme.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Light, MATERIAL_INTRA_LIGHT_THEME_ATTRIBUTES))
        // Attribtues inside the Material Dark theme.
        .addAll(
            createTestData(R.style.Theme_MaterialComponents, MATERIAL_INTRA_DARK_THEME_ATTRIBUTES))
        // M3 Dark Themes and Theme Overlays
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark_NoActionBar,
                R.style.Theme_Material3_Dark,
                M3_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark_Dialog,
                R.style.Theme_Material3_Dark,
                M3_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.ThemeOverlay_Material3_Dark,
                R.style.Theme_Material3_Dark,
                M3_ADDITIONAL_COLOR_ATTRIBUTES))
        // M3 Light Themes and Theme Overlays
        .addAll(
            createTestData(
                R.style.Theme_Material3_Light_NoActionBar,
                R.style.Theme_Material3_Light,
                M3_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_Material3_Light_Dialog,
                R.style.Theme_Material3_Light,
                M3_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.ThemeOverlay_Material3_Light,
                R.style.Theme_Material3_Light,
                M3_ADDITIONAL_COLOR_ATTRIBUTES))
        // Compare M3 Light and Dark themes - they should have the same common widget styles.
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark,
                R.style.Theme_Material3_Light,
                M3_COMMON_WIDGET_STYLE_ATTRIBUTES))
        // Compare M3 Light and Dark themes - they should have the same typography styles.
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark,
                R.style.Theme_Material3_Light,
                M3_FULL_TYPOGRAPHY_ATTRIBUTES))
        // Compare M3 Light and Dark themes - they should have the same motion styles.
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark,
                R.style.Theme_Material3_Light,
                M3_MOTION_STYLE_ATTRIBUTES))
        .build();
  }

  @Parameter(0)
  public String testName;

  @Parameter(1)
  public int theme;

  @Parameter(2)
  public int attribute;

  @Parameter(3)
  public int comparisonTheme;

  @Parameter(4)
  public int comparisonAttribute;

  private static final ImmutableMap<Integer, String> RESOURCE_NAME_MAP =
      ResourceNameLookup.createResourceNameMap(R.style.class, R.attr.class, android.R.attr.class);

  private static List<Object[]> createTestData(int theme, ImmutableList<int[]> attributes) {
    List<Object[]> testData = new ArrayList<>();
    for (int[] attr : attributes) {
      testData.add(getTestParameters(theme, theme, attr));
    }
    Iterables.removeIf(testData, Predicates.isNull());
    return testData;
  }

  private static List<Object[]> createTestData(
      int theme, int comparisonTheme, ImmutableList<Integer> attributes) {
    List<Object[]> testData = new ArrayList<>();
    for (int attr : attributes) {
      testData.add(getTestParameters(theme, comparisonTheme, attr));
    }
    Iterables.removeIf(testData, Predicates.isNull());
    return testData;
  }

  private static Object[] getTestParameters(int theme, int comparisonTheme, int... attributes) {
    if (attributes.length == 0) {
      return null;
    }
    if (attributes.length > 2) {
      throw new IllegalArgumentException("Cannot compare more than 2 attributes in a test.");
    }
    int attr = attributes[0];
    int comparisonAttr = attributes[0];
    if (attributes.length > 1) {
      comparisonAttr = attributes[1];
    }
    return new Object[] {
      String.format(
          "%s %s expected to match %s %s",
          RESOURCE_NAME_MAP.get(theme),
          RESOURCE_NAME_MAP.get(attr),
          RESOURCE_NAME_MAP.get(comparisonTheme),
          RESOURCE_NAME_MAP.get(comparisonAttr)),
      theme,
      attr,
      comparisonTheme,
      comparisonAttr
    };
  }

  @Test
  public void testAttribute() {
    assertThat(resolveAttribute(theme, attribute, "missing in theme"))
        .isEqualTo(
            resolveAttribute(comparisonTheme, comparisonAttribute, "missing in comparison theme"));
  }

  @SuppressWarnings("RestrictTo")
  private static int resolveAttribute(int theme, int attribute, String error) {
    return MaterialAttributes.resolveOrThrow(
        new ContextThemeWrapper(getApplicationContext(), theme), attribute, error);
  }
}
