/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.shape;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.android.material.shape.ShapeAppearanceModel.OnChangedListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link OnChangedListener}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ShapeAppearanceModelOnChangedListenerTest {

  @CornerFamily private static final int DEFAULT_CORNER_FAMILY = CornerFamily.ROUNDED;
  @CornerFamily private static final int CUT_CORNER_FAMILY = CornerFamily.CUT;
  private static final Class<CutCornerTreatment> CUT_CORNER_FAMILY_CLASS = CutCornerTreatment.class;
  private static final float DEFAULT_CORNER_SIZE = 10f;
  private static final float LARGE_CORNER_SIZE = 40f;

  private final ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel();
  private final MaterialShapeDrawable materialShapeDrawable =
      new MaterialShapeDrawable(shapeAppearanceModel);

  @Before
  public void setShapeAppearanceDefaultCornerTreatments() {
    shapeAppearanceModel.setAllCorners(DEFAULT_CORNER_FAMILY, (int) DEFAULT_CORNER_SIZE);
  }

  @Test
  public void changeShapeAppearance_allCorners() {
    shapeAppearanceModel.setAllCorners(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);

    assertTopLeftCornerIsUpdated(CUT_CORNER_FAMILY_CLASS, LARGE_CORNER_SIZE);
  }

  @Test
  public void changeShapeAppearance_topLeftCorner() {
    shapeAppearanceModel.setTopLeftCorner(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);

    assertTopLeftCornerIsUpdated(CUT_CORNER_FAMILY_CLASS, LARGE_CORNER_SIZE);
  }

  @Test
  public void changeShapeAppearance_topLeftCornerModifyExisting() {
    materialShapeDrawable
        .getShapeAppearanceModel()
        .setTopLeftCorner(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);

    assertTopLeftCornerIsUpdated(CUT_CORNER_FAMILY_CLASS, LARGE_CORNER_SIZE);
  }

  @Test
  public void changeShapeAppearance_newShapeAppearance() {
    ShapeAppearanceModel newShapeAppearanceModel = new ShapeAppearanceModel();
    newShapeAppearanceModel.setTopLeftCorner(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);
    materialShapeDrawable.setShapeAppearanceModel(newShapeAppearanceModel);

    assertTopLeftCornerIsUpdated(CUT_CORNER_FAMILY_CLASS, LARGE_CORNER_SIZE);
  }

  @Test
  public void changeShapeAppearance_shapeChangedCallbackInvokedSetTopLeftCorner() {
    final OnChangedListener mockOnChangedListener = mock(OnChangedListener.class);
    shapeAppearanceModel.addOnChangedListener(mockOnChangedListener);
    shapeAppearanceModel.setTopLeftCorner(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);
    verify(mockOnChangedListener, times(1)).onShapeAppearanceModelChanged();
  }

  @Test
  public void
      changeShapeAppearance_shapeChangedCallbackInvokedFromMaterialShapeDrawableTopLeftCorner() {
    final OnChangedListener mockOnChangedListener = mock(OnChangedListener.class);
    shapeAppearanceModel.addOnChangedListener(mockOnChangedListener);

    materialShapeDrawable
        .getShapeAppearanceModel()
        .setTopLeftCorner(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);

    verify(mockOnChangedListener, times(1)).onShapeAppearanceModelChanged();
  }

  @Test
  public void changeShapeAppearance_shapeChangedCallbackInvokedAllCorners() {
    final OnChangedListener mockOnChangedListener = mock(OnChangedListener.class);
    shapeAppearanceModel.addOnChangedListener(mockOnChangedListener);
    shapeAppearanceModel.setAllCorners(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);
    verify(mockOnChangedListener, times(1)).onShapeAppearanceModelChanged();
  }

  @Test
  public void
      changeShapeAppearance_shapeChangedCallbackInvokedFromMaterialShapeDrawableAllCorners() {
    final OnChangedListener mockOnChangedListener = mock(OnChangedListener.class);
    shapeAppearanceModel.addOnChangedListener(mockOnChangedListener);

    materialShapeDrawable
        .getShapeAppearanceModel()
        .setAllCorners(CUT_CORNER_FAMILY, (int) LARGE_CORNER_SIZE);

    verify(mockOnChangedListener, times(1)).onShapeAppearanceModelChanged();
  }

  private void assertTopLeftCornerIsUpdated(
      Class<? extends CornerTreatment> expectedCornerFamily, float expectedCornerSize) {
    assertTopLeftCornerSize(materialShapeDrawable, expectedCornerSize);
    assertTopLeftCornerInstanceOf(materialShapeDrawable, expectedCornerFamily);
  }

  private static void assertTopLeftCornerInstanceOf(
      MaterialShapeDrawable materialShapeDrawable, Class<? extends CornerTreatment> clazz) {
    // Retrieve the ShapeAppearanceModel from the MaterialShapeDrawable to ensure that the
    // MaterialShapeDrawable's corners have updated to the correct ShapeAppearanceModel.
    assertThat(materialShapeDrawable.getShapeAppearanceModel().getTopLeftCorner())
        .isInstanceOf(clazz);
  }

  private static void assertTopLeftCornerSize(
      MaterialShapeDrawable materialShapeDrawable, float cornerSize) {
    // Retrieve the ShapeAppearanceModel from the MaterialShapeDrawable to ensure that the
    // MaterialShapeDrawable's corners have updated to the correct ShapeAppearanceModel.
    assertThat(materialShapeDrawable.getShapeAppearanceModel().getTopLeftCorner().getCornerSize())
        .isEqualTo(cornerSize);
  }
}
