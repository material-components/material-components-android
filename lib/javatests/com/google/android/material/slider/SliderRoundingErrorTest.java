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
}
