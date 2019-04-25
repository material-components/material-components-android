/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.picker;

import static org.junit.Assert.assertEquals;

import com.google.android.material.picker.selector.ParcelableTestUtils;
import java.util.Calendar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class CalendarBoundsTest {

  private static final Month FEB_2016 = Month.create(2016, Calendar.FEBRUARY);
  private static final Month MARCH_2016 = Month.create(2016, Calendar.MARCH);
  private static final Month APRIL_2016 = Month.create(2016, Calendar.APRIL);

  @Rule public final ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void equalAfterParceling() {
    CalendarBounds originalBounds =
        CalendarBounds.create(/* start= */ FEB_2016, /* end= */ APRIL_2016);
    CalendarBounds constructedBounds =
        ParcelableTestUtils.parcelAndCreate(originalBounds, CalendarBounds.CREATOR);
    assertEquals(originalBounds, constructedBounds);
  }

  @Test
  public void currentDefaultsToTodayIfWithinBounds() {
    Month today = Month.today();
    Month start = today.monthsLater(-1);
    Month end = today.monthsLater(1);
    CalendarBounds calendarBounds = CalendarBounds.create(start, end);
    assertEquals(today, calendarBounds.getCurrent());
  }

  @Test
  public void currentDefaultsToStartIfTodayIsInvalid() {
    CalendarBounds calendarBounds =
        CalendarBounds.create(/* start= */ FEB_2016, /* end= */ APRIL_2016);
    assertEquals(FEB_2016, calendarBounds.getCurrent());
  }

  @Test
  public void illegalCurrentMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    CalendarBounds.create(/* start= */ FEB_2016, /* end= */ MARCH_2016, /* current= */ APRIL_2016);
  }

  @Test
  public void illegalEndMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    CalendarBounds.create(/* start= */ MARCH_2016, /* end= */ FEB_2016, /* current= */ MARCH_2016);
  }
}
