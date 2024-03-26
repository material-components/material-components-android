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
package com.google.android.material.chip;

import com.google.android.material.test.R;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.chip.ChipGroup.OnCheckedStateChangeListener;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link com.google.android.material.chip.ChipGroup}. */
@RunWith(RobolectricTestRunner.class)
public class ChipGroupTest {

  private static final int CHIP_GROUP_SPACING = 4;
  private ChipGroup chipgroup;
  private int checkedChangeCallCount;
  private List<Integer> checkedIds;
  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_reflow_chipgroup, null);
    chipgroup = inflated.findViewById(R.id.chip_group);
  }

  @Test
  public void testSetChipSpacing() {
    chipgroup.setChipSpacing(CHIP_GROUP_SPACING);
    assertThat(chipgroup.getChipSpacingHorizontal()).isEqualTo(chipgroup.getChipSpacingVertical());
    assertThat(chipgroup.getChipSpacingHorizontal()).isEqualTo(CHIP_GROUP_SPACING);
  }

  @Test
  public void testSelection() {
    chipgroup.setSingleSelection(true);
    assertThat(chipgroup.isSingleSelection()).isTrue();
    assertThat(chipgroup.getCheckedChipIds()).isEmpty();
    int chipId = chipgroup.getChildAt(0).getId();
    assertThat(chipId).isNotEqualTo(View.NO_ID);
    chipgroup.check(chipId);
    assertThat(chipId).isEqualTo(chipgroup.getCheckedChipIds().get(0));
    chipgroup.clearCheck();
    assertThat(chipgroup.getCheckedChipIds()).isEmpty();
  }

  @Test
  public void testMultipleCheckedChip() {
    int chip1Id = chipgroup.getChildAt(0).getId();
    chipgroup.check(chip1Id);
    int chip2Id = chipgroup.getChildAt(1).getId();
    chipgroup.check(chip2Id);
    assertThat(chipgroup.getCheckedChipIds()).hasSize(2);
  }

  @Test
  public void testSingleCheckedChip() {
    chipgroup.setSingleSelection(true);
    int chipId = chipgroup.getChildAt(2).getId();
    chipgroup.check(chipId);
    assertThat(chipgroup.getCheckedChipIds()).hasSize(1);
    //check that for a single checked chip methods should be equivalent
    Integer checkedId1 = chipgroup.getCheckedChipIds().get(0);
    int checkedId2 = chipgroup.getCheckedChipId();
    assertThat(checkedId1).isEqualTo(checkedId2);
  }

  @Test
  public void testSingleSelection_addingCheckedChipWithoutId() {
    chipgroup.setSingleSelection(true);
    int chipId = chipgroup.getChildAt(2).getId();
    chipgroup.check(chipId);

    Chip chipNotChecked = new Chip(context);
    chipgroup.addView(chipNotChecked);
    assertThat(chipgroup.getCheckedChipIds()).hasSize(1);
    int checkedId = chipgroup.getCheckedChipIds().get(0);
    assertThat(checkedId).isEqualTo(chipId);

    // Add a checked Chip
    Chip chipChecked = new Chip(context);
    chipChecked.setCheckable(true);
    chipChecked.setChecked(true);
    chipgroup.addView(chipChecked);

    int newChipId = chipChecked.getId();
    assertThat(chipgroup.getCheckedChipIds()).hasSize(1);
    int checkedId2 = chipgroup.getCheckedChipIds().get(0);
    assertThat(checkedId2).isEqualTo(newChipId);
  }

  @Test
  public void singleSelection_withSelectionRequired_doesNotUnSelect() {
    chipgroup.setSelectionRequired(true);
    chipgroup.setSingleSelection(true);

    View chip = chipgroup.getChildAt(0);
    chip.performClick();
    chip.performClick();

    assertThat(((Chip) chip).isChecked()).isTrue();
  }

  @Test
  public void singleSelection_withSelectionRequired_callsListenerOnce() {
    chipgroup.setSelectionRequired(true);
    chipgroup.setSingleSelection(true);
    checkedChangeCallCount = 0;

    chipgroup.setOnCheckedStateChangeListener(
        new OnCheckedStateChangeListener() {
          @Override
          public void onCheckedChanged(ChipGroup group, List<Integer> checkedIds) {
            checkedChangeCallCount++;
          }
        });

    View chip = chipgroup.getChildAt(0);
    chip.performClick();
    chip.performClick();

    assertThat(checkedChangeCallCount).isEqualTo(1);
  }

  @Test
  public void singleSelection_withoutSelectionRequired_unSelects() {
    chipgroup.setSingleSelection(true);
    chipgroup.setSelectionRequired(false);

    View chip = chipgroup.getChildAt(0);
    chip.performClick();
    chip.performClick();

    assertThat(((Chip) chip).isChecked()).isFalse();
  }

  @Test
  public void multipleSelection_callsListener() {
    chipgroup.setSingleSelection(false);

    chipgroup.setOnCheckedStateChangeListener(
        new OnCheckedStateChangeListener() {
          @Override
          public void onCheckedChanged(ChipGroup group, List<Integer> checkedIds) {
            checkedChangeCallCount++;
            ChipGroupTest.this.checkedIds = checkedIds;
          }
        });

    View first = chipgroup.getChildAt(0);
    View second = chipgroup.getChildAt(1);

    first.performClick();

    assertThat(checkedChangeCallCount).isEqualTo(1);
    assertThat(checkedIds).hasSize(1);
    assertThat(checkedIds).contains(first.getId());

    second.performClick();

    assertThat(checkedChangeCallCount).isEqualTo(2);
    assertThat(checkedIds).hasSize(2);
    assertThat(checkedIds).contains(first.getId());
    assertThat(checkedIds).contains(second.getId());
  }

  @Test
  public void multipleSelection_chipListener() {
    chipgroup.setSingleSelection(false);

    Chip first = (Chip) chipgroup.getChildAt(0);
    first.setOnCheckedChangeListener(this::onChipCheckedStateChanged);

    Chip second = (Chip) chipgroup.getChildAt(1);
    second.setOnCheckedChangeListener(this::onChipCheckedStateChanged);

    first.performClick();
    getInstrumentation().waitForIdleSync();

    assertThat(checkedChangeCallCount).isEqualTo(1);
    assertThat(checkedIds).containsExactly(first.getId());

    second.performClick();
    getInstrumentation().waitForIdleSync();

    assertThat(checkedChangeCallCount).isEqualTo(2);
    assertThat(checkedIds).containsExactly(first.getId(), second.getId());
  }

  @Test
  public void multiSelection_withSelectionRequired_unSelectsIfTwo() {
    chipgroup.setSingleSelection(false);
    chipgroup.setSelectionRequired(true);

    View first = chipgroup.getChildAt(0);
    View second = chipgroup.getChildAt(1);
    first.performClick();

    second.performClick();
    second.performClick();

    // first button is selected
    assertThat(((Chip) first).isChecked()).isTrue();
    assertThat(((Chip) second).isChecked()).isFalse();
  }

  @Test
  @Config(minSdk = 23, maxSdk = 28)
  public void isSingleLine_initializesAccessibilityNodeInfo() {
    chipgroup.setSingleLine(true);
    AccessibilityNodeInfo groupInfo = AccessibilityNodeInfo.obtain();
    // onLayout must be triggered for rowCount
    chipgroup.layout(0, 0, 100, 100);
    chipgroup.onInitializeAccessibilityNodeInfo(groupInfo);

    AccessibilityNodeInfo.CollectionInfo collectionInfo = groupInfo.getCollectionInfo();
    assertEquals(chipgroup.getChildCount(), collectionInfo.getColumnCount());
    assertEquals(1, collectionInfo.getRowCount());

    Chip secondChild = (Chip) chipgroup.getChildAt(1);
    secondChild.setChecked(true);
    AccessibilityNodeInfo chipInfo = AccessibilityNodeInfo.obtain();
    secondChild.onInitializeAccessibilityNodeInfo(chipInfo);

    AccessibilityNodeInfo.CollectionItemInfo itemInfo = chipInfo.getCollectionItemInfo();
    assertEquals(1, itemInfo.getColumnIndex());
    assertEquals(0, itemInfo.getRowIndex());
    assertTrue(itemInfo.isSelected());
    assertEquals(1, chipgroup.getIndexOfChip(secondChild));
  }

  @Test
  @Config(minSdk = 23, maxSdk = 28)
  public void isSingleLine_initializesAccessibilityNodeInfo_invisibleChip() {
    chipgroup.setSingleLine(true);
    AccessibilityNodeInfo groupInfo = AccessibilityNodeInfo.obtain();
    // onLayout must be triggered for rowCount
    chipgroup.layout(0, 0, 100, 100);
    chipgroup.onInitializeAccessibilityNodeInfo(groupInfo);

    AccessibilityNodeInfo.CollectionInfo collectionInfo = groupInfo.getCollectionInfo();
    assertEquals(chipgroup.getChildCount(), collectionInfo.getColumnCount());
    assertEquals(1, collectionInfo.getRowCount());

    Chip firstChild = (Chip) chipgroup.getChildAt(0);
    firstChild.setVisibility(INVISIBLE);
    Chip secondChild = (Chip) chipgroup.getChildAt(1);
    secondChild.setVisibility(GONE);
    Chip thirdChild = (Chip) chipgroup.getChildAt(2);
    AccessibilityNodeInfo chipInfo = AccessibilityNodeInfo.obtain();
    thirdChild.onInitializeAccessibilityNodeInfo(chipInfo);

    AccessibilityNodeInfo.CollectionItemInfo itemInfo = chipInfo.getCollectionItemInfo();
    assertEquals(0, itemInfo.getColumnIndex());
    assertEquals(0, itemInfo.getRowIndex());
    assertEquals(-1, chipgroup.getIndexOfChip(firstChild));
    assertEquals(-1, chipgroup.getIndexOfChip(secondChild));
    assertEquals(0, chipgroup.getIndexOfChip(thirdChild));

    chipgroup.onInitializeAccessibilityNodeInfo(groupInfo);
    assertEquals(1, groupInfo.getCollectionInfo().getColumnCount());
  }

  @Test
  @Config(minSdk = 23, maxSdk = 28)
  public void isNotSingleLine_initializesAccessibilityNodeInfo() {
    AccessibilityNodeInfo groupInfo = AccessibilityNodeInfo.obtain();
    // onLayout must be triggered for rowCount
    chipgroup.layout(0, 0, 10, 100);
    chipgroup.onInitializeAccessibilityNodeInfo(groupInfo);

    AccessibilityNodeInfo.CollectionInfo collectionInfo = groupInfo.getCollectionInfo();
    assertEquals(-1, collectionInfo.getColumnCount());
    assertEquals(2, collectionInfo.getRowCount());

    Chip secondChild = (Chip) chipgroup.getChildAt(2);
    secondChild.setChecked(true);
    AccessibilityNodeInfo chipInfo = AccessibilityNodeInfo.obtain();
    secondChild.onInitializeAccessibilityNodeInfo(chipInfo);

    AccessibilityNodeInfo.CollectionItemInfo itemInfo = chipInfo.getCollectionItemInfo();
    assertEquals(-1, itemInfo.getColumnIndex());
    assertEquals(1, itemInfo.getRowIndex());
    assertTrue(itemInfo.isSelected());
  }

  @Test
  public void getChipAccessibilityClassName_multipleChecked_buttonName() {
    Chip chip = (Chip) chipgroup.getChildAt(0);
    assertEquals("android.widget.Button", chip.getAccessibilityClassName().toString());
  }

  @Test
  public void getChipAccessibilityClassName_singleChecked_radioButtonName() {
    chipgroup.setSingleSelection(true);
    Chip chip = (Chip) chipgroup.getChildAt(0);
    assertEquals("android.widget.RadioButton", chip.getAccessibilityClassName().toString());
  }

  private void onChipCheckedStateChanged(CompoundButton chip, boolean checked) {
    checkedChangeCallCount++;
    checkedIds = chipgroup.getCheckedChipIds();
  }
}
