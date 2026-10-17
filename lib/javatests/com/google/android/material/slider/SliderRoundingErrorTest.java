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

package com.google.android.material.slider;

import com.google.android.material.test.R;

import static android.os.Looper.getMainLooper;
import static com.google.android.material.slider.SliderHelper.touchSliderAtValue;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for floating point rounding error handling of {@link Slider} */
@RunWith(RobolectricTestRunner.class)
public class SliderRoundingErrorTest {

  private Slider slider;

  @Before
  public void createSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();

    // Creates slider and adds the listener.
    SliderHelper helper = new SliderHelper(activity);
    slider = helper.getSlider();
    slider.setValueTo(10000f);
    slider.setStepSize(100f);

    helper.addContentView(activity);

    slider.requestFocus();
  }

  @Test
  public void testKnownValues_snapTouchToValue_NoRoundingError() {
    touchSliderAtValue(slider, 0f, MotionEvent.ACTION_DOWN);
    shadowOf(getMainLooper()).idle();
    for (float i = 100f; i < 10000f; i += 100) {
      touchSliderAtValue(slider, i, MotionEvent.ACTION_MOVE);
      shadowOf(getMainLooper()).idle();
      assertThat(slider.getValue()).isEqualTo(i);
    }
  }

  @Test
  public void testFractionalStepSize_snapTouchToValue_valueLandsExactlyOnTick() {
    slider.setValueFrom(0.2f);
    slider.setValueTo(4f);
    slider.setStepSize(0.05f);
    slider.setValues(1f);

    touchSliderAtValue(slider, 1f, MotionEvent.ACTION_DOWN);
    shadowOf(getMainLooper()).idle();

    // These came back as 1.4499999, 1.6999999, ... because the snapped position was scaled back
    // up by the value range instead of being counted from valueFrom in whole steps.
    float[] ticks = {1.45f, 1.7f, 1.95f, 2.4f, 2.65f, 2.9f, 3.15f, 3.4f, 3.65f, 3.9f};
    for (float tick : ticks) {
      touchSliderAtValue(slider, tick, MotionEvent.ACTION_MOVE);
      shadowOf(getMainLooper()).idle();
      assertThat(slider.getValue()).isEqualTo(tick);
    }
  }

  @Test
  public void testFractionalRange_snapTouchToValue_stepCountIsNotTruncated() {
    slider.setValueFrom(-1.5f);
    slider.setValueTo(3.7f);
    slider.setStepSize(0.1f);
    slider.setValues(-1.5f);

    touchSliderAtValue(slider, -1.5f, MotionEvent.ACTION_DOWN);
    shadowOf(getMainLooper()).idle();

    // This range is exactly 52 steps, but (3.7 - -1.5) / 0.1 comes out as 51.999996 in float
    // arithmetic, so the step count used to be truncated to 51 and every value landed off the
    // 0.1 grid. Touching -1 used to give -0.9901961 and valueTo was never reachable.
    float[] ticks = {-1f, -0.5f, 0f, 0.5f, 1f, 2.5f, 3.7f};
    for (float tick : ticks) {
      touchSliderAtValue(slider, tick, MotionEvent.ACTION_MOVE);
      shadowOf(getMainLooper()).idle();
      assertThat(slider.getValue()).isEqualTo(tick);
    }
  }
}
