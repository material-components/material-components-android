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

import static com.google.android.material.sidesheet.Sheet.STATE_DRAGGING;
import static com.google.android.material.sidesheet.Sheet.STATE_EXPANDED;
import static com.google.android.material.sidesheet.Sheet.STATE_HIDDEN;
import static com.google.android.material.sidesheet.Sheet.STATE_SETTLING;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link com.google.android.material.sidesheet.SideSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class SideSheetBehaviorTest {

  @NonNull TestActivity activity;

  private View sideSheet;
  private SideSheetBehavior<View> sideSheetBehavior;

  @Before
  public void createActivity() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    CoordinatorLayout coordinatorLayout =
        (CoordinatorLayout) activity.getLayoutInflater().inflate(R.layout.test_side_sheet, null);
    sideSheet = coordinatorLayout.findViewById(R.id.test_side_sheet_container);
    sideSheetBehavior = SideSheetBehavior.from(sideSheet);
    activity.setContentView(coordinatorLayout);

    // Wait until the layout is measured.
    shadowOf(Looper.getMainLooper()).idle();
  }

  @Test
  public void onInitialization_sheetIsHidden() {
    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void expand_ofInitializedSheet_yieldsExpandedState() {
    expandSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_EXPANDED);
  }

  @Test
  public void expand_ofExpandedSheet_isIdempotent() {
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
    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);

    hideSheet(sideSheetBehavior);

    assertThat(sideSheetBehavior.getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void onInitialization_sheetIsInvisible() {
    assertThat(sideSheet.getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void show_ofHiddenSheet_sheetIsVisible() {
    expandSheet(sideSheetBehavior);

    assertThat(sideSheet.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void hide_ofExpandedSheet_sheetIsInvisible() {
    expandSheet(sideSheetBehavior);
    hideSheet(sideSheetBehavior);

    assertThat(sideSheet.getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void drag_ofExpandedSheet_sheetIsVisible() {
    expandSheet(sideSheetBehavior);

    sideSheetBehavior.setStateInternal(STATE_DRAGGING);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheet.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void settle_ofHiddenSheet_sheetIsVisible() {
    // Sheet is hidden on initialization.
    sideSheetBehavior.setStateInternal(STATE_SETTLING);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheet.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void settle_ofExpandedSheet_sheetIsVisible() {
    expandSheet(sideSheetBehavior);

    sideSheetBehavior.setStateInternal(STATE_SETTLING);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheet.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void setAccessibilityPaneTitle_ofDefaultSheet_customTitleIsUsed() {
    String defaultAccessibilityPaneTitle =
        String.valueOf(ViewCompat.getAccessibilityPaneTitle(sideSheet));
    shadowOf(Looper.getMainLooper()).idle();

    String customAccessibilityPaneTitle = "Custom side sheet accessibility pane title";

    ViewCompat.setAccessibilityPaneTitle(sideSheet, customAccessibilityPaneTitle);
    shadowOf(Looper.getMainLooper()).idle();

    String updatedAccessibilityPaneTitle =
        String.valueOf(ViewCompat.getAccessibilityPaneTitle(sideSheet));
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(defaultAccessibilityPaneTitle).isNotNull();
    assertThat(updatedAccessibilityPaneTitle).isEqualTo(customAccessibilityPaneTitle);
    assertThat(updatedAccessibilityPaneTitle).isNotEqualTo(defaultAccessibilityPaneTitle);
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
