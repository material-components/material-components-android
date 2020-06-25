/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;

/** Unit tests for checking drawable type used in {@link ProgressIndicator} in different modes. */
@LooperMode(PAUSED)
@RunWith(ParameterizedRobolectricTestRunner.class)
public class ProgressIndicatorConfigTest {

  @Parameter public int indicatorType;

  @Parameter(1)
  public boolean indeterminate;

  @Parameter(2)
  public Class<? extends Drawable> drawableType;

  private ProgressIndicator progressIndicator;

  @Parameters(name = "indicatorType={0},isIndeterminate={1},drawableType={2}")
  public static ImmutableList<Object[]> data() {
    return ImmutableList.of(
        new Object[] {ProgressIndicator.LINEAR, true, IndeterminateDrawable.class},
        new Object[] {ProgressIndicator.LINEAR, false, DeterminateDrawable.class},
        new Object[] {ProgressIndicator.CIRCULAR, true, IndeterminateDrawable.class},
        new Object[] {ProgressIndicator.CIRCULAR, false, DeterminateDrawable.class});
  }

  @Before
  public void createProgressIndicator() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    progressIndicator = new ProgressIndicator(activity);
    progressIndicator.setIndicatorType(indicatorType);
    progressIndicator.setIndeterminate(indeterminate);
    activity.setContentView(progressIndicator);
  }

  @Test
  public void testCurrentDrawableType() {
    Truth.assertThat(progressIndicator.getCurrentDrawable()).isInstanceOf(drawableType);
  }
}
