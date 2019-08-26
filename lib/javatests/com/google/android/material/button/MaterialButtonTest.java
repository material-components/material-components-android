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
package com.google.android.material.button;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;

/** Tests for {@link com.google.android.material.button.MaterialButton}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MaterialButtonTest {

  private static final float LARGE_CORNER_SIZE = 40f;
  private static final int CUT_CORNER_FAMILY = CornerFamily.CUT;
  private static final Class<CutCornerTreatment> CUT_CORNER_FAMILY_CLASS = CutCornerTreatment.class;

  private final ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel();
  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
  }

  @Test
  public void testSetShapeAppearanceModel_setCornerRadius() {
    MaterialButton materialButton = new MaterialButton(context);
    shapeAppearanceModel.setCornerRadius(LARGE_CORNER_SIZE);

    materialButton.setShapeAppearanceModel(shapeAppearanceModel);

    ShapeAppearanceModel newShapeAppearanceModel = materialButton.getShapeAppearanceModel();

    assertThatCornerSizesMatch(shapeAppearanceModel, newShapeAppearanceModel);
  }

  @Test
  public void testSetShapeAppearanceModel() {
    MaterialButton materialButton = new MaterialButton(context);
    shapeAppearanceModel.setAllCorners(CUT_CORNER_FAMILY, materialButton.getCornerRadius());

    materialButton.setShapeAppearanceModel(shapeAppearanceModel);

    ShapeAppearanceModel newShapeAppearanceModel = materialButton.getShapeAppearanceModel();

    assertThatCornerSizesMatch(shapeAppearanceModel, newShapeAppearanceModel);
    assertThatCornerFamilyMatches(newShapeAppearanceModel, CUT_CORNER_FAMILY_CLASS);
  }

  @Test
  public void testGetShapeAppearanceModel() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton
        .getShapeAppearanceModel()
        .setAllCorners(CUT_CORNER_FAMILY, materialButton.getCornerRadius());

    assertThatCornerFamilyMatches(
        materialButton.getShapeAppearanceModel(), CUT_CORNER_FAMILY_CLASS);
  }

  @Test
  public void setIcon_IconUpdated_whenCalledTwice() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setText("test");
    int measureSpec =
        MeasureSpec.makeMeasureSpec(200, MeasureSpec.AT_MOST);

    Drawable drawable1 = ContextCompat.getDrawable(context, android.R.drawable.btn_plus);
    materialButton.setIcon(drawable1);
    materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_START);
    materialButton.measure(measureSpec, measureSpec);

    assertThat(materialButton.getIcon()).isEqualTo(drawable1);

    Drawable drawable2 = ContextCompat.getDrawable(context, android.R.drawable.btn_minus);
    materialButton.setIcon(drawable2);
    materialButton.measure(measureSpec, measureSpec);

    assertThat(materialButton.getIcon()).isEqualTo(drawable2);
  }

  private void assertThatCornerFamilyMatches(
      ShapeAppearanceModel shapeAppearanceModel,
      Class<? extends CornerTreatment> expectedCornerFamily) {
    assertThat(shapeAppearanceModel.getTopLeftCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getTopRightCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getBottomRightCorner()).isInstanceOf(expectedCornerFamily);
    assertThat(shapeAppearanceModel.getBottomLeftCorner()).isInstanceOf(expectedCornerFamily);
  }

  private void assertThatCornerSizesMatch(
      ShapeAppearanceModel shapeAppearanceModel, ShapeAppearanceModel newShapeAppearanceModel) {
    assertThat(shapeAppearanceModel.getTopLeftCorner().getCornerSize())
        .isEqualTo(newShapeAppearanceModel.getTopLeftCorner().getCornerSize());
    assertThat(shapeAppearanceModel.getTopRightCorner().getCornerSize())
        .isEqualTo(newShapeAppearanceModel.getTopRightCorner().getCornerSize());
    assertThat(shapeAppearanceModel.getBottomRightCorner().getCornerSize())
        .isEqualTo(newShapeAppearanceModel.getBottomRightCorner().getCornerSize());
    assertThat(shapeAppearanceModel.getBottomLeftCorner().getCornerSize())
        .isEqualTo(newShapeAppearanceModel.getBottomLeftCorner().getCornerSize());
  }
}
