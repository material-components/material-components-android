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

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;

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

  private AppCompatActivity activity;
  private Month feb2016;
  private Month march2016;
  private Month april2016;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    feb2016 = Month.create(2016, Calendar.FEBRUARY);
    march2016 = Month.create(2016, Calendar.MARCH);
    april2016 = Month.create(2016, Calendar.APRIL);
  }

  @Test
  public void startingPageCalculated() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            activity.getSupportFragmentManager(),
            activity.getLifecycle(),
            /* gridSelector= */ null,
            /* firstPage= */ feb2016,
            /* lastPage= */ april2016,
            /* startPage= */ march2016,
            /* onDayClickListener= */ null);
    assertEquals(3, monthsAdapter.getItemCount());
    assertEquals(1, monthsAdapter.getStartPosition());
  }

  @Test
  public void singleMonthConstructs() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            activity.getSupportFragmentManager(),
            activity.getLifecycle(),
            /* gridSelector= */ null,
            /* firstPage= */ feb2016,
            /* lastPage= */ feb2016,
            /* startPage= */ feb2016,
            /* onDayClickListener= */ null);
    assertEquals(1, monthsAdapter.getItemCount());
    assertEquals(0, monthsAdapter.getStartPosition());
  }

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void illegalStartMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new MonthsPagerAdapter(
        activity.getSupportFragmentManager(),
        activity.getLifecycle(),
        /* gridSelector= */ null,
        /* firstPage= */ feb2016,
        /* lastPage= */ march2016,
        /* startPage= */ april2016,
        /* onDayClickListener= */ null);
  }

  @Test
  public void illegalLastMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new MonthsPagerAdapter(
        activity.getSupportFragmentManager(),
        activity.getLifecycle(),
        /* gridSelector= */ null,
        /* firstPage= */ march2016,
        /* lastPage= */ feb2016,
        /* startPage= */ march2016,
        /* onDayClickListener= */ null);
  }

  @Test
  public void pageTitles() {
    MonthsPagerAdapter monthsAdapter =
        new MonthsPagerAdapter(
            activity.getSupportFragmentManager(),
            activity.getLifecycle(),
            /* gridSelector= */ null,
            /* firstPage= */ feb2016,
            /* lastPage= */ april2016,
            /* startPage= */ march2016,
            /* onDayClickListener= */ null);
    assertEquals(feb2016.getLongName(), monthsAdapter.getPageTitle(/* position= */ 0).toString());
    assertEquals(march2016.getLongName(), monthsAdapter.getPageTitle(/* position= */ 1).toString());
    assertEquals(april2016.getLongName(), monthsAdapter.getPageTitle(/* position= */ 2).toString());
  }
}
