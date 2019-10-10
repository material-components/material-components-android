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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

import java.util.List;

/** Tests for {@link com.google.android.material.chip.ChipGroup}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ChipGroupTest {

  private static final int CHIP_GROUP_SPACING = 4;
  private ChipGroup chipgroup;

  @Before
  public void themeApplicationContext() {
    ApplicationProvider.getApplicationContext().setTheme(
        R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
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
    assertThat(chipgroup.getCheckedChipId()).isEqualTo(View.NO_ID);
    int chipId = chipgroup.getChildAt(0).getId();
    assertThat(chipId).isNotEqualTo(View.NO_ID);
    chipgroup.check(chipId);
    assertThat(chipId).isEqualTo(chipgroup.getCheckedChipId());
    chipgroup.clearCheck();
    assertThat(chipgroup.getCheckedChipId()).isEqualTo(View.NO_ID);
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
}
