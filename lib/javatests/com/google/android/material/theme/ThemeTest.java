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

import com.google.android.material.test.R;

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
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for the Material themes. */
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
@DoNotInstrument
public class ThemeTest {

  private static final ImmutableList<Integer> MATERIAL_DEPRECATED_MOTION_ATTRIBUTES =
      ImmutableList.of(
          R.attr.motionEasingStandard,
          R.attr.motionEasingEmphasized,
          R.attr.motionEasingDecelerated,
          R.attr.motionEasingAccelerated,
          R.attr.motionEasingLinear);
  private static final ImmutableList<Integer> MATERIAL_ACTIVE_MOTION_ATTRIBUTES =
      ImmutableList.of(
          R.attr.motionDurationShort1,
          R.attr.motionDurationShort2,
          R.attr.motionDurationMedium1,
          R.attr.motionDurationMedium2,
          R.attr.motionDurationLong1,
          R.attr.motionDurationLong2,
          R.attr.motionPath);

  private static final ImmutableList<Integer> MATERIAL_MOTION_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_DEPRECATED_MOTION_ATTRIBUTES)
          .addAll(MATERIAL_ACTIVE_MOTION_ATTRIBUTES)
          .build();

  private static final ImmutableList<Integer> DEPRECATED_TYPOGRAPHY_ATTRIBUTES =
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
   * These are color attributes that all themes, including bridge themes, should have.
   *
   * <p>All Light themes should match {@code Theme.MaterialComponents.Light} and all Dark themes
   * should match {@code Theme.MaterialComponents}.
   */
  private static final ImmutableList<Integer> MATERIAL_BRIDGE_COLOR_ATTRIBUTES =
      ImmutableList.of(
          R.attr.colorPrimaryVariant,
          R.attr.colorSecondary,
          R.attr.colorSecondaryVariant,
          R.attr.colorSurface,
          R.attr.colorOnPrimary,
          R.attr.colorOnSecondary,
          R.attr.colorOnBackground,
          R.attr.colorOnError,
          R.attr.colorOnSurface,
          R.attr.colorOnPrimarySurface);

  /**
   * These are color attributes that all full themes should have in addition to {@link
   * #MATERIAL_BRIDGE_COLOR_ATTRIBUTES}.
   *
   * <p>All Light full themes should match {@code Theme.MaterialComponents.Light} and all Dark
   * themes should match {@code Theme.MaterialComponents}.
   */
  private static final ImmutableList<Integer> MATERIAL_NON_BRIDGE_COLOR_ATTRIBUTES =
      ImmutableList.of(
          R.attr.colorPrimary,
          R.attr.colorPrimaryDark,
          R.attr.colorAccent,
          android.R.attr.colorBackground,
          R.attr.colorError);

  /**
   * There are color attributes that all theme overlays should have.
   *
   * <p>All Light theme overlays should match {@code Theme.MaterialComponents.Light} and all Dark
   * theme overlays should match {@code Theme.MaterialComponents}.
   */
  private static final ImmutableList<Integer> MATERIAL_OVERLAY_COLOR_ATTRIBUTES =
      ImmutableList.of(
          android.R.attr.colorBackground,
          R.attr.colorOnBackground,
          R.attr.colorSurface,
          R.attr.colorOnSurface,
          R.attr.colorError,
          R.attr.colorOnError);

  /**
   * These are all the color attributes that all full themes should have.
   *
   * <p>All Light full themes should match {@code Theme.MaterialComponents.Light} and all Dark full
   * themes should match {@code Theme.MaterialComponents}.
   */
  private static final ImmutableList<Integer> MATERIAL_FULL_COLOR_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_BRIDGE_COLOR_ATTRIBUTES)
          .addAll(MATERIAL_NON_BRIDGE_COLOR_ATTRIBUTES)
          .build();

  /**
   * These are widget styles that should be the same for *all* bridge and full themes (Light and
   * Dark).
   */
  private static final ImmutableList<Integer> MATERIAL_BRIDGE_WIDGET_STYLE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.badgeStyle,
          R.attr.chipStyle,
          R.attr.chipGroupStyle,
          R.attr.chipStandaloneStyle,
          R.attr.circularProgressIndicatorStyle,
          R.attr.extendedFloatingActionButtonStyle,
          R.attr.linearProgressIndicatorStyle,
          R.attr.materialButtonStyle,
          R.attr.materialButtonOutlinedStyle,
          R.attr.materialButtonToggleGroupStyle,
          R.attr.materialCardViewStyle,
          R.attr.navigationRailStyle,
          R.attr.sliderStyle);

  /**
   * These are widget styles that should be the same for *all* full themes (Light and Dark) in
   * addition to the {@link #MATERIAL_BRIDGE_WIDGET_STYLE_ATTRIBUTES}.
   */
  private static final ImmutableList<Integer> MATERIAL_NON_BRIDGE_WIDGET_STYLE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.borderlessButtonStyle,
          R.attr.bottomNavigationStyle,
          R.attr.checkboxStyle,
          R.attr.floatingActionButtonStyle,
          R.attr.listPopupWindowStyle,
          R.attr.navigationViewStyle,
          R.attr.popupMenuStyle,
          R.attr.radioButtonStyle,
          R.attr.snackbarStyle,
          R.attr.snackbarButtonStyle,
          R.attr.snackbarTextViewStyle,
          R.attr.switchStyle,
          R.attr.tabStyle,
          R.attr.textInputStyle,
          R.attr.textInputOutlinedStyle,
          R.attr.textInputFilledStyle,
          R.attr.textInputOutlinedDenseStyle,
          R.attr.textInputFilledDenseStyle,
          R.attr.textInputOutlinedExposedDropdownMenuStyle,
          R.attr.textInputFilledExposedDropdownMenuStyle,
          R.attr.toolbarStyle);

  /** These are all the widget style attributes that should be the same for *all* full themes. */
  private static final ImmutableList<Integer> MATERIAL_COMMON_WIDGET_STYLE_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_BRIDGE_WIDGET_STYLE_ATTRIBUTES)
          .addAll(MATERIAL_NON_BRIDGE_WIDGET_STYLE_ATTRIBUTES)
          .build();

  /**
   * These are all the attributes where the light bridge theme should match the light full theme and
   * the dark bridge theme should match the dark full theme.
   */
  private static final ImmutableList<Integer> MATERIAL_BRIDGE_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_BRIDGE_COLOR_ATTRIBUTES)
          .addAll(MATERIAL_BRIDGE_WIDGET_STYLE_ATTRIBUTES)
          .addAll(MATERIAL_MOTION_ATTRIBUTES)
          .addAll(DEPRECATED_TYPOGRAPHY_ATTRIBUTES)
          .build();

  /**
   * These are all the attributes where full themes should match {@code Theme.MaterialComponents} or
   * {@code Theme.MaterialComponents.Light}.
   */
  private static final ImmutableList<Integer> MATERIAL_FULL_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_FULL_COLOR_ATTRIBUTES)
          .addAll(MATERIAL_MOTION_ATTRIBUTES)
          .addAll(MATERIAL_COMMON_WIDGET_STYLE_ATTRIBUTES)
          .build();

  /**
   * These are color attributes that all M3 themes should have.
   *
   * <p>All M3 Light themes should match {@code Theme.Material3.Light} and all M3 Dark themes should
   * match {@code Theme.Material3.Dark}.
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
          R.attr.colorPrimaryFixed,
          R.attr.colorPrimaryFixedDim,
          R.attr.colorOnPrimaryFixed,
          R.attr.colorOnPrimaryFixedVariant,
          // Secondary colors.
          R.attr.colorSecondary,
          R.attr.colorOnSecondary,
          R.attr.colorSecondaryContainer,
          R.attr.colorOnSecondaryContainer,
          R.attr.colorSecondaryFixed,
          R.attr.colorSecondaryFixedDim,
          R.attr.colorOnSecondaryFixed,
          R.attr.colorOnSecondaryFixedVariant,
          // Tertiary colors.
          R.attr.colorTertiary,
          R.attr.colorOnTertiary,
          R.attr.colorTertiaryContainer,
          R.attr.colorOnTertiaryContainer,
          R.attr.colorTertiaryFixed,
          R.attr.colorTertiaryFixedDim,
          R.attr.colorOnTertiaryFixed,
          R.attr.colorOnTertiaryFixedVariant);

  /**
   * These are color attributes that all themes and theme overlays should have.
   *
   * <p>All M3 Light themes should match {@code Theme.Material3.Light} and all M3 Dark themes should
   * match {@code Theme.Material3.Dark}.
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
          R.attr.colorSurfaceBright,
          R.attr.colorSurfaceDim,
          R.attr.colorSurfaceContainer,
          R.attr.colorSurfaceContainerHigh,
          R.attr.colorSurfaceContainerHighest,
          R.attr.colorSurfaceContainerLow,
          R.attr.colorSurfaceContainerLowest,
          // Outline color.
          R.attr.colorOutline,
          R.attr.colorOutlineVariant,
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

  private static final ImmutableList<Integer> DEFAULT_FRAMEWORK_TEXT_STYLE_ATTRIBUTES =
      ImmutableList.of(
          android.R.attr.textAppearance,
          android.R.attr.textAppearanceInverse,
          android.R.attr.textAppearanceLarge,
          android.R.attr.textAppearanceLargeInverse,
          android.R.attr.textAppearanceMedium,
          android.R.attr.textAppearanceMediumInverse,
          android.R.attr.textAppearanceSmall,
          android.R.attr.textAppearanceSmallInverse,
          android.R.attr.textAppearanceListItem,
          android.R.attr.textAppearanceListItemSmall,
          android.R.attr.textAppearanceListItemSecondary,
          R.attr.textAppearanceListItem,
          R.attr.textAppearanceListItemSmall,
          R.attr.textAppearanceListItemSecondary);

  /** These are typography styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_FULL_TYPOGRAPHY_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(M3_ACTIVE_TYPOGRAPHY_ATTRIBUTES)
          .addAll(DEPRECATED_TYPOGRAPHY_ATTRIBUTES)
          .addAll(DEFAULT_FRAMEWORK_TEXT_STYLE_ATTRIBUTES)
          .build();

  private static final ImmutableList<Integer> M3_ACTIVE_MOTION_ATTRIBUTES =
      ImmutableList.of(
          R.attr.motionSpringFastSpatial,
          R.attr.motionSpringFastEffects,
          R.attr.motionSpringDefaultSpatial,
          R.attr.motionSpringDefaultEffects,
          R.attr.motionSpringSlowSpatial,
          R.attr.motionSpringSlowEffects,
          R.attr.motionEasingStandardInterpolator,
          R.attr.motionEasingStandardAccelerateInterpolator,
          R.attr.motionEasingStandardDecelerateInterpolator,
          R.attr.motionEasingEmphasizedInterpolator,
          R.attr.motionEasingEmphasizedAccelerateInterpolator,
          R.attr.motionEasingEmphasizedDecelerateInterpolator,
          R.attr.motionEasingLinearInterpolator,
          R.attr.motionDurationShort1,
          R.attr.motionDurationShort2,
          R.attr.motionDurationShort3,
          R.attr.motionDurationShort4,
          R.attr.motionDurationMedium1,
          R.attr.motionDurationMedium2,
          R.attr.motionDurationMedium3,
          R.attr.motionDurationMedium4,
          R.attr.motionDurationLong1,
          R.attr.motionDurationLong2,
          R.attr.motionDurationLong3,
          R.attr.motionDurationLong4,
          R.attr.motionDurationExtraLong1,
          R.attr.motionDurationExtraLong2,
          R.attr.motionDurationExtraLong3,
          R.attr.motionDurationExtraLong4,
          R.attr.motionPath);

  private static final ImmutableList<Integer> M3_FULL_MOTION_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(MATERIAL_DEPRECATED_MOTION_ATTRIBUTES)
          .addAll(M3_ACTIVE_MOTION_ATTRIBUTES)
          .build();

  /** These are widget styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_COMMON_WIDGET_STYLE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.appBarLayoutStyle,
          R.attr.badgeStyle,
          R.attr.borderlessButtonStyle,
          R.attr.bottomAppBarStyle,
          R.attr.bottomNavigationStyle,
          R.attr.bottomSheetDragHandleStyle,
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
          R.attr.dockedToolbarStyle,
          R.attr.dockedToolbarVibrantStyle,
          R.attr.floatingActionButtonStyle,
          R.attr.floatingActionButtonPrimaryStyle,
          R.attr.floatingActionButtonSecondaryStyle,
          R.attr.floatingActionButtonTertiaryStyle,
          R.attr.floatingActionButtonSurfaceStyle,
          R.attr.floatingActionButtonSmallStyle,
          R.attr.floatingActionButtonSmallPrimaryStyle,
          R.attr.floatingActionButtonSmallSecondaryStyle,
          R.attr.floatingActionButtonSmallTertiaryStyle,
          R.attr.floatingActionButtonSmallSurfaceStyle,
          R.attr.floatingActionButtonLargeStyle,
          R.attr.floatingActionButtonLargePrimaryStyle,
          R.attr.floatingActionButtonLargeSecondaryStyle,
          R.attr.floatingActionButtonLargeTertiaryStyle,
          R.attr.floatingActionButtonLargeSurfaceStyle,
          R.attr.floatingToolbarStyle,
          R.attr.floatingToolbarVibrantStyle,
          R.attr.linearProgressIndicatorStyle,
          R.attr.listItemLayoutStyle,
          R.attr.listItemCardViewStyle,
          R.attr.listItemCardViewSegmentedStyle,
          R.attr.materialIconButtonStyle,
          R.attr.materialButtonOutlinedStyle,
          R.attr.materialButtonStyle,
          R.attr.materialCardViewStyle,
          R.attr.materialCardViewOutlinedStyle,
          R.attr.materialCardViewFilledStyle,
          R.attr.materialCardViewElevatedStyle,
          R.attr.materialSearchBarStyle,
          R.attr.materialSearchViewStyle,
          R.attr.materialSearchViewPrefixStyle,
          R.attr.materialSearchViewToolbarHeight,
          R.attr.materialSearchViewToolbarStyle,
          R.attr.materialSwitchStyle,
          R.attr.overflowLinearLayoutStyle,
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

  /** These are shape styles that should be the same for *all* M3 full themes. */
  private static final ImmutableList<Integer> M3_SHAPE_ATTRIBUTES =
      ImmutableList.of(
          R.attr.shapeCornerFamily,
          R.attr.shapeCornerSizeExtraSmall,
          R.attr.shapeCornerSizeSmall,
          R.attr.shapeCornerSizeMedium,
          R.attr.shapeCornerSizeLarge,
          R.attr.shapeCornerSizeLargeIncreased,
          R.attr.shapeCornerSizeExtraExtraLarge,
          R.attr.shapeCornerSizeExtraLargeIncreased,
          R.attr.shapeCornerSizeExtraExtraLarge,
          R.attr.shapeAppearanceCornerExtraSmall,
          R.attr.shapeAppearanceCornerSmall,
          R.attr.shapeAppearanceCornerMedium,
          R.attr.shapeAppearanceCornerLarge,
          R.attr.shapeAppearanceCornerLargeIncreased,
          R.attr.shapeAppearanceCornerExtraLarge,
          R.attr.shapeAppearanceCornerExtraLargeIncreased,
          R.attr.listItemShapeAppearanceFirst,
          R.attr.listItemShapeAppearanceLast,
          R.attr.listItemShapeAppearanceMiddle,
          R.attr.listItemShapeAppearanceSingle,
          R.attr.listItemShapeAppearanceChecked);

  /**
   * These are all the attributes where full themes should match {@code Theme.Material3.Light} or
   * {@code Theme_Material3_Dark}.
   */
  private static final ImmutableList<Integer> M3_FULL_ATTRIBUTES =
      ImmutableList.<Integer>builder()
          .addAll(M3_FULL_COLOR_ATTRIBUTES)
          .addAll(M3_FULL_TYPOGRAPHY_ATTRIBUTES)
          .addAll(M3_FULL_MOTION_ATTRIBUTES)
          .addAll(M3_COMMON_WIDGET_STYLE_ATTRIBUTES)
          .addAll(M3_SHAPE_ATTRIBUTES)
          .build();

  @Parameters(name = "{0}")
  public static ImmutableList<Object[]> getTestData() {
    return ImmutableList.<Object[]>builder()
        // Within Material Light theme.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Light, MATERIAL_INTRA_LIGHT_THEME_ATTRIBUTES))
        // Within Material Dark theme.
        .addAll(
            createTestData(R.style.Theme_MaterialComponents, MATERIAL_INTRA_DARK_THEME_ATTRIBUTES))
        // Material Dark Themes and Theme Overlays.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Bridge,
                R.style.Theme_MaterialComponents,
                MATERIAL_BRIDGE_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_NoActionBar,
                R.style.Theme_MaterialComponents,
                MATERIAL_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Dialog,
                R.style.Theme_MaterialComponents,
                MATERIAL_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.ThemeOverlay_MaterialComponents_Dark,
                R.style.Theme_MaterialComponents,
                MATERIAL_OVERLAY_COLOR_ATTRIBUTES))
        // Material Light Themes and Theme Overlays.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Light_Bridge,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_BRIDGE_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Light_NoActionBar,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Light_Dialog,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_FULL_ATTRIBUTES))
        .addAll(
            createTestData(
                R.style.ThemeOverlay_MaterialComponents_Light,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_OVERLAY_COLOR_ATTRIBUTES))
        // Compare Material Light and Dark themes - they should have the same common widget styles.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_COMMON_WIDGET_STYLE_ATTRIBUTES))
        // Compare Material Light and Dark themes - they should have the same motion styles.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents,
                R.style.Theme_MaterialComponents_Light,
                MATERIAL_MOTION_ATTRIBUTES))
        // Compare Material Light and Dark themes - they should have the same typography styles.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents,
                R.style.Theme_MaterialComponents_Light,
                DEPRECATED_TYPOGRAPHY_ATTRIBUTES))
        // Compare Material Light and Dark bridge themes - they should have the same bridge widget
        // styles.
        .addAll(
            createTestData(
                R.style.Theme_MaterialComponents_Bridge,
                R.style.Theme_MaterialComponents_Light_Bridge,
                MATERIAL_BRIDGE_WIDGET_STYLE_ATTRIBUTES))
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
                M3_FULL_MOTION_ATTRIBUTES))
        // Compare M3 Light and Dark themes - they should have the same shape styles.
        .addAll(
            createTestData(
                R.style.Theme_Material3_Dark, R.style.Theme_Material3_Light, M3_SHAPE_ATTRIBUTES))
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
