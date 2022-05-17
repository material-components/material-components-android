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

import com.google.android.material.test.R;

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

  private static final CornerSize DEFAULT_CORNER_SIZE = new AbsoluteCornerSize(10);
  private static final CornerSize LARGE_CORNER_SIZE = new AbsoluteCornerSize(20);

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
    assertThat(shapeAppearance.getTopLeftCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getTopRightCornerSize()).isEqualTo(LARGE_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomLeftCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomRightCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
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
    assertThat(shapeAppearance.getTopLeftCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getTopRightCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomLeftCornerSize()).isEqualTo(LARGE_CORNER_SIZE);
    assertThat(shapeAppearance.getBottomRightCornerSize()).isEqualTo(DEFAULT_CORNER_SIZE);
  }

  @Test
  public void setCornerRadius_defaultDoesNotChange() {
    shapeAppearance = ShapeAppearanceModel.builder().setAllCornerSizes(DEFAULT_CORNER_SIZE).build();
    ShapeAppearanceModel largeCornerShape =
        shapeAppearance.toBuilder().setAllCornerSizes(LARGE_CORNER_SIZE).build();

    assertCornerSize(shapeAppearance, DEFAULT_CORNER_SIZE);
    assertCornerSize(largeCornerShape, LARGE_CORNER_SIZE);
  }

  @Test
  public void setOneCornerRadius_othersDoNotChange() {
    shapeAppearance =
        ShapeAppearanceModel.builder()
            .setAllCorners(new RoundedCornerTreatment())
            .setAllCornerSizes(DEFAULT_CORNER_SIZE)
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
            .setTopLeftCorner(new RoundedCornerTreatment())
            .setTopLeftCornerSize(DEFAULT_CORNER_SIZE)
            .setTopRightCorner(new CutCornerTreatment())
            .setTopRightCornerSize(LARGE_CORNER_SIZE)
            .setBottomLeftCorner(new RoundedCornerTreatment())
            .setBottomLeftCornerSize(LARGE_CORNER_SIZE)
            .setBottomRightCorner(new CutCornerTreatment())
            .setBottomRightCornerSize(DEFAULT_CORNER_SIZE)
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

  private void assertCornerSize(CornerSize cornerSize) {
    assertCornerSize(shapeAppearance, cornerSize);
  }

  private static void assertCornerSize(
      ShapeAppearanceModel shapeAppearance, CornerSize cornerSize) {
    assertCornerSizes(shapeAppearance, cornerSize, cornerSize, cornerSize, cornerSize);
  }

  private static void assertCornerSizes(
      ShapeAppearanceModel shapeAppearance,
      CornerSize topLeftCornerSize,
      CornerSize topRightCornerSize,
      CornerSize bottomLeftCornerSize,
      CornerSize bottomRightCornerSize) {
    assertThat(shapeAppearance.getTopLeftCornerSize()).isEqualTo(topLeftCornerSize);
    assertThat(shapeAppearance.getTopRightCornerSize()).isEqualTo(topRightCornerSize);
    assertThat(shapeAppearance.getBottomLeftCornerSize()).isEqualTo(bottomLeftCornerSize);
    assertThat(shapeAppearance.getBottomRightCornerSize()).isEqualTo(bottomRightCornerSize);
  }
}
