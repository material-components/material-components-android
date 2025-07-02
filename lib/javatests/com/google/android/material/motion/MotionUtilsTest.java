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
import static org.junit.Assert.assertThrows;

import android.animation.TimeInterpolator;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import androidx.core.content.res.ResourcesCompat;
import androidx.dynamicanimation.animation.SpringForce;
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
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
@DoNotInstrument
public class MotionUtilsTest {

  private ActivityController<AppCompatActivity> activityController;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testResolvesThemeSpring() {
    createActivityAndSetTheme(R.style.Theme_Material3_DayNight);
    Context context = activityController.get().getApplicationContext();
    float expectedStiffness = ResourcesCompat.getFloat(
        context.getResources(), R.dimen.m3_sys_motion_standard_spring_fast_spatial_stiffness);
    float expectedDampingRatio = ResourcesCompat.getFloat(
        context.getResources(), R.dimen.m3_sys_motion_standard_spring_fast_spatial_damping);
    SpringForce spring = MotionUtils.resolveThemeSpringForce(context,
        R.attr.motionSpringFastSpatial, R.style.Motion_Material3_Spring_Standard_Fast_Spatial);

    assertThat(spring.getStiffness()).isEqualTo(expectedStiffness);
    assertThat(spring.getDampingRatio()).isEqualTo(expectedDampingRatio);
  }

  @Test
  public void testAbsentThemeSpring_shouldResolveDefault() {
    createActivityAndSetTheme(R.style.Theme_AppCompat_DayNight);
    Context context = activityController.get().getApplicationContext();

    SpringForce spring = MotionUtils.resolveThemeSpringForce(context,
        R.attr.motionSpringFastSpatial, R.style.Motion_MyApp_Spring_Custom_Default);

    assertThat(spring.getStiffness()).isEqualTo(1450f);
    assertThat(spring.getDampingRatio()).isEqualTo(0.5f);
  }

  @Test
  public void testPartialSpring_shouldThrow() {
    createActivityAndSetTheme(R.style.Theme_Material3_DayNight_PartialSpring);
    Context context = activityController.get().getApplicationContext();

    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> MotionUtils.resolveThemeSpringForce(context, R.attr.motionSpringFastSpatial,
            R.style.Motion_Material3_Spring_Standard_Fast_Spatial)
    );
    assertThat(thrown).hasMessageThat().contains("must have a damping");
  }

  @Test
  public void testResolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight,
        R.attr.motionEasingStandardInterpolator,
        isAnimationUtilsShadowed() ? LinearInterpolator.class : PathInterpolator.class);
  }

  @Test
  public void testCustomInterpolator_resolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight_CustomInterpolator,
        R.attr.motionEasingStandardInterpolator,
        isAnimationUtilsShadowed() ? LinearInterpolator.class : PathInterpolator.class);
  }

  @Test
  public void testCustomAnimInterpolator_resolvesThemeInterpolator() {
    assertThemeInterpolatorIsInstanceOf(
        R.style.Theme_Material3_DayNight_CustomAnimInterpolator,
        R.attr.motionEasingStandardInterpolator,
        isAnimationUtilsShadowed() ? LinearInterpolator.class : AccelerateInterpolator.class);
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

  private boolean isAnimationUtilsShadowed() {
    return Shadow.extract(new AnimationUtils()) != null;
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
