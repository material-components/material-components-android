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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.chip.ChipGroup}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ChipGroupTest {

  private static final int CHIP_GROUP_SPACING = 4;
  private ChipGroup chipgroup;

  @Before
  public void themeApplicationContext() {
    RuntimeEnvironment.application.setTheme(
        R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_reflow_chipgroup, null);
    chipgroup = inflated.findViewById(R.id.chip_group);
  }

  @Test
  public void testSetChipSpacing() {
    chipgroup.setChipSpacing(CHIP_GROUP_SPACING);
    assertEquals(chipgroup.getChipSpacingHorizontal(), chipgroup.getChipSpacingVertical());
    assertEquals(CHIP_GROUP_SPACING, chipgroup.getChipSpacingHorizontal());
  }

  @Test
  public void testSelection() {
    chipgroup.setSingleSelection(true);
    assertTrue(chipgroup.isSingleSelection());
    assertEquals(View.NO_ID, chipgroup.getCheckedChipId());
    int chipId = chipgroup.getChildAt(0).getId();
    assertNotEquals(View.NO_ID, chipId);
    chipgroup.check(chipId);
    assertEquals(chipId, chipgroup.getCheckedChipId());
    chipgroup.clearCheck();
    assertEquals(View.NO_ID, chipgroup.getCheckedChipId());
  }
}
