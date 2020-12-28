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

import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link TimeModel} */
@RunWith(RobolectricTestRunner.class)
public class TimeModelTest {

  @Test
  public void timeModel_with12HFormat_hasCorrectValidators() {
    TimeModel timeModel = new TimeModel(CLOCK_12H);

    assertThat(timeModel.getHourInputValidator().getMax()).isEqualTo(12);
    assertThat(timeModel.getMinuteInputValidator().getMax()).isEqualTo(59);
  }

  @Test
  public void timeModel_with24HFormat_hasCorrectValidators() {
    TimeModel timeModel = new TimeModel(CLOCK_24H);

    assertThat(timeModel.getHourInputValidator().getMax()).isEqualTo(24);
    assertThat(timeModel.getMinuteInputValidator().getMax()).isEqualTo(59);
  }
}
