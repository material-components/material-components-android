/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.sidesheet;

import com.google.android.material.test.R;

import static android.graphics.Color.RED;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link com.google.android.material.sidesheet.SideSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class SideSheetCallbackTest {

  private View sideSheet;
  private SideSheetBehavior<View> sideSheetBehavior;

  @Before
  public void setUp() throws Exception {
    AppCompatActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    CoordinatorLayout coordinatorLayout =
        (CoordinatorLayout) activity.getLayoutInflater().inflate(R.layout.test_side_sheet, null);
    sideSheet = coordinatorLayout.findViewById(R.id.test_side_sheet_container);
    sideSheetBehavior = SideSheetBehavior.from(sideSheet);

    activity.setContentView(coordinatorLayout);

    // Wait until the layout is measured.
    shadowOf(Looper.getMainLooper()).idle();
  }

  @Test
  public void test_setSheetRedOnExpandWithCallback_sheetIsRedOnExpand() {
    // Create a callback and add it to the side sheet behavior.
    sideSheetBehavior.addCallback(
        new SideSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View sheet, int newState) {
            if (newState == SideSheetBehavior.STATE_EXPANDED) {
              sideSheet.setBackground(new ColorDrawable(RED));
            }
          }

          @Override
          public void onSlide(@NonNull View sheet, float slideOffset) {}
        });

    sideSheetBehavior.expand();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getState()).isEqualTo(SideSheetBehavior.STATE_EXPANDED);
    assertThat(sideSheet.getBackground()).isInstanceOf(ColorDrawable.class);
    assertThat(((ColorDrawable) sideSheet.getBackground()).getColor()).isEqualTo(RED);
  }

  @Test
  public void test_removeCallback_callbackIsRemoved() {
    SideSheetCallback sideSheetCallback = createExpandedRedSideSheetCallback();
    // Ensure that side sheet doesn't already have a background.
    sideSheet.setBackground(null);

    sideSheetBehavior.addCallback(sideSheetCallback);
    sideSheetBehavior.removeCallback(sideSheetCallback);

    sideSheetBehavior.expand();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getState()).isEqualTo(SideSheetBehavior.STATE_EXPANDED);
    assertThat(sideSheet.getBackground()).isNull();
  }

  private SideSheetCallback createExpandedRedSideSheetCallback() {
    return new SideSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View sheet, int newState) {
        if (newState == SideSheetBehavior.STATE_EXPANDED) {
          sideSheet.setBackground(new ColorDrawable(RED));
        }
      }

      @Override
      public void onSlide(@NonNull View sheet, float slideOffset) {}
    };
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
    }
  }
}
