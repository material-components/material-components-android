/*
 * Copyright 2018 The Android Open Source Project
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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.util.AttributeSet;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Robolectric.AttributeSetBuilder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.shape.ShapeAppearanceModel}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ShapeAppearanceModelTest {

  private static final float DEFAULT_CORNER_SIZE = 10;
  private static final float LARGE_CORNER_SIZE = 20;

  private final Context context = ApplicationProvider.getApplicationContext();

  private ShapeAppearanceModel shapeAppearance;

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_AppCompat);
  }

  @Test
  public void noOverlay_hasDefaultCorners() {
    AttributeSet attributes = buildStyleAttributeSet().build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertCornersInstanceOf(RoundedCornerTreatment.class);
    assertCornerSize(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void cornerFamilyCutOverlay_hasCutCorners() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(R.attr.shapeAppearanceOverlay, "@style/ShapeAppearanceOverlay.Cut")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertCornersInstanceOf(CutCornerTreatment.class);
    assertCornerSize(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void topLeftCornerFamilyCutOverlay_hasTopLeftCutCorner() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(R.attr.shapeAppearanceOverlay, "@style/ShapeAppearanceOverlay.TopLeftCut")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertThat(shapeAppearance.getTopLeftCorner()).isInstanceOf(CutCornerTreatment.class);
    assertThat(shapeAppearance.getTopRightCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getBottomLeftCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getBottomRightCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertCornerSize(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void bottomRightCornerFamilyCutOverlay_hasBottomRightCutCorner() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(
                R.attr.shapeAppearanceOverlay, "@style/ShapeAppearanceOverlay.BottomRightCut")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertThat(shapeAppearance.getTopLeftCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getTopRightCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getBottomLeftCorner()).isInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getBottomRightCorner()).isInstanceOf(CutCornerTreatment.class);
    assertCornerSize(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void cornerSizeOverlay_hasCorrectlySizedCorners() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(
                R.attr.shapeAppearanceOverlay, "@style/ShapeAppearanceOverlay.DifferentCornerSize")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertCornersInstanceOf(RoundedCornerTreatment.class);
    assertCornerSize(LARGE_CORNER_SIZE);
  }

  @Test
  public void topRightCornerSizeOverlay_hasCorrectlySizedTopRightCorner() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(
                R.attr.shapeAppearanceOverlay,
                "@style/ShapeAppearanceOverlay.TopRightDifferentCornerSize")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertCornersInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getTopLeftCorner().getCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getTopRightCorner().getCornerSize()).isEqualTo(LARGE_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomLeftCorner().getCornerSize())
        .isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomRightCorner().getCornerSize())
        .isEqualTo(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void bottomLeftCornerSizeOverlay_hasCorrectlySizedBottomLeftCorner() {
    AttributeSet attributes =
        buildStyleAttributeSet()
            .addAttribute(
                R.attr.shapeAppearanceOverlay,
                "@style/ShapeAppearanceOverlay.BottomLeftDifferentCornerSize")
            .build();

    shapeAppearance = ShapeAppearanceModel.builder(context, attributes, 0, 0).build();

    assertCornersInstanceOf(RoundedCornerTreatment.class);
    assertThat(shapeAppearance.getTopLeftCorner().getCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getTopRightCorner().getCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomLeftCorner().getCornerSize()).isEqualTo(LARGE_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomRightCorner().getCornerSize())
        .isEqualTo(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void setCornerRadius_defaultDoesNotChange() {
    shapeAppearance = ShapeAppearanceModel.builder().setCornerRadius(DEFAULT_CORNER_SIZE).build();
    ShapeAppearanceModel largeCornerShape =
        shapeAppearance.toBuilder().setCornerRadius(LARGE_CORNER_SIZE).build();

    assertCornerSize(shapeAppearance, DEFAULT_CORNER_SIZE);
    assertCornerSize(largeCornerShape, LARGE_CORNER_SIZE);
  }

  @Test
  public void setOneCornerRadius_othersDoNotChange() {
    shapeAppearance =
        ShapeAppearanceModel.builder()
            .setAllCorners(new RoundedCornerTreatment(DEFAULT_CORNER_SIZE))
            .setTopLeftCornerSize(LARGE_CORNER_SIZE)
            .build();

    assertCornerSizes(
        shapeAppearance,
        LARGE_CORNER_SIZE,
        DEFAULT_CORNER_SIZE,
        DEFAULT_CORNER_SIZE,
        DEFAULT_CORNER_SIZE);
  }

  @Test
  public void setDifferentCornerTreatments_allTreatmentsSet() {
    shapeAppearance =
        ShapeAppearanceModel.builder()
            .setTopLeftCorner(new RoundedCornerTreatment(DEFAULT_CORNER_SIZE))
            .setTopRightCorner(new CutCornerTreatment(LARGE_CORNER_SIZE))
            .setBottomLeftCorner(new RoundedCornerTreatment(LARGE_CORNER_SIZE))
            .setBottomRightCorner(new CutCornerTreatment(DEFAULT_CORNER_SIZE))
            .build();

    assertCornerSizes(
        shapeAppearance,
        DEFAULT_CORNER_SIZE,
        LARGE_CORNER_SIZE,
        LARGE_CORNER_SIZE,
        DEFAULT_CORNER_SIZE);
  }

  private AttributeSetBuilder buildStyleAttributeSet() {
    return Robolectric.buildAttributeSet()
        .addAttribute(R.attr.shapeAppearance, "@style/ShapeAppearance.MaterialComponents.Test");
  }

  private void assertCornersInstanceOf(Class<? extends CornerTreatment> clazz) {
    assertThat(shapeAppearance.getTopLeftCorner()).isInstanceOf(clazz);
    assertThat(shapeAppearance.getTopRightCorner()).isInstanceOf(clazz);
    assertThat(shapeAppearance.getBottomLeftCorner()).isInstanceOf(clazz);
    assertThat(shapeAppearance.getBottomRightCorner()).isInstanceOf(clazz);
  }

  private void assertCornerSize(float cornerSize) {
    assertCornerSize(shapeAppearance, cornerSize);
  }

  private static void assertCornerSize(ShapeAppearanceModel shapeAppearance, float cornerSize) {
    assertCornerSizes(shapeAppearance, cornerSize, cornerSize, cornerSize, cornerSize);
  }

  private static void assertCornerSizes(
      ShapeAppearanceModel shapeAppearance,
      float topLeftCornerSize,
      float topRightCornerSize,
      float bottomLeftCornerSize,
      float bottomRightCornerSize) {
    assertThat(shapeAppearance.getTopLeftCorner().getCornerSize()).isEqualTo(topLeftCornerSize);
    assertThat(shapeAppearance.getTopRightCorner().getCornerSize()).isEqualTo(topRightCornerSize);
    assertThat(shapeAppearance.getBottomLeftCorner().getCornerSize())
        .isEqualTo(bottomLeftCornerSize);
    assertThat(shapeAppearance.getBottomRightCorner().getCornerSize())
        .isEqualTo(bottomRightCornerSize);
  }
}
