/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.bottomappbar;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class BottomAppBarBehaviorTest {

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  BottomAppBar bar;

  @Before
  public void createBottomAppBar() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          AppCompatActivity activity = activityTestRule.getActivity();
          CoordinatorLayout container = new CoordinatorLayout(activity);
          bar = new BottomAppBar(activity);
          View otherView = new View(activity);
          otherView.setFocusable(true);
          container.addView(otherView);
          container.addView(bar);
          activity.setContentView(container);
        });
  }

  @Test
  public void testMovedDown_elevationIncreasedWhileHidden() throws Throwable {
    bar.setElevation(10);
    bar.performHide();
    float originalYTranslation = bar.getTranslationY();

    bar.setElevation(30);

    assertTrue(
        "The bar should have bee moved further down to hide the larger shadow.",
        bar.getTranslationY() > originalYTranslation);
  }

  @Test
  public void testHidden_importantForAccessibilityNoHideDescendants() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.performHide());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getImportantForAccessibility())
        .isEqualTo(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
  }

  @Test
  public void testShown_restoresImportantForAccessibility() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> bar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(
        () -> {
          bar.performHide();
          bar.performShow();
        });
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getImportantForAccessibility()).isEqualTo(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Test
  public void testHidden_clearsFocus() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          bar.setFocusable(true);
          bar.requestFocus();
        });
    assertTrue("Bar should have focus", bar.hasFocus());

    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertFalse("Bar should not have focus after hide", bar.hasFocus());
  }

  @Test
  public void testHidden_isNotFocusable() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(
        () -> {
          boolean gained = bar.requestFocus();
          assertFalse("Bar should not be able to gain focus after hide", gained);
          assertThat(bar.getVisibility()).isEqualTo(View.INVISIBLE);
        });
  }

  @Test
  public void testShown_isVisible() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    activityTestRule.runOnUiThread(() -> bar.performShow(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void testShown_doesNotRestoreVisibilityIfInitiallyGone() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.setVisibility(View.GONE));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(() -> bar.performShow(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testHidden_doesNotChangeToInvisibleIfAlreadyGone() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.setVisibility(View.GONE));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testShown_doesNotRestoreVisibilityIfSetToGoneWhileHidden() throws Throwable {
    activityTestRule.runOnUiThread(() -> bar.performHide(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(() -> bar.setVisibility(View.GONE));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    activityTestRule.runOnUiThread(() -> bar.performShow(/* animate= */ false));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertThat(bar.getVisibility()).isEqualTo(View.GONE);
  }
}
