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
import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link SideSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class SideSheetDialogTest {

  @NonNull TestActivity activity;
  @NonNull SideSheetDialog sideSheetDialog;

  @Before
  public void createActivity() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    sideSheetDialog = activity.getSideSheetDialog();
  }

  @Test
  public void onInitialization_dialogIsNotShowing() {
    assertThat(sideSheetDialog.isShowing()).isFalse();
  }

  @Test
  public void onInitialization_dialogIsHidden() {
    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void show_ofInitializedDialog_yieldsExpandedState() {
    showSideSheetDialog();

    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_EXPANDED);
  }

  @Test
  public void hide_ofInitializedDialog_yieldsHiddenState() {
    hideSideSheetDialog();

    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void show_ofExpandedDialog_dialogIsIdempotent() {
    showSideSheetDialog();
    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_EXPANDED);

    showSideSheetDialog();

    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_EXPANDED);
  }

  @Test
  public void hide_ofHiddenDialog_dialogIsIdempotent() {
    hideSideSheetDialog();
    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_HIDDEN);

    hideSideSheetDialog();

    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_HIDDEN);
  }

  @Test
  public void expandedOffset_isIdempotent() {
    showSideSheetDialog();
    assertThat(sideSheetDialog.getBehavior().getState()).isEqualTo(STATE_EXPANDED);
    int expandedOffsetOnInitialization = sideSheetDialog.getBehavior().getExpandedOffset();
    hideSideSheetDialog();

    showSideSheetDialog();

    assertThat(sideSheetDialog.getBehavior().getExpandedOffset())
        .isEqualTo(expandedOffsetOnInitialization);
  }

  @Test
  public void click_onScrim_cancelsSheet() {
    View scrim = sideSheetDialog.findViewById(R.id.touch_outside);
    assertThat(scrim).isNotNull();

    showSideSheetDialog();
    assertThat(sideSheetDialog.isShowing()).isTrue();
    assertThat(scrim.getVisibility()).isEqualTo(View.VISIBLE);

    // Click outside the side sheet.
    scrim.performClick();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetDialog.isShowing()).isFalse();
  }

  @UiThread
  private void showSideSheetDialog() {
    activity.runOnUiThread(
        () -> {
          sideSheetDialog.show();
          shadowOf(Looper.getMainLooper()).idle();
        });
  }

  @UiThread
  private void hideSideSheetDialog() {
    activity.runOnUiThread(
        () -> {
          sideSheetDialog.dismiss();
          shadowOf(Looper.getMainLooper()).idle();
        });
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
    }

    @NonNull
    private SideSheetDialog getSideSheetDialog() {
      SideSheetDialog sideSheetDialog = new SideSheetDialog(this);
      AppCompatTextView textView = new AppCompatTextView(this);
      textView.setText(new StringBuilder().append("Side sheet dialog test"));
      sideSheetDialog.setContentView(textView);
      shadowOf(Looper.getMainLooper()).idle();
      return sideSheetDialog;
    }
  }
}
