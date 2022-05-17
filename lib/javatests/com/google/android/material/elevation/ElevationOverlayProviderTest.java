/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.elevation;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.resources.MaterialAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link ElevationOverlayProvider}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.P)
@DoNotInstrument
public class ElevationOverlayProviderTest {

  private static final float ELEVATION_ZERO = 0;
  private static final float ELEVATION_NON_ZERO = 4;
  private static final float ELEVATION_OVERLAY_VIEW_PARENT = 8;
  private static final int ALPHA_TRANSLUCENT = 75;
  private static final int ALPHA_TRANSPARENT = 0;
  private static final String EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY = "ff181818";
  private static final String EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY_AND_VIEW = "ff232323";
  private static final float EXPECTED_ELEVATION_NON_ZERO_ALPHA_FRACTION = 0.09f;
  private static final int EXPECTED_ELEVATION_NON_ZERO_ALPHA = 24;

  private final Context context = ApplicationProvider.getApplicationContext();

  private ElevationOverlayProvider provider;
  private View overlayView;

  @Before
  public void initContextThemeWithElevationOverlay() {
    context.setTheme(R.style.Theme_MaterialComponents_NoActionBar);
  }

  @Before
  public void initOverlayView() {
    overlayView = new View(context);
    ViewGroup overlayViewParent = new FrameLayout(context);
    ViewCompat.setElevation(overlayViewParent, ELEVATION_OVERLAY_VIEW_PARENT);
    overlayViewParent.addView(overlayView);
  }

  @Test
  public void
      givenOverlayEnabledAndSurfaceColorAndElevation_whenCompositeOverlayIfNeeded_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getThemeSurfaceColor();
    assertThat(provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(provider.compositeOverlay(backgroundColor, ELEVATION_NON_ZERO));
  }

  @Test
  public void
      givenOverlayEnabledAndTranslucentSurfaceColorAndElevation_whenCompositeOverlayIfNeeded_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor =
        ColorUtils.setAlphaComponent(provider.getThemeSurfaceColor(), ALPHA_TRANSLUCENT);
    int actualOverlayColor = provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO);
    assertThat(actualOverlayColor)
        .isEqualTo(provider.compositeOverlay(backgroundColor, ELEVATION_NON_ZERO));
    assertThat(Color.alpha(actualOverlayColor)).isEqualTo(ALPHA_TRANSLUCENT);
  }

  @Test
  public void
      givenOverlayEnabledAndTransparentSurfaceColorAndElevation_whenCompositeOverlayIfNeeded_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor =
        ColorUtils.setAlphaComponent(provider.getThemeSurfaceColor(), ALPHA_TRANSPARENT);
    int actualOverlayColor = provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO);
    assertThat(actualOverlayColor)
        .isEqualTo(provider.compositeOverlay(backgroundColor, ELEVATION_NON_ZERO));
    assertThat(Color.alpha(actualOverlayColor)).isEqualTo(ALPHA_TRANSPARENT);
  }

  @Test
  public void
      givenOverlayEnabledAndSurfaceColorAndElevationAndOverlayView_whenCompositeOverlayIfNeeded_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getThemeSurfaceColor();
    assertThat(provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO, overlayView))
        .isEqualTo(
            provider.compositeOverlay(
                backgroundColor, ELEVATION_NON_ZERO + ELEVATION_OVERLAY_VIEW_PARENT));
  }

  @Test
  public void
      givenOverlayEnabledAndNoSurfaceColorAndElevation_whenCompositeOverlayIfNeeded_returnsColorWithoutOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = Color.RED;
    assertThat(provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void
      givenOverlayEnabledAndSurfaceColorAndNoElevation_whenCompositeOverlayIfNeeded_returnsColorWithoutOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getThemeSurfaceColor();
    assertThat(provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void givenOverlayDisabled_whenCompositeOverlayIfNeeded_returnsColorWithoutOverlay() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);

    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getThemeSurfaceColor();
    assertThat(provider.compositeOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void givenBackgroundColorAndElevation_whenCompositeOverlay_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    assertThat(Integer.toHexString(provider.compositeOverlay(Color.BLACK, ELEVATION_NON_ZERO)))
        .isEqualTo(EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY);
  }

  @Test
  public void
      givenBackgroundColorAndElevationAndOverlayView_whenCompositeOverlay_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    assertThat(
            Integer.toHexString(
                provider.compositeOverlay(Color.BLACK, ELEVATION_NON_ZERO, overlayView)))
        .isEqualTo(EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY_AND_VIEW);
  }

  @Test
  public void givenNoElevation_whenCalculateOverlayAlphaFraction_returnsZero() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlphaFraction(ELEVATION_ZERO)).isZero();
  }

  @Test
  public void givenNegativeElevation_whenCalculateOverlayAlphaFraction_returnsZero() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlphaFraction(-ELEVATION_NON_ZERO)).isZero();
  }

  @Test
  public void givenElevation_whenCalculateOverlayAlphaFraction_returnsAlphaFraction() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlphaFraction(ELEVATION_NON_ZERO))
        .isWithin(0.01f)
        .of(EXPECTED_ELEVATION_NON_ZERO_ALPHA_FRACTION);
  }

  @Test
  public void givenMaxElevation_whenCalculateOverlayAlphaFraction_returnsOne() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlphaFraction(Float.MAX_VALUE)).isWithin(0.01f).of(1);
  }

  @Test
  public void givenNoElevation_whenCalculateOverlayAlpha_returnsZero() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlpha(ELEVATION_ZERO)).isEqualTo(0);
  }

  @Test
  public void givenNegativeElevation_whenCalculateOverlayAlpha_returnsZero() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlpha(-ELEVATION_NON_ZERO)).isEqualTo(0);
  }

  @Test
  public void givenElevation_whenCalculateOverlayAlpha_returnsAlpha() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlpha(ELEVATION_NON_ZERO))
        .isEqualTo(EXPECTED_ELEVATION_NON_ZERO_ALPHA);
  }

  @Test
  public void givenMaxElevation_whenCalculateOverlayAlpha_returns255() {
    provider = new ElevationOverlayProvider(context);

    assertThat(provider.calculateOverlayAlpha(Float.MAX_VALUE)).isEqualTo(255);
  }

  @Test
  public void givenManuallyConstructedObject_whenGetThemeSurfaceColor_returnsExplicitValue() {
    provider =
        new ElevationOverlayProvider(
            MaterialAttributes.resolveBoolean(context, R.attr.elevationOverlayEnabled, false),
            MaterialColors.getColor(context, R.attr.elevationOverlayColor, Color.TRANSPARENT),
            MaterialColors.getColor(context, R.attr.elevationOverlayAccentColor, Color.TRANSPARENT),
            /* colorSurface= */ Color.RED,
            context.getResources().getDisplayMetrics().density);

    assertThat(provider.getThemeSurfaceColor()).isEqualTo(Color.RED);
  }
}
