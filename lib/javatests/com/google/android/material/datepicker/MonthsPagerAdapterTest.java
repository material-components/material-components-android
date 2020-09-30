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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MonthsPagerAdapterTest {

  private Context context;
  private Month feb2016;
  private Month march2016;
  private Month april2016;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    context =
        Robolectric.buildActivity(AppCompatActivity.class).setup().get().getApplicationContext();
    feb2016 = Month.create(2016, Calendar.FEBRUARY);
    march2016 = Month.create(2016, Calendar.MARCH);
    april2016 = Month.create(2016, Calendar.APRIL);
  }

  @Test
  public void startingPageCalculated() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            context,
            /* dateSelector= */ null,
            new CalendarConstraints.Builder()
                .setStart(feb2016.timeInMillis)
                .setEnd(april2016.timeInMillis)
                .setOpenAt(march2016.timeInMillis)
                .build(),
            /* onDayClickListener= */ null);
    assertEquals(3, monthsAdapter.getItemCount());
    assertEquals(1, monthsAdapter.getPosition(march2016));
  }

  @Test
  public void singleMonthConstructs() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            context,
            /* dateSelector= */ null,
            new CalendarConstraints.Builder()
                .setStart(feb2016.timeInMillis)
                .setEnd(feb2016.timeInMillis)
                .setOpenAt(feb2016.timeInMillis)
                .build(),
            /* onDayClickListener= */ null);
    assertEquals(1, monthsAdapter.getItemCount());
    assertEquals(0, monthsAdapter.getPosition(feb2016));
  }

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void illegalStartMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new MonthsPagerAdapter(
        context,
        /* dateSelector= */ null,
        new CalendarConstraints.Builder()
            .setStart(feb2016.timeInMillis)
            .setEnd(march2016.timeInMillis)
            .setOpenAt(april2016.timeInMillis)
            .build(),
        /* onDayClickListener= */ null);
  }

  @Test
  public void illegalLastMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new MonthsPagerAdapter(
        context,
        /* dateSelector= */ null,
        new CalendarConstraints.Builder()
            .setStart(march2016.timeInMillis)
            .setEnd(feb2016.timeInMillis)
            .setOpenAt(march2016.timeInMillis)
            .build(),
        /* onDayClickListener= */ null);
  }

  @Test
  public void pageTitles() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            context,
            /* dateSelector= */ null,
            new CalendarConstraints.Builder()
                .setStart(feb2016.timeInMillis)
                .setEnd(april2016.timeInMillis)
                .setOpenAt(march2016.timeInMillis)
                .build(),
            /* onDayClickListener= */ null);
    assertEquals(
        feb2016.getLongName(context), monthsAdapter.getPageTitle(/* position= */ 0).toString());
    assertEquals(
        march2016.getLongName(context), monthsAdapter.getPageTitle(/* position= */ 1).toString());
    assertEquals(
        april2016.getLongName(context), monthsAdapter.getPageTitle(/* position= */ 2).toString());
  }
}
