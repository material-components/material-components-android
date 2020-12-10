/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.snackbar;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAccessibilityManager;

@LooperMode(LooperMode.Mode.LEGACY)
/** Tests for {@link com.google.android.material.snackbar.Snackbar}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class SnackbarTest {

  private Snackbar snackbar;
  private Context activity;
  private ShadowAccessibilityManager accessibilityManager;

  @Before
  public void createActivityAndShadow() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);
    activity = Robolectric.buildActivity(AppCompatActivity.class).create().get();
    accessibilityManager = Shadow.
        extract(activity.getSystemService(Context.ACCESSIBILITY_SERVICE));
  }

  @Test
  public void testGetDuration_whenTouchExplorationEnabled_isIndefinite() {
    accessibilityManager.setTouchExplorationEnabled(true);

    CoordinatorLayout view = new CoordinatorLayout(activity);
    snackbar = Snackbar.make(view, "Test text", Snackbar.LENGTH_LONG).setAction("STUFF!",
        new OnClickListener() {
          @Override
          public void onClick(View v) {}
        });

    assertThat(snackbar.getDuration()).isEqualTo(Snackbar.LENGTH_INDEFINITE);
  }

  @Test
  public void testGetDuration_whenTouchExplorationDisabled_isProvidedValue() {
    accessibilityManager.setTouchExplorationEnabled(false);

    CoordinatorLayout view = new CoordinatorLayout(activity);
    snackbar = Snackbar.make(view, "Test text", 300).setAction("STUFF!",
        new OnClickListener() {
          @Override
          public void onClick(View v) {}
        });

    assertThat(snackbar.getDuration()).isEqualTo(300);
  }
}
