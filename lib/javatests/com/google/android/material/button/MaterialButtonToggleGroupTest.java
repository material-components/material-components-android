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

package com.google.android.material.button;

import com.google.android.material.R;

import static android.view.View.GONE;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.ShapeAppearanceModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link com.google.android.material.button.MaterialButtonToggleGroup}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class MaterialButtonToggleGroupTest {

  private static final float CORNER_SIZE = 10f;
  private final Context context = ApplicationProvider.getApplicationContext();

  private MaterialButtonToggleGroup toggleGroup;

  @Before
  public void setup() {
    // Set up app material theme
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);

    // Set up test buttons in toggle group
    toggleGroup = new MaterialButtonToggleGroup(context);
    for (int i = 0; i < 3; ++i) {
      MaterialButton child = new MaterialButton(context);
      child.setShapeAppearanceModel(child.getShapeAppearanceModel().withCornerSize(CORNER_SIZE));
      toggleGroup.addView(child, i);
      getInstrumentation().waitForIdleSync();
    }
  }

  @Test
  public void correctShapeAppearances_inToggle_afterAdding() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    toggleGroup.updateChildShapes();
    assertShapeAppearance(firstChild.getShapeAppearanceModel(), CORNER_SIZE, CORNER_SIZE, 0, 0);
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), 0, 0, 0, 0);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 0, 0, CORNER_SIZE, CORNER_SIZE);
  }

  @Test
  public void correctShapeAppearances_inToggle_afterAddingInVertical() {
    toggleGroup.setOrientation(LinearLayout.VERTICAL);
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    toggleGroup.updateChildShapes();
    assertShapeAppearance(firstChild.getShapeAppearanceModel(), CORNER_SIZE, 0, CORNER_SIZE, 0);
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), 0, 0, 0, 0);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 0, CORNER_SIZE, 0, CORNER_SIZE);
  }

  @Test
  public void correctShapeAppearances_inToggle_afterSettingViewToGone() {
    toggleGroup.setOrientation(LinearLayout.VERTICAL);
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    firstChild.setVisibility(GONE);
    toggleGroup.updateChildShapes();

    // Now middle and end child has rounded corners.
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), CORNER_SIZE, 0, CORNER_SIZE, 0);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 0, CORNER_SIZE, 0, CORNER_SIZE);
  }

  private static void assertShapeAppearance(
      ShapeAppearanceModel shapeAppearanceModel, float... corners) {
    RectF ignore = new RectF();
    assertThat(
        new float[]{
            shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(ignore),
            shapeAppearanceModel.getBottomLeftCornerSize().getCornerSize(ignore),
            shapeAppearanceModel.getTopRightCornerSize().getCornerSize(ignore),
            shapeAppearanceModel.getBottomRightCornerSize().getCornerSize(ignore)
        })
        .isEqualTo(corners);
  }

  @Test
  public void singleSelection_withSelectionRequired_doesNotUnSelect() {
    toggleGroup.setSelectionRequired(true);
    toggleGroup.setSingleSelection(true);

    View button = toggleGroup.getChildAt(0);
    button.performClick();
    button.performClick();

    assertThat(((Checkable) button).isChecked()).isTrue();
  }

  @Test
  public void singleSelection_withoutSelectionRequired_unSelects() {
    toggleGroup.setSingleSelection(true);
    toggleGroup.setSelectionRequired(false);

    View button = toggleGroup.getChildAt(0);
    button.performClick();
    button.performClick();

    assertThat(((Checkable) button).isChecked()).isFalse();
  }

  @Test
  public void multiSelection_withSelectionRequired_unSelectsIfTwo() {
    toggleGroup.setSingleSelection(false);
    toggleGroup.setSelectionRequired(true);

    View first = toggleGroup.getChildAt(0);
    View second = toggleGroup.getChildAt(1);
    first.performClick();
    second.performClick();
    second.performClick();

    // first button is selected
    assertThat(((Checkable) first).isChecked()).isTrue();
    assertThat(((Checkable) second).isChecked()).isFalse();
  }
}
