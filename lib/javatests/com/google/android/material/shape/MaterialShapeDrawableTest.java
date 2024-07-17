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
package com.google.android.material.shape;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.color.MaterialColors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link MaterialShapeDrawable}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialShapeDrawableTest {

  private static final float ELEVATION = 4;
  private static final float TRANSLATION_Z = 2;
  private static final float Z = ELEVATION + TRANSLATION_Z;
  private static final int ALPHA = 127;

  private final Context context = ApplicationProvider.getApplicationContext();

  private MaterialShapeDrawable materialShapeDrawable;
  private int colorSurface;

  @Before
  public void setUpThemeAndResources() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    colorSurface =
        MaterialColors.getColor(context, R.attr.colorSurface, getClass().getSimpleName());
  }

  @Before
  public void setUpMaterialShapeDrawable() {
    materialShapeDrawable = new MaterialShapeDrawable();
  }

  @Test
  public void givenNoElevation_whenGetElevation_returnsZero() {
    assertThat(materialShapeDrawable.getElevation()).isZero();
  }

  @Test
  public void givenNoTranslationZ_whenGetTranslationZ_returnsZero() {
    assertThat(materialShapeDrawable.getTranslationZ()).isZero();
  }

  @Test
  public void givenNoElevationOrTranslationZ_whenGetZ_returnsZero() {
    assertThat(materialShapeDrawable.getZ()).isZero();
  }

  @Test
  public void givenElevation_whenGetElevation_returnsElevation() {
    materialShapeDrawable.setElevation(ELEVATION);

    assertThat(materialShapeDrawable.getElevation()).isEqualTo(ELEVATION);
  }

  @Test
  public void givenTranslationZ_whenGetTranslationZ_returnsTranslationZ() {
    materialShapeDrawable.setTranslationZ(TRANSLATION_Z);

    assertThat(materialShapeDrawable.getTranslationZ()).isEqualTo(TRANSLATION_Z);
  }

  @Test
  public void givenElevation_whenGetZ_returnsElevation() {
    materialShapeDrawable.setElevation(ELEVATION);

    assertThat(materialShapeDrawable.getZ()).isEqualTo(ELEVATION);
  }

  @Test
  public void givenTranslationZ_whenGetZ_returnsTranslationZ() {
    materialShapeDrawable.setTranslationZ(TRANSLATION_Z);

    assertThat(materialShapeDrawable.getZ()).isEqualTo(TRANSLATION_Z);
  }

  @Test
  public void givenElevationAndTranslationZ_whenGetZ_returnsSumOfElevationAndTranslationZ() {
    materialShapeDrawable.setElevation(ELEVATION);
    materialShapeDrawable.setTranslationZ(TRANSLATION_Z);

    assertThat(materialShapeDrawable.getZ()).isEqualTo(Z);
  }

  @Test
  public void givenElevation_whenSetZ_setsTranslationZToDifferenceBetweenZAndElevation() {
    materialShapeDrawable.setElevation(ELEVATION);

    materialShapeDrawable.setZ(Z);

    assertThat(materialShapeDrawable.getTranslationZ()).isEqualTo(TRANSLATION_Z);
  }

  @Test
  public void
      givenNoElevation_whenCreateWithElevationOverlay_returnsMaterialShapeDrawableWithOverlayAndNoElevation() {
    MaterialShapeDrawable drawable = MaterialShapeDrawable.createWithElevationOverlay(context);

    assertThat(drawable.getElevation()).isZero();
    assertThat(drawable.getFillColor().getDefaultColor()).isEqualTo(colorSurface);
    assertThat(drawable.isElevationOverlayInitialized()).isTrue();
  }

  @Test
  public void
      givenElevation_whenCreateWithElevationOverlay_returnsMaterialShapeDrawableWithOverlayAndElevation() {
    MaterialShapeDrawable drawable =
        MaterialShapeDrawable.createWithElevationOverlay(context, ELEVATION);

    assertThat(drawable.getElevation()).isEqualTo(ELEVATION);
    assertThat(drawable.getFillColor().getDefaultColor()).isEqualTo(colorSurface);
    assertThat(drawable.isElevationOverlayInitialized()).isTrue();
  }

  @Test
  public void whenSetAlpha_returnsAlpha() {
    materialShapeDrawable.setAlpha(ALPHA);

    assertThat(materialShapeDrawable.getAlpha()).isEqualTo(ALPHA);
  }
}
