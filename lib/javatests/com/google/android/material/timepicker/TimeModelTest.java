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

package com.google.android.material.timepicker;

import com.google.android.material.test.R;

import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link TimeModel} */
@RunWith(RobolectricTestRunner.class)
public class TimeModelTest {

  @Test
  public void with12HFormat_returnsCorrectHourContentDescription() {
    TimeModel timeModel = new TimeModel(CLOCK_12H);

    assertThat(timeModel.getHourContentDescriptionResId()).isEqualTo(R.string.material_hour_suffix);
  }

  @Test
  public void with24HFormat_returnsCorrectHourContentDescription() {
    TimeModel timeModel = new TimeModel(CLOCK_24H);

    assertThat(timeModel.getHourContentDescriptionResId())
        .isEqualTo(R.string.material_hour_24h_suffix);
  }

  @Test
  public void formatText_validInput_returnsFormattedText() {
    String formattedText =
        TimeModel.formatText(ApplicationProvider.getApplicationContext().getResources(), "1");

    assertThat(formattedText).isEqualTo("01");
  }

  @Test
  public void formatText_invalidInput_returnsNull() {
    String formattedText =
        TimeModel.formatText(ApplicationProvider.getApplicationContext().getResources(), "+");

    assertThat(formattedText).isNull();
  }
}
