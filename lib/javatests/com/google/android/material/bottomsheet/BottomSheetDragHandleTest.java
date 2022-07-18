/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.bottomsheet;

import com.google.android.material.test.R;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BottomSheetDragHandleTest {
  private AccessibilityManager accessibilityManager;
  private TestActivity activity;
  private BottomSheetDragHandleView dragHandleView;

  @Before
  public void setUp() throws Exception {
    accessibilityManager =
        (AccessibilityManager)
            ApplicationProvider.getApplicationContext().getSystemService(ACCESSIBILITY_SERVICE);

    activity = Robolectric.buildActivity(TestActivity.class).setup().get();

    dragHandleView = new BottomSheetDragHandleView(activity);
  }

  @Test
  public void test_notInteractableWhenDetachedAndAccessibilityDisabled() {
    assertImportantForAccessibility(false);
    assertThat(dragHandleView.isClickable()).isFalse();
  }

  @Test
  public void test_notInteractableWhenDetachedAndAccessibilityEnabled() {
    shadowOf(accessibilityManager).setEnabled(true);
    assertImportantForAccessibility(false);
    assertThat(dragHandleView.isClickable()).isFalse();
  }

  @Test
  public void test_notInteractableWhenAttachedAndAccessibilityDisabled() {
    activity.addViewToBottomSheet(dragHandleView);
    assertImportantForAccessibility(true);
    assertThat(dragHandleView.isClickable()).isFalse();
  }

  @Test
  public void test_notInteractableWhenNotAttachedToBottomSheetAndAccessibilityEnabled() {
    activity.addViewToContainer(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);
    assertImportantForAccessibility(false);
    assertThat(dragHandleView.isClickable()).isFalse();
  }

  @Test
  public void test_interactableWhenAttachedAndAccessibilityEnabled() {
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);
    assertImportantForAccessibility(true);
    assertThat(dragHandleView.isClickable()).isTrue();
  }

  @Test
  public void test_expandCollapsedBottomSheetWhenClicked() {
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);
    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_EXPANDED);
  }

  @Test
  public void test_collapseExpandedBottomSheetWhenClicked() {
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_COLLAPSED);
  }

  @Test
  public void test_collapsedBottomSheetMoveToHalfExpanded_whenClickedAndFitToContentsFalse() {
    activity.bottomSheetBehavior.setFitToContents(false);
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);
    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_HALF_EXPANDED);
  }

  @Test
  public void test_expandedBottomSheetMoveToHalfExpanded_whenClickedAndFitToContentsFalse() {
    activity.bottomSheetBehavior.setFitToContents(false);
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_HALF_EXPANDED);
  }

  @Test
  public void test_halfExpandedBottomSheetMoveToExpanded_whenPreviouslyCollapsed() {
    activity.bottomSheetBehavior.setFitToContents(false);
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_EXPANDED);
  }

  @Test
  public void test_halfExpandedBottomSheetMoveToCollapsed_whenPreviouslyExpanded() {
    activity.bottomSheetBehavior.setFitToContents(false);
    activity.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    activity.addViewToBottomSheet(dragHandleView);
    shadowOf(accessibilityManager).setEnabled(true);

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    dragHandleView.performClick();

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(activity.bottomSheetBehavior.getState())
        .isEqualTo(BottomSheetBehavior.STATE_COLLAPSED);
  }

  private void assertImportantForAccessibility(boolean important) {
    if (important) {
      assertThat(ViewCompat.getImportantForAccessibility(dragHandleView))
          .isEqualTo(ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    } else {
      assertThat(ViewCompat.getImportantForAccessibility(dragHandleView))
          .isEqualTo(ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
  }

  private static class TestActivity extends AppCompatActivity {
    @Nullable
    private CoordinatorLayout container;

    @Nullable
    private FrameLayout bottomSheet;

    @NonNull
    private final BottomSheetBehavior<View> bottomSheetBehavior = new BottomSheetBehavior<>();

    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light_NoActionBar);
      container = new CoordinatorLayout(this);
      setContentView(
          container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      bottomSheet = new FrameLayout(this);
      CoordinatorLayout.LayoutParams layoutParams =
          new CoordinatorLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      layoutParams.setBehavior(bottomSheetBehavior);
      container.addView(bottomSheet, layoutParams);
    }

    void addViewToContainer(View view) {
      if (container == null) {
        return;
      }
      container.addView(view);
    }

    void addViewToBottomSheet(View view) {
      if (bottomSheet == null) {
        return;
      }
      bottomSheet.addView(view);
    }
  }
}
