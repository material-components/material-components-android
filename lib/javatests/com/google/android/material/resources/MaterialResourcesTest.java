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

package com.google.android.material.resources;

import com.google.android.material.test.R;

import static android.content.Context.WINDOW_SERVICE;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MaterialResourcesTest {

  private ActivityController<AppCompatActivity> activityController;

  @Before
  public void createActivity() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Theme_Material3_DayNight_NoActionBar);
    activityController = Robolectric.buildActivity(AppCompatActivity.class).create();
  }

  @Test
  public void testGetUnscaledDimensionPixelSize_returnsSameValueWhenFontScalingIsOff() {
    AppCompatActivity activity = activityController.get();
    int spResult =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_UsesSP, 0);
    int dpResult =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_UsesDp, 0);
    assertThat(spResult).isEqualTo(dpResult);
  }

  @Test
  public void testUnscaledDimensionPixelSize_differsFromDimensionPixelSize() {
    float fontScale = 1.7F;
    setFontScale(activityController, fontScale);
    AppCompatActivity activity = activityController.get();
    int unscaledSp =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_UsesSP, 0);
    int scaledSp = activity.getResources().getDimensionPixelSize(
        R.dimen.material_text_size_sp);
    assertThat(unscaledSp).isNotEqualTo(scaledSp);
    assertThat(scaledSp).isEqualTo(Math.round(unscaledSp * fontScale));
  }

  @Test
  @Config(qualifiers = "xxxhdpi")
  public void
      testGetUnscaledDimensionPixelSize_returnsSameValueWhenFontScalingIsOnWithHighDensity() {
    setFontScale(activityController, 1.3f);
    AppCompatActivity activity = activityController.get();
    int spResult =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_UsesSP, 0);
    int dpResult =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_UsesDp, 0);
    assertThat(spResult).isEqualTo(dpResult);
  }

  @Test
  public void testUnscaledDimensionPixelSize_noTextSizeAvailable() {
    AppCompatActivity activity = activityController.get();
    int noSizeResult =
        MaterialResources.getUnscaledTextSize(
            activity, R.style.TextAppearance_Test_NoTextSize, 18);
    assertThat(noSizeResult).isEqualTo(18);
  }

  @Test
  public void testUnscaledDimensionPixelSize_noTextAppearanceAvailable() {
    AppCompatActivity activity = activityController.get();
    int noTextAppearanceResult =
        MaterialResources.getUnscaledTextSize(
            activity, 0, 22);
    assertThat(noTextAppearanceResult).isEqualTo(22);
  }

  private static void setFontScale(
      ActivityController<AppCompatActivity> activityController, float scale) {
    AppCompatActivity activity = activityController.get();
    Configuration configuration = activity.getResources().getConfiguration();
    DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
    WindowManager wm = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(metrics);
    configuration.fontScale = scale;
    metrics.scaledDensity = configuration.fontScale * metrics.density;
    activity.getResources().updateConfiguration(configuration, metrics);
    activityController.configurationChange(configuration);
  }
}
