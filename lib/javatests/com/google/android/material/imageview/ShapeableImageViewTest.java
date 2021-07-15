/*
 * Copyright 2021 The Android Open Source Project
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
package com.google.android.material.imageview;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Build;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link com.google.android.material.imageview.ShapeableImageView}.
 */
@RunWith(RobolectricTestRunner.class)
public class ShapeableImageViewTest {

  private static final float SMALL_CORNER_SIZE = 20f;
  private static final float LARGE_CORNER_SIZE = 40f;
  private static final int CUT_CORNER_FAMILY = CornerFamily.CUT;
  private static final Class<CutCornerTreatment> CUT_CORNER_FAMILY_CLASS = CutCornerTreatment.class;

  // Valid measureSpec values copied from a debugging session:
  private static final int WIDTH_MEASURE_SPEC = 0x4000_03da;
  private static final int HEIGHT_MEASURE_SPEC = 0x0000_0593;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
  }

  @Test
  public void testSetShapeAppearanceModel() {
    ShapeableImageView imageView = new ShapeableImageView(context);
    ShapeAppearanceModel appliedShapeAppearanceModel =
        ShapeAppearanceModel.builder()
            .setAllCorners(CUT_CORNER_FAMILY, SMALL_CORNER_SIZE)
            .build();

    imageView.setShapeAppearanceModel(appliedShapeAppearanceModel);

    ShapeAppearanceModel actualShapeAppearanceModel = imageView.getShapeAppearanceModel();

    assertThatCornerSizesMatch(appliedShapeAppearanceModel, actualShapeAppearanceModel);
    assertThatCornerFamilyMatches(CUT_CORNER_FAMILY_CLASS, actualShapeAppearanceModel);
  }

  private void assertThatCornerSizesMatch(
      ShapeAppearanceModel expected, ShapeAppearanceModel actual) {
    assertThat(actual.getTopLeftCornerSize())
        .isEqualTo(expected.getTopLeftCornerSize());
    assertThat(actual.getTopRightCornerSize())
        .isEqualTo(expected.getTopRightCornerSize());
    assertThat(actual.getBottomRightCornerSize())
        .isEqualTo(expected.getBottomRightCornerSize());
    assertThat(actual.getBottomLeftCornerSize())
        .isEqualTo(expected.getBottomLeftCornerSize());
  }

  private void assertThatCornerFamilyMatches(
      Class<? extends CornerTreatment> expectedCornerFamily,
      ShapeAppearanceModel shapeAppearanceModel) {
    assertThat(shapeAppearanceModel.getTopLeftCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getTopRightCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getBottomRightCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getBottomLeftCorner()).isInstanceOf(expectedCornerFamily);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setContentPaddingBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setContentPadding(1, 2, 3, 4);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(2);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(3);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(4);
    assertThat(imageView.getPaddingLeft()).isEqualTo(0);
    assertThat(imageView.getPaddingTop()).isEqualTo(0);
    assertThat(imageView.getPaddingRight()).isEqualTo(0);
    assertThat(imageView.getPaddingBottom()).isEqualTo(0);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(2);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(3);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(4);
    assertThat(imageView.getPaddingLeft()).isEqualTo(0);
    assertThat(imageView.getPaddingTop()).isEqualTo(0);
    assertThat(imageView.getPaddingRight()).isEqualTo(0);
    assertThat(imageView.getPaddingBottom()).isEqualTo(0);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setPaddingBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setPadding(1, 2, 3, 4);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(0);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(0);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(0);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(0);
    assertThat(imageView.getPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingRight()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(0);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(0);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(0);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(0);
    assertThat(imageView.getPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingRight()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setPaddingAndContentPaddingBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setPadding(1, 2, 3, 4);
    imageView.setContentPadding(5, 6, 7, 8);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(5);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(6);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(7);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(8);
    assertThat(imageView.getPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingRight()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingLeft()).isEqualTo(5);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(6);
    assertThat(imageView.getContentPaddingRight()).isEqualTo(7);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(8);
    assertThat(imageView.getPaddingLeft()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingRight()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setContentPaddingRelativeBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setContentPaddingRelative(1, 2, 3, 4);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(1);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(2);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(4);
    assertThat(imageView.getPaddingStart()).isEqualTo(0);
    assertThat(imageView.getPaddingTop()).isEqualTo(0);
    assertThat(imageView.getPaddingEnd()).isEqualTo(0);
    assertThat(imageView.getPaddingBottom()).isEqualTo(0);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(1);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(2);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(4);
    assertThat(imageView.getPaddingStart()).isEqualTo(0);
    assertThat(imageView.getPaddingTop()).isEqualTo(0);
    assertThat(imageView.getPaddingEnd()).isEqualTo(0);
    assertThat(imageView.getPaddingBottom()).isEqualTo(0);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setPaddingRelativeBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setPaddingRelative(1, 2, 3, 4);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(0);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(0);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(0);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(0);
    assertThat(imageView.getPaddingStart()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(0);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(0);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(0);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(0);
    assertThat(imageView.getPaddingStart()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);
  }

  @Config(sdk = {16, 18, 19, 21, 30})
  @Test
  public void setPaddingAndContentPaddingRelativeBeforeMeasure() {
    ShapeableImageView imageView = new ShapeableImageView(context);

    imageView.setPaddingRelative(1, 2, 3, 4);
    imageView.setContentPaddingRelative(5, 6, 7, 8);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(5);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(6);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(7);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(8);
    assertThat(imageView.getPaddingStart()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);

    forceResolveLayoutDirection(imageView);
    imageView.onMeasure(WIDTH_MEASURE_SPEC, HEIGHT_MEASURE_SPEC);

    assertThat(imageView.getContentPaddingStart()).isEqualTo(5);
    assertThat(imageView.getContentPaddingTop()).isEqualTo(6);
    assertThat(imageView.getContentPaddingEnd()).isEqualTo(7);
    assertThat(imageView.getContentPaddingBottom()).isEqualTo(8);
    assertThat(imageView.getPaddingStart()).isEqualTo(1);
    assertThat(imageView.getPaddingTop()).isEqualTo(2);
    assertThat(imageView.getPaddingEnd()).isEqualTo(3);
    assertThat(imageView.getPaddingBottom()).isEqualTo(4);
  }

  /**
   * Normally called by the system, we must call this because onMeasure's behavior relies on it. It
   * is a hidden API, so must be called with reflection.
   */
  private void forceResolveLayoutDirection(View view) {
    if (Build.VERSION.SDK_INT < 19) {
      return;
    }

    try {
      Method method = view.getClass().getMethod("resolveLayoutDirection");
      method.invoke(view);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      fail("Could not force resolve layout direction: " + e.getMessage());
    }

    assertThat(view.isLayoutDirectionResolved()).isTrue();
  }
}
