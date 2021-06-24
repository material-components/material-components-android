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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link MaterialDividerItemDecoration}. */
@RunWith(RobolectricTestRunner.class)
public final class MaterialDividerItemDecorationTest {

  private static final int DIVIDER_THICKNESS = 5;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Mock private RecyclerView.State state;
  @Mock private RecyclerView recyclerView;
  private Rect rect;
  private MaterialDividerItemDecoration divider;

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    rect = new Rect();
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
    assertThrows(IllegalArgumentException.class, () -> divider.setOrientation(3));
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
  public void getItemOffsets_verticalOrientation_returnsCorrectRect() {
    divider.getItemOffsets(rect, /* view= */ null, recyclerView, state);

    assertThat(rect).isEqualTo(new Rect(0, 0, 0, DIVIDER_THICKNESS));
  }

  @Test
  public void getItemOffsets_horizontalOrientation_returnsCorrectRect() {
    divider.setOrientation(LinearLayout.HORIZONTAL);

    divider.getItemOffsets(rect, /* view= */ null, recyclerView, state);

    assertThat(rect).isEqualTo(new Rect(0, 0, DIVIDER_THICKNESS, 0));
  }
}
