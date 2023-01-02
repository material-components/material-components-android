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

package com.google.android.material.motion;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.animation.TimeInterpolator;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import androidx.annotation.RequiresApi;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
@RequiresApi(api = VERSION_CODES.LOLLIPOP)
public class MotionUtilsTest {

  private ActivityController<AppCompatActivity> activityController;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testResolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight,
        R.attr.motionEasingStandardInterpolator,
        LinearInterpolator.class);
  }

  @Test
  public void testCustomInterpolator_resolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight_CustomInterpolator,
        R.attr.motionEasingStandardInterpolator,
        LinearInterpolator.class);
  }

  @Test
  public void testCustomAnimInterpolator_resolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight_CustomAnimInterpolator,
        R.attr.motionEasingStandardInterpolator,
        LinearInterpolator.class);
  }

  @Test
  public void testResolvesLegacyInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight, R.attr.motionEasingStandard, PathInterpolator.class);
  }

  @Test
  public void testMaterialComponentsTheme_resolveUnavailableInterpolatorReturnsDefault() {
    createActivityAndSetTheme(R.style.Theme_MaterialComponents_DayNight);

    DefaultDummyInterpolator defaultInterpolator = new DefaultDummyInterpolator();
    TimeInterpolator standardInterpolator =
        MotionUtils.resolveThemeInterpolator(
            activityController.get().getApplicationContext(),
            R.attr.motionEasingStandardInterpolator,
            defaultInterpolator);
    assertThat(standardInterpolator).isEqualTo(defaultInterpolator);
  }

  @Test
  public void testMaterialComponentsTheme_resolvesLegacyInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_MaterialComponents_DayNight,
        R.attr.motionEasingStandard,
        PathInterpolator.class);
  }

  @Test
  public void testMaterialComponentsThemeIncorrectLegacyAttrType_shouldThrowException() {
    createActivityAndSetTheme(
        R.style.Theme_MaterialComponents_DayNight_IncorrectLegacyEasingAttrType);

    // ThrowingRunnable used by assertThrows is not available until gradle 4.13
    thrown.expect(IllegalArgumentException.class);
    MotionUtils.resolveThemeInterpolator(
        activityController.get().getApplicationContext(),
        R.attr.motionEasingStandard,
        new DefaultDummyInterpolator());
  }

  @Test
  public void testMaterialComponentsThemeIncorrectLegacyFormatting_shouldThrowException() {
    createActivityAndSetTheme(
        R.style.Theme_MaterialComponents_DayNight_IncorrectLegacyEasingFormat);

    // ThrowingRunnable used by assertThrows is not available until gradle 4.13
    thrown.expect(IllegalArgumentException.class);
    MotionUtils.resolveThemeInterpolator(
        activityController.get().getApplicationContext(),
        R.attr.motionEasingStandard,
        new DefaultDummyInterpolator());
  }

  private void createActivityAndSetTheme(int themeId) {
    ApplicationProvider.getApplicationContext().setTheme(themeId);
    activityController = Robolectric.buildActivity(AppCompatActivity.class).create();
  }

  private void assertThemeInterpolatorIsInstanceOf(int theme, int attrId, Class<?> expectedClass) {
    createActivityAndSetTheme(theme);
    DefaultDummyInterpolator defaultInterpolator = new DefaultDummyInterpolator();
    TimeInterpolator themeInterpolator =
        MotionUtils.resolveThemeInterpolator(
            activityController.get().getApplicationContext(), attrId, defaultInterpolator);
    assertThat(themeInterpolator).isNotEqualTo(defaultInterpolator);
    // Robolectric's shadow of Android's AnimationUtils always returns a LinearInterpolator.
    assertThat(themeInterpolator).isInstanceOf(expectedClass);
  }

  private static class DefaultDummyInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
      return 0;
    }
  }
}
