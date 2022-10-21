/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.material.sidesheet;

import com.google.android.material.test.R;

import static com.google.android.material.sidesheet.Sheet.STATE_EXPANDED;
import static com.google.android.material.sidesheet.Sheet.STATE_HIDDEN;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link com.google.android.material.sidesheet.SideSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class SideSheetBehaviorTest {

  @NonNull TestActivity activity;

  @Before
  public void createActivity() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
  }

  @Test
  public void onInitialization_sheetIsHidden() {
    SideSheetBehavior<View> sideSheetBehavior = new SideSheetBehavior<>();

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void expand_ofInitializedSheet_yieldsExpandedState() {
    SideSheetBehavior<View> sideSheetBehavior = new SideSheetBehavior<>();

    expandSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_EXPANDED);
  }

  @Test
  public void expand_ofExpandedSheet_isIdempotent() {
    SideSheetBehavior<View> sideSheetBehavior = new SideSheetBehavior<>();
    expandSheet(sideSheetBehavior);
    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_EXPANDED);

    expandSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_EXPANDED);
  }

  @Test
  public void hide_ofExpandedSheet_yieldsHiddenState() {
    SideSheetBehavior<View> sideSheetBehavior = new SideSheetBehavior<>();
    expandSheet(sideSheetBehavior);
    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_EXPANDED);

    hideSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void hide_ofHiddenSheet_isIdempotent() {
    SideSheetBehavior<View> sideSheetBehavior = new SideSheetBehavior<>();
    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);

    hideSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);
  }

  private void expandSheet(SideSheetBehavior<View> sideSheetBehavior) {
    sideSheetBehavior.expand();
    shadowOf(Looper.getMainLooper()).idle();
  }

  private void hideSheet(SideSheetBehavior<View> sideSheetBehavior) {
    sideSheetBehavior.hide();
    shadowOf(Looper.getMainLooper()).idle();
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
    }
  }
}
