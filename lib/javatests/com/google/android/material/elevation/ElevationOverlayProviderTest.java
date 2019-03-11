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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Color;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link ElevationOverlayProvider}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ElevationOverlayProviderTest {

  private static final float ELEVATION_ZERO = 0;
  private static final float ELEVATION_NON_ZERO = 4;
  private static final String EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY = "ff121212";
  private static final float EXPECTED_ELEVATION_NON_ZERO_ALPHA_FRACTION = 0.07f;
  private static final int EXPECTED_ELEVATION_NON_ZERO_ALPHA = 18;

  private final Context context = ApplicationProvider.getApplicationContext();

  private ElevationOverlayProvider provider;

  @Before
  public void initContextThemeWithElevationOverlays() {
    context.setTheme(R.style.Test_Theme_MaterialComponents_ElevationOverlays);
  }

  @Test
  public void
      givenOverlaysEnabledAndSurfaceColorAndElevation_whenLayerOverlayIfNeeded_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getColorSurface();
    assertThat(provider.layerOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(provider.layerOverlay(backgroundColor, ELEVATION_NON_ZERO));
  }

  @Test
  public void
      givenOverlaysEnabledAndNoSurfaceColorAndElevation_whenLayerOverlayIfNeeded_returnsColorWithoutOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = Color.RED;
    assertThat(provider.layerOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void
      givenOverlaysEnabledAndSurfaceColorAndNoElevation_whenLayerOverlayIfNeeded_returnsColorWithoutOverlay() {
    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getColorSurface();
    assertThat(provider.layerOverlayIfNeeded(backgroundColor, ELEVATION_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void givenOverlaysDisabled_whenLayerOverlayIfNeeded_returnsColorWithoutOverlay() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);

    provider = new ElevationOverlayProvider(context);

    int backgroundColor = provider.getColorSurface();
    assertThat(provider.layerOverlayIfNeeded(backgroundColor, ELEVATION_NON_ZERO))
        .isEqualTo(backgroundColor);
  }

  @Test
  public void givenBackgroundColorAndElevation_whenLayerOverlay_returnsColorWithOverlay() {
    provider = new ElevationOverlayProvider(context);

    assertThat(Integer.toHexString(provider.layerOverlay(Color.BLACK, ELEVATION_NON_ZERO)))
        .isEqualTo(EXPECTED_ELEVATION_NON_ZERO_COLOR_WITH_OVERLAY);
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
}
