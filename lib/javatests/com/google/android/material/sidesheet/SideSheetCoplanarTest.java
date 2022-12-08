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

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link SideSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class SideSheetCoplanarTest {

  private SideSheetBehavior<View> sideSheetBehavior;

  private static final int COPLANAR_SIBLING_ID_1 = R.id.coplanar_sibling_1;
  private static final int COPLANAR_SIBLING_ID_2 = R.id.coplanar_sibling_2;
  private View coplanarSibling1;
  private View coplanarSibling2;

  @Before
  public void setUp() throws Exception {
    AppCompatActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    CoordinatorLayout coordinatorLayout =
        (CoordinatorLayout)
            activity.getLayoutInflater().inflate(R.layout.test_coplanar_side_sheet, null);
    View sideSheet = coordinatorLayout.findViewById(R.id.test_coplanar_side_sheet_container);
    sideSheetBehavior = SideSheetBehavior.from(sideSheet);

    coplanarSibling1 = coordinatorLayout.findViewById(COPLANAR_SIBLING_ID_1);
    coplanarSibling2 = coordinatorLayout.findViewById(COPLANAR_SIBLING_ID_2);

    activity.setContentView(coordinatorLayout);

    // Wait until the layout is measured.
    shadowOf(Looper.getMainLooper()).idle();
  }

  @Test
  public void test_onSetUp_coplanarSiblingIsNull() {
    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isNull();
  }

  @Test
  public void test_setCoplanarSiblingByViewWithPreexistingViewById_viewIsSet() {
    sideSheetBehavior.setCoplanarSiblingViewId(COPLANAR_SIBLING_ID_1);
    sideSheetBehavior.setCoplanarSiblingView(coplanarSibling2);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(coplanarSibling2);
  }

  @Test
  public void test_setCoplanarSiblingByViewWithPreexistingView_viewIsSet() {
    sideSheetBehavior.setCoplanarSiblingView(coplanarSibling2);
    sideSheetBehavior.setCoplanarSiblingViewId(COPLANAR_SIBLING_ID_1);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(coplanarSibling1);
  }

  @Test
  public void test_setCoplanarSiblingById_viewIsSet() {
    sideSheetBehavior.setCoplanarSiblingViewId(COPLANAR_SIBLING_ID_2);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(coplanarSibling2);
  }

  @Test
  public void test_setCoplanarSiblingByView_viewIsSet() {
    sideSheetBehavior.setCoplanarSiblingView(coplanarSibling1);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(coplanarSibling1);
  }

  @Test
  public void test_setCoplanarSiblingViewNull_siblingIsNull() {
    sideSheetBehavior.setCoplanarSiblingView(coplanarSibling1);
    sideSheetBehavior.setCoplanarSiblingView(null);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(null);
  }

  @Test
  public void test_setCoplanarSiblingViewNullById_siblingIsNull() {
    sideSheetBehavior.setCoplanarSiblingViewId(COPLANAR_SIBLING_ID_1);
    sideSheetBehavior.setCoplanarSiblingViewId(View.NO_ID);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(sideSheetBehavior.getCoplanarSiblingView()).isEqualTo(null);
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
    }
  }
}
