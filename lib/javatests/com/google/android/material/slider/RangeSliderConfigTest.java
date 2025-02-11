/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.material.slider;

import com.google.android.material.test.R;

import static android.os.Looper.getMainLooper;
import static com.google.android.material.slider.BaseSlider.UNIT_PX;
import static com.google.android.material.slider.BaseSlider.UNIT_VALUE;
import static org.robolectric.Shadows.shadowOf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.slider.BaseSlider.SeparationUnit;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.Robolectric;

/** Tests for {@link com.google.android.material.slider.RangeSlider}. */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class RangeSliderConfigTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Parameter public float valueFrom;

  @Parameter(1)
  public float valueTo;

  @Parameter(2)
  public float stepValue;

  @Parameter(3)
  public float minSeparation;

  @Parameter(4)
  @SeparationUnit
  public int unit;

  @Parameter(5)
  public Class<? extends Throwable> exception;

  private RangeSlider rangeSlider;
  private AppCompatActivity activity;

  @Parameters(
      name =
          "valueFrom={0},valueTo={1},stepValue={2},minSeparation={3},unit={4},expectedException={5}")
  public static Iterable<Object[]> data() {
    return ImmutableList.of(
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 0f,
          /* minSeparation = */ 0f,
          /* unit = */ UNIT_PX,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 0f,
          /* minSeparation = */ 0f,
          /* unit = */ UNIT_VALUE,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 0f,
          /* minSeparation = */ 2f,
          /* unit = */ UNIT_PX,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 0f,
          /* minSeparation = */ 2f,
          /* unit = */ UNIT_VALUE,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 0f,
          /* unit = */ UNIT_PX,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 0f,
          /* unit = */ UNIT_VALUE,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 2f,
          /* unit = */ UNIT_PX,
          IllegalStateException.class,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 2f,
          /* unit = */ UNIT_VALUE,
          null,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 1f,
          /* unit = */ UNIT_VALUE,
          IllegalStateException.class,
        },
        new Object[] {
          /* valueFrom = */ 0f,
          /* valueTo = */ 10f,
          /* stepValue = */ 2f,
          /* minSeparation = */ 3f,
          /* unit = */ UNIT_VALUE,
          IllegalStateException.class,
        });
  }

  @Before
  public void createRangeSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    rangeSlider = new RangeSlider(activity);
  }

  @Test
  public void rangeSliderWithValues() {
    if (exception != null) {
      thrown.expect(exception);
    }

    rangeSlider.setValueTo(valueTo);
    rangeSlider.setValueFrom(valueFrom);
    rangeSlider.setStepSize(stepValue);
    if (unit == UNIT_PX) {
      rangeSlider.setMinSeparation(minSeparation);
    } else if (unit == UNIT_VALUE) {
      rangeSlider.setMinSeparationValue(minSeparation);
    }

    // Force the slider to do the validation.
    activity.setContentView(rangeSlider);
    shadowOf(getMainLooper()).idle();
  }
}
