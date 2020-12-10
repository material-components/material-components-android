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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.RectF;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import com.google.android.material.shape.ShapeAppearanceModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@LooperMode(LooperMode.Mode.LEGACY)
/** Tests for {@link com.google.android.material.button.MaterialButtonToggleGroup}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class MaterialButtonToggleGroupTest {

  private static final float CORNER_SIZE = 10f;
  private final Context context = ApplicationProvider.getApplicationContext();

  private MaterialButtonToggleGroup toggleGroup;
  private int checkedChangeCallCount;

  private void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
  }

  @Before
  public void createToggleGroupWithButtons() {
    themeApplicationContext();
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

  @Test
  public void correctShapeAppearances_inToggle_whenOneVisibleButton() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    firstChild.setVisibility(GONE);
    middleChild.setVisibility(GONE);
    toggleGroup.updateChildShapes();
    // Last child has default shape appearance.
    assertShapeAppearance(
        lastChild.getShapeAppearanceModel(), CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
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
  @Config(sdk = 23)
  public void onInitializeAccessibilityNodeInfo() {
    AccessibilityNodeInfoCompat groupInfoCompat = AccessibilityNodeInfoCompat.obtain();
    ViewCompat.onInitializeAccessibilityNodeInfo(toggleGroup, groupInfoCompat);

    CollectionInfoCompat collectionInfo = groupInfoCompat.getCollectionInfo();
    assertEquals(3, collectionInfo.getColumnCount());
    assertEquals(1, collectionInfo.getRowCount());

    MaterialButton secondChild = (MaterialButton) toggleGroup.getChildAt(1);
    secondChild.setChecked(true);
    AccessibilityNodeInfoCompat buttonInfoCompat = AccessibilityNodeInfoCompat.obtain();
    ViewCompat.onInitializeAccessibilityNodeInfo(secondChild, buttonInfoCompat);

    CollectionItemInfoCompat itemInfo = buttonInfoCompat.getCollectionItemInfo();
    assertEquals(1, itemInfo.getColumnIndex());
    assertEquals(0, itemInfo.getRowIndex());
    assertTrue(itemInfo.isSelected());
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


  @Test
  public void singleSelection_withSelectionRequired_correctCheckedIdWithTwoTaps() {
    int id = singleSelection_withSelectedRequired_setup();
    View child = toggleGroup.findViewById(id);
    child.performClick();
    child.performClick();

    // child button is selected
    assertThat(toggleGroup.getCheckedButtonId()).isEqualTo(id);
  }

  private int singleSelection_withSelectedRequired_setup() {
    toggleGroup.setSingleSelection(true);
    toggleGroup.setSelectionRequired(true);

    View child = toggleGroup.getChildAt(1);
    int id = ViewCompat.generateViewId();
    child.setId(id);
    return id;
  }

  @Test
  public void singleSelection_withSelectionRequired_callsListenerOnlyOnce() {
    int id = singleSelection_withSelectedRequired_setup();
    View child = toggleGroup.findViewById(id);
    checkedChangeCallCount = 0;

    OnButtonCheckedListener listener =
        new OnButtonCheckedListener() {
          @Override
          public void onButtonChecked(
              MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
            checkedChangeCallCount++;
          }
        };
    toggleGroup.addOnButtonCheckedListener(listener);
    child.performClick();
    child.performClick();
    assertThat(checkedChangeCallCount).isEqualTo(1);
  }

  @Test
  public void singleSelection_withSelectionRequired_callsListenerOnFirstPressAndClick() {
    int id = singleSelection_withSelectedRequired_setup();
    View child = toggleGroup.findViewById(id);
    checkedChangeCallCount = 0;

    OnButtonCheckedListener listener =
        new OnButtonCheckedListener() {
          @Override
          public void onButtonChecked(
              MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
            checkedChangeCallCount++;
          }
        };
    toggleGroup.addOnButtonCheckedListener(listener);
    child.setPressed(true);
    child.performClick();
    assertThat(checkedChangeCallCount).isEqualTo(1);
  }
}
