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

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import android.os.Parcel;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

/** Tests for state restoration of {@link Slider} */
@RunWith(RobolectricTestRunner.class)
public class SliderStateTest {

  @Test
  public void testValues() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);

    ActivityController<AppCompatActivity> controller =
        Robolectric.buildActivity(AppCompatActivity.class).setup().start();

    Slider slider = addSlider(controller);
    slider.setValues(1F, 2F);

    Bundle bundle = new Bundle();
    controller.saveInstanceState(bundle);

    slider.setValues(2F, 3F);

    controller.restoreInstanceState(parcelAndUnParcel(bundle));

    assertThat(slider.getValues()).containsExactly(1F, 2F).inOrder();
  }

  private Slider addSlider(ActivityController<AppCompatActivity> controller) {
    Slider slider = new Slider(controller.get());
    slider.setId(View.generateViewId());
    ViewGroup content = controller.get().findViewById(android.R.id.content);
    content.addView(slider);
    return slider;
  }

  private Bundle parcelAndUnParcel(Bundle bundle) {
    Parcel parcel = Parcel.obtain();
    bundle.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    return parcel.readBundle();
  }
}
