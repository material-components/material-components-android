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

import com.google.android.material.test.R;

import static android.view.View.GONE;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@LooperMode(LooperMode.Mode.LEGACY)
/** Tests for {@link com.google.android.material.button.MaterialButtonToggleGroup}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
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
  public void correctShapeAppearances_inToggle_afterAdding_withInnerCorner() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    toggleGroup.setInnerCornerSize(new AbsoluteCornerSize(5));
    assertShapeAppearance(firstChild.getShapeAppearanceModel(), CORNER_SIZE, CORNER_SIZE, 5, 5);
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), 5, 5, 5, 5);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 5, 5, CORNER_SIZE, CORNER_SIZE);
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
  public void correctShapeAppearances_inToggle_afterAddingInVertical_withInnerCorner() {
    toggleGroup.setOrientation(LinearLayout.VERTICAL);
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    toggleGroup.setInnerCornerSize(new AbsoluteCornerSize(5));
    assertShapeAppearance(firstChild.getShapeAppearanceModel(), CORNER_SIZE, 5, CORNER_SIZE, 5);
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), 5, 5, 5, 5);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 5, CORNER_SIZE, 5, CORNER_SIZE);
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
  public void correctShapeAppearances_inToggle_afterSettingViewToGone_withInnerCorner() {
    toggleGroup.setOrientation(LinearLayout.VERTICAL);
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    firstChild.setVisibility(GONE);
    toggleGroup.setInnerCornerSize(new AbsoluteCornerSize(5));

    // Now middle and end child has rounded corners.
    assertShapeAppearance(middleChild.getShapeAppearanceModel(), CORNER_SIZE, 5, CORNER_SIZE, 5);
    assertShapeAppearance(lastChild.getShapeAppearanceModel(), 5, CORNER_SIZE, 5, CORNER_SIZE);
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

  @Test
  public void correctShapeAppearances_inToggle_whenOneVisibleButton_withInnerCorner() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    firstChild.setVisibility(GONE);
    middleChild.setVisibility(GONE);
    toggleGroup.setInnerCornerSize(new AbsoluteCornerSize(5));
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
    AccessibilityNodeInfo groupInfo = AccessibilityNodeInfo.obtain();
    toggleGroup.onInitializeAccessibilityNodeInfo(groupInfo);

    AccessibilityNodeInfo.CollectionInfo collectionInfo = groupInfo.getCollectionInfo();
    assertEquals(3, collectionInfo.getColumnCount());
    assertEquals(1, collectionInfo.getRowCount());

    MaterialButton secondChild = (MaterialButton) toggleGroup.getChildAt(1);
    secondChild.setChecked(true);
    AccessibilityNodeInfo buttonInfo = AccessibilityNodeInfo.obtain();
    secondChild.onInitializeAccessibilityNodeInfo(buttonInfo);

    AccessibilityNodeInfo.CollectionItemInfo itemInfo = buttonInfo.getCollectionItemInfo();
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
  public void singleSelection_doesNotMultiSelect() {
    toggleGroup.setSingleSelection(true);

    View button1 = toggleGroup.getChildAt(0);
    button1.performClick();
    View button2 = toggleGroup.getChildAt(1);
    button2.performClick();

    assertThat(((Checkable) button1).isChecked()).isFalse();
    assertThat(((Checkable) button2).isChecked()).isTrue();
  }

  @Test
  public void singleSelection_doesNotMultiSelect_programmatically() {
    toggleGroup.setSingleSelection(true);

    View button1 = toggleGroup.getChildAt(0);
    int id1 = View.generateViewId();
    button1.setId(id1);

    View button2 = toggleGroup.getChildAt(1);
    int id2 = View.generateViewId();
    button2.setId(id2);

    toggleGroup.check(id1);
    toggleGroup.check(id2);

    assertThat(((Checkable) button1).isChecked()).isFalse();
    assertThat(((Checkable) button2).isChecked()).isTrue();
  }

  @Test
  public void multiSelection_correctSelectedIds() {
    toggleGroup.setSingleSelection(false);

    View button1 = toggleGroup.getChildAt(0);
    int id1 = View.generateViewId();
    button1.setId(id1);

    View button2 = toggleGroup.getChildAt(1);
    int id2 = View.generateViewId();
    button2.setId(id2);

    toggleGroup.check(id1);
    toggleGroup.check(id2);

    List<Integer> checkedIds = toggleGroup.getCheckedButtonIds();
    assertThat(checkedIds.contains(id1)).isTrue();
    assertThat(checkedIds.contains(id2)).isTrue();
    assertThat(checkedIds.size()).isEqualTo(2);
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
    int id = View.generateViewId();
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

  @Test
  public void setEnable_false_disablesChildButtons() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);
    firstChild.setEnabled(true);
    middleChild.setEnabled(true);
    lastChild.setEnabled(true);

    toggleGroup.setEnabled(false);

    assertThat(firstChild.isEnabled()).isFalse();
    assertThat(middleChild.isEnabled()).isFalse();
    assertThat(lastChild.isEnabled()).isFalse();
  }

  @Test
  public void setEnable_true_enablesChildButtons() {
    MaterialButton firstChild = (MaterialButton) toggleGroup.getChildAt(0);
    MaterialButton middleChild = (MaterialButton) toggleGroup.getChildAt(1);
    MaterialButton lastChild = (MaterialButton) toggleGroup.getChildAt(2);

    firstChild.setEnabled(false);
    middleChild.setEnabled(false);
    lastChild.setEnabled(false);

    toggleGroup.setEnabled(true);

    assertThat(firstChild.isEnabled()).isTrue();
    assertThat(middleChild.isEnabled()).isTrue();
    assertThat(lastChild.isEnabled()).isTrue();
  }

  @Test
  public void singleSelection_hasRadioButtonA11yClassName() {
    toggleGroup.setSingleSelection(true);
    View button1 = toggleGroup.getChildAt(0);

    assertThat(((MaterialButton) button1).getA11yClassName())
        .isEqualTo(RadioButton.class.getName());
  }

  @Test
  public void multiSelection_hasToggleButtonA11yClassName() {
    toggleGroup.setSingleSelection(false);
    View button1 = toggleGroup.getChildAt(0);

    assertThat(((MaterialButton) button1).getA11yClassName())
        .isEqualTo(ToggleButton.class.getName());
  }
}
