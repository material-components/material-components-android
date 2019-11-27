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

import com.google.android.material.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
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

/**
 * Tests for {@link com.google.android.material.slider.Slider}.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class SliderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Parameter
  public float valueFrom;

  @Parameter(1)
  public float valueTo;

  @Parameter(2)
  public float stepValue;

  @Parameter(3)
  public Class<? extends Throwable> exception;

  private Slider slider;

  @Parameters(name = "valueFrom={0},valueTo={1},stepValue={2},expectedException={3}")
  public static Iterable<Object[]> data() {
    return ImmutableList.of(
        new Object[]{
            /* valueFrom = */0f,
            /* valueTo = */1f,
            /* stepValue = */.2f,
            null,
        },
        new Object[]{
            /* valueFrom = */1.2f,
            /* valueTo = */2.5f,
            /* stepValue = */ 0.01f,
            null,
        },
        new Object[]{
            /* valueFrom = */1.235f,
            /* valueTo = */2.555f,
            /* stepValue = */ 0.04f,
            null,
        },
        new Object[]{
            /* valueFrom = */1.235f,
            /* valueTo = */2.555f,
            /* stepValue = */ 0.039f,
            IllegalArgumentException.class,
        },
        new Object[]{
            /* valueFrom = */10f,
            /* valueTo = */3f,
            /* stepValue = */ 2f,
            IllegalArgumentException.class,
        },
        new Object[]{
            /* valueFrom = */0f,
            /* valueTo = */1f,
            /* stepValue = */ 0,
            null,
        },
        new Object[]{
            /* valueFrom = */10f,
            /* valueTo = */3f,
            /* stepValue = */ 2f,
            IllegalArgumentException.class,
        },
        new Object[]{
            /* valueFrom = */0f,
            /* valueTo = */5f,
            /* stepValue = */ 10f,
            IllegalArgumentException.class,
        },
        new Object[]{
            /* valueFrom = */0f,
            /* valueTo = */10f,
            /* stepValue = */ 10f,
            null,
        }
    );
  }

  @Before
  public void createSlider() {
    ApplicationProvider
        .getApplicationContext()
        .setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    slider = new Slider(activity);
  }

  @Test
  public void sliderWithValues() {
    if (exception != null) {
      thrown.expect(exception);
    }

    slider.setValueTo(valueTo);
    slider.setValueFrom(valueFrom);
    slider.setStepSize(stepValue);
  }
}
