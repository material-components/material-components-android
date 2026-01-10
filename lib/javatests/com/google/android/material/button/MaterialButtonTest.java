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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link com.google.android.material.button.MaterialButton}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialButtonTest {

  private static final float LARGE_CORNER_SIZE = 40f;
  private static final int CUT_CORNER_FAMILY = CornerFamily.CUT;
  private static final Class<CutCornerTreatment> CUT_CORNER_FAMILY_CLASS = CutCornerTreatment.class;

  private final Context context = ApplicationProvider.getApplicationContext();
  private int callCount;

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
  }

  @Test
  public void testSetShapeAppearanceModel_setCornerRadius() {
    MaterialButton materialButton = new MaterialButton(context);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder().setAllCornerSizes(LARGE_CORNER_SIZE).build();

    materialButton.setShapeAppearanceModel(shapeAppearanceModel);

    ShapeAppearanceModel newShapeAppearanceModel = materialButton.getShapeAppearanceModel();

    assertThat(shapeAppearanceModel).isSameInstanceAs(newShapeAppearanceModel);
    assertThatCornerSizesMatch(shapeAppearanceModel, newShapeAppearanceModel);
  }

  @Test
  @Config(sdk = Config.OLDEST_SDK)
  public void testShapeRippleDrawableInLollipop() {
    MaterialButton materialButton = new MaterialButton(context);
    ShapeAppearanceModel shapeAppearanceModel = materialButton.getShapeAppearanceModel();

    materialButton.setCornerRadius((int) LARGE_CORNER_SIZE);
    ShapeAppearanceModel newShapeAppearanceModel = materialButton.getShapeAppearanceModel();
    assertThat(shapeAppearanceModel).isNotSameInstanceAs(newShapeAppearanceModel);
    assertThat(shapeAppearanceModel).isNotEqualTo(newShapeAppearanceModel);
  }

  @Test
  public void testSetShapeAppearanceModel() {
    MaterialButton materialButton = new MaterialButton(context);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder()
            .setAllCorners(CUT_CORNER_FAMILY, materialButton.getCornerRadius())
            .build();

    materialButton.setShapeAppearanceModel(shapeAppearanceModel);

    ShapeAppearanceModel newShapeAppearanceModel = materialButton.getShapeAppearanceModel();

    assertThatCornerSizesMatch(shapeAppearanceModel, newShapeAppearanceModel);
    assertThatCornerFamilyMatches(newShapeAppearanceModel, CUT_CORNER_FAMILY_CLASS);
  }

  @Test
  public void testGetShapeAppearanceModel() {
    MaterialButton materialButton = new MaterialButton(context);

    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder()
            .setAllCorners(CUT_CORNER_FAMILY, materialButton.getCornerRadius())
            .build();

    materialButton.setShapeAppearanceModel(shapeAppearanceModel);

    assertThatCornerFamilyMatches(
        materialButton.getShapeAppearanceModel(), CUT_CORNER_FAMILY_CLASS);
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
    assertThat(shapeAppearanceModel.getTopLeftCornerSize())
        .isEqualTo(newShapeAppearanceModel.getTopLeftCornerSize());
    assertThat(shapeAppearanceModel.getTopRightCornerSize())
        .isEqualTo(newShapeAppearanceModel.getTopRightCornerSize());
    assertThat(shapeAppearanceModel.getBottomRightCornerSize())
        .isEqualTo(newShapeAppearanceModel.getBottomRightCornerSize());
    assertThat(shapeAppearanceModel.getBottomLeftCornerSize())
        .isEqualTo(newShapeAppearanceModel.getBottomLeftCornerSize());
  }

  @Test
  public void setIcon_iconUpdated_whenCalledTwice() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setText("test");
    Drawable drawable1 = ContextCompat.getDrawable(context, android.R.drawable.btn_plus);
    setIcon(materialButton, makeMeasureSpec(200), drawable1);

    Drawable unwrapDrawable = DrawableCompat.unwrap(materialButton.getIcon());
    assertThat(unwrapDrawable).isEqualTo(drawable1);

    Drawable drawable2 = ContextCompat.getDrawable(context, android.R.drawable.btn_minus);
    setIcon(materialButton, makeMeasureSpec(200), drawable2);

    unwrapDrawable = DrawableCompat.unwrap(materialButton.getIcon());
    assertThat(unwrapDrawable).isEqualTo(drawable2);
  }

  @Test
  public void checkedStateTogglesOnClick() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setCheckable(true);

    materialButton.performClick();
    assertThat(materialButton.isChecked()).isTrue();

    materialButton.performClick();
    assertThat(materialButton.isChecked()).isFalse();
  }

  @Test
  public void togglingCheckedStateTogglesOnClick() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setCheckable(true);
    assertThat(materialButton.isChecked()).isFalse();

    materialButton.setToggleCheckedStateOnClick(false);
    materialButton.performClick();
    assertThat(materialButton.isChecked()).isFalse();
  }

  @Test
  public void setToggleCheckedStateOnClick() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setCheckable(true);

    assertThat(materialButton.isToggleCheckedStateOnClick()).isTrue();

    materialButton.setToggleCheckedStateOnClick(false);
    assertThat(materialButton.isToggleCheckedStateOnClick()).isFalse();
  }

  @Test
  public void getA11yClassName_whenCheckable() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setCheckable(true);

    assertThat(materialButton.getA11yClassName()).isEqualTo(CompoundButton.class.getName());
  }

  @Test
  public void getA11yClassName_whenNotCheckable() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setCheckable(false);

    assertThat(materialButton.getA11yClassName()).isEqualTo(Button.class.getName());
  }

  @Test
  public void getA11yClassName_whenSetToRadioButton() {
    MaterialButton materialButton = new MaterialButton(context);
    materialButton.setA11yClassName(RadioButton.class.getName());

    assertThat(materialButton.getA11yClassName()).isEqualTo(RadioButton.class.getName());
  }

  @Test
  @Config(minSdk = 23, maxSdk = 28)
  public void setIcon_iconNotUpdated_whenPositionChanged() {
    callCount = 0;
    MaterialButton materialButton =
        new MaterialButton(context) {

          @Override
          public void setCompoundDrawablesRelative(
              @Nullable Drawable left,
              @Nullable Drawable top,
              @Nullable Drawable right,
              @Nullable Drawable bottom) {
            super.setCompoundDrawablesRelative(left, top, right, bottom);
            callCount++;
          }
        };

    Drawable drawable = ContextCompat.getDrawable(context, android.R.drawable.btn_plus);
    setIcon(materialButton, makeMeasureSpec(200), drawable);
    setIcon(materialButton, makeMeasureSpec(300), drawable);

    assertThat(callCount).isEqualTo(1);
  }

  private static int makeMeasureSpec(int size) {
    return MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
  }

  private static void setIcon(MaterialButton materialButton, int measureSpec, Drawable drawable) {
    materialButton.setIcon(drawable);
    materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_START);
    materialButton.measure(measureSpec, measureSpec);
  }
}
