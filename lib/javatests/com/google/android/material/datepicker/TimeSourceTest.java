/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.datepicker;

import static com.google.common.truth.Truth.assertThat;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class TimeSourceTest {

  @Test
  public void system_usesSystemTimeZone() throws Exception {
    Calendar expected = Calendar.getInstance();
    Calendar instance = TimeSource.system().now();

    assertThat(instance.getTimeZone()).isEqualTo(expected.getTimeZone());
    // Skip checking the actual date and time fields to ensure that the test is deterministic.
    // Rationale: Though unlikely, it is possible for the rest of the calender fields to have
    // changed between getting the two calendar instances.
  }

  @Test
  public void fixed_usesSystemTimeZoneByDefault() throws Exception {
    Calendar expected = Calendar.getInstance();
    Calendar instance = TimeSource.fixed(expected.getTimeInMillis()).now();

    assertThat(instance).isEqualTo(expected);
  }

  @Test
  public void now_usesConfiguredTimeZone() throws Exception {
    TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    Calendar expected = Calendar.getInstance(utcTimeZone);
    Calendar instance = TimeSource.fixed(expected.getTimeInMillis(), utcTimeZone).now();

    assertThat(instance).isEqualTo(expected);
  }

  @Test
  public void now_usesGivenTimeZone() throws Exception {
    TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("US/Pacific"));
    Calendar instance = TimeSource.fixed(expected.getTimeInMillis()).now(utcTimeZone);

    assertThat(instance.getTimeZone()).isEqualTo(utcTimeZone);
  }
}
