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

package com.google.android.material.textfield;

import com.google.android.material.test.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAccessibilityManager;

/** A11y tests for the Exposed Dropdown Menu. */
@RunWith(RobolectricTestRunner.class)
public class ExposedDropdownMenuAccessibilityTest {

  private AppCompatActivity activity;
  private ShadowAccessibilityManager accessibilityManager;
  private TextInputLayout dropdownEditable;
  private TextInputLayout dropdownNonEditable;

  @Before
  public void themeApplicationContext() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Theme_Material3_DayNight_NoActionBar);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    accessibilityManager =
        shadowOf((AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE));
  }

  private void inflateLayout() {
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_exposed_dropdown_menu, null);
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(inflated);
    dropdownEditable = inflated.findViewById(R.id.dropdown_editable);
    dropdownNonEditable = inflated.findViewById(R.id.dropdown_noneditable);
  }

  @Test
  public void testEndIconInNonEditableMenu_isImportantForA11y() {
    accessibilityManager.setEnabled(true);

    inflateLayout();

    assertThat(dropdownNonEditable.getEndIconView().isImportantForAccessibility(), is(true));
  }

  @Test
  public void testEndIconInEditableMenu_isImportantForA11y() {
    accessibilityManager.setEnabled(true);

    inflateLayout();

    assertThat(dropdownEditable.getEndIconView().isImportantForAccessibility(), is(true));;
  }

  @Test
  public void testEndIconInNonEditableMenu_inTouchExplorationMode_notImportantForA11y() {
    inflateLayout();

    accessibilityManager.setTouchExplorationEnabled(true);
    boolean importantForA11yInTouchExplMode =
        dropdownNonEditable.getEndIconView().isImportantForAccessibility();
    accessibilityManager.setTouchExplorationEnabled(false);
    accessibilityManager.setEnabled(false);

    // Assert it was false in touch exploration mode.
    assertThat(importantForA11yInTouchExplMode, is(false));
    // Assert it switches back to true.
    assertThat(dropdownNonEditable.getEndIconView().isImportantForAccessibility(), is(true));
  }

  @Test
  public void testEndIconInEditableMenu_inTouchExplorationMode_alwaysImportantForA11y() {
    inflateLayout();

    accessibilityManager.setTouchExplorationEnabled(true);
    boolean importantForA11yInTouchExplMode =
        dropdownEditable.getEndIconView().isImportantForAccessibility();
    accessibilityManager.setTouchExplorationEnabled(false);

    assertThat(importantForA11yInTouchExplMode, is(true));
    assertThat(dropdownEditable.getEndIconView().isImportantForAccessibility(), is(true));
  }
}
