/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.divider;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link MaterialDividerItemDecoration}. */
@RunWith(RobolectricTestRunner.class)
public final class MaterialDividerItemDecorationTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final int DIVIDER_THICKNESS = 5;

  private final Context context = ApplicationProvider.getApplicationContext();

  private MaterialDividerItemDecoration divider;

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    divider = new MaterialDividerItemDecoration(context, LinearLayout.VERTICAL);
    divider.setDividerThickness(DIVIDER_THICKNESS);
  }

  @Test
  public void setOrientation_validInput_succeeds() {
    divider.setOrientation(LinearLayout.VERTICAL);

    assertThat(divider.getOrientation()).isEqualTo(LinearLayout.VERTICAL);
  }

  @Test
  public void setOrientation_invalidInput_throws() {
    thrown.expect(IllegalArgumentException.class);
    divider.setOrientation(3);
  }

  @Test
  public void setDividerThickness_succeeds() {
    divider.setDividerThickness(10);

    assertThat(divider.getDividerThickness()).isEqualTo(10);
  }

  @Test
  public void setDividerThicknessResource_succeeds() {
    divider.setDividerThicknessResource(context, R.dimen.test_dimen);

    assertThat(divider.getDividerThickness()).isEqualTo(2);
  }

  @Test
  public void setDividerColor_succeeds() {
    divider.setDividerColor(Color.RED);

    assertThat(divider.getDividerColor()).isEqualTo(Color.RED);
  }

  @Test
  public void setDividerColorResource_succeeds() {
    divider.setDividerColorResource(context, R.color.test_color);

    assertThat(divider.getDividerColor()).isEqualTo(Color.BLACK);
  }

  @Test
  public void setDividerInsetStart_succeeds() {
    divider.setDividerInsetStart(10);

    assertThat(divider.getDividerInsetStart()).isEqualTo(10);
  }

  @Test
  public void setDividerInsetStartResource_succeeds() {
    divider.setDividerInsetStartResource(context, R.dimen.test_dimen);

    assertThat(divider.getDividerInsetStart()).isEqualTo(2);
  }

  @Test
  public void setDividerInsetEnd_succeeds() {
    divider.setDividerInsetEnd(10);

    assertThat(divider.getDividerInsetEnd()).isEqualTo(10);
  }

  @Test
  public void setDividerInsetEndResource_succeeds() {
    divider.setDividerInsetEndResource(context, R.dimen.test_dimen);

    assertThat(divider.getDividerInsetEnd()).isEqualTo(2);
  }

  @Test
  public void isLastItemDecorated_isTrueByDefault() {
    assertThat(divider.isLastItemDecorated()).isTrue();
  }

  @Test
  public void setLastItemNotDecorated_succeeds() {
    divider.setLastItemDecorated(false);

    assertThat(divider.isLastItemDecorated()).isFalse();
  }
}
