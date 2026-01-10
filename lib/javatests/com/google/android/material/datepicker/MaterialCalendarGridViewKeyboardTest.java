/*
 * Copyright 2025 The Android Open Source Project
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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Calendar.APRIL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.datepicker.CalendarConstraints.DateValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@SuppressLint("VisibleForTests")
@RunWith(RobolectricTestRunner.class)
public final class MaterialCalendarGridViewKeyboardTest {

  private MaterialCalendarGridView gridView;
  private MonthAdapter adapter;
  private Context context;
  private Month month;
  private SpyOnMonthNavigationListener monthNavigationListener;

  @Before
  public void setup() {
    Locale.setDefault(Locale.US);
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    context = ApplicationProvider.getApplicationContext();
    month = Month.create(2024, APRIL);
    monthNavigationListener = new SpyOnMonthNavigationListener();
  }

  @Test
  public void onKeyDown_dPadLeft_movesSelectionBackwards() {
    setupGridView();
    int day = 5;
    int position = adapter.dayToPosition(day);
    gridView.setSelection(position);

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
    ShadowLooper.idleMainLooper();

    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(day - 1));
  }

  @Test
  public void onKeyDown_dPadRight_movesSelectionForward() {
    setupGridView();
    int day = 5;
    int position = adapter.dayToPosition(day);
    gridView.setSelection(position);

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_RIGHT,
        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
    ShadowLooper.idleMainLooper();

    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(day + 1));
  }

  @Test
  public void onKeyDown_tabForwardFromLastDay_returnsFalse() {
    setupGridView();
    int lastDay = 30;
    int position = adapter.dayToPosition(lastDay);
    gridView.setSelection(position);

    boolean result =
        gridView.onKeyDown(
            KeyEvent.KEYCODE_TAB, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB));
    ShadowLooper.idleMainLooper();

    assertThat(result).isFalse();
  }

  @Test
  public void onKeyDown_tabBackwardFromFirstDay_returnsFalse() {
    setupGridView();
    int firstDay = 1;
    int position = adapter.dayToPosition(firstDay);
    gridView.setSelection(position);

    boolean result =
        gridView.onKeyDown(
            KeyEvent.KEYCODE_TAB,
            new KeyEvent(
                /* downTime= */ 0L,
                /* eventTime= */ 0L,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_TAB,
                /* repeat= */ 0,
                KeyEvent.META_SHIFT_ON));
    ShadowLooper.idleMainLooper();

    assertThat(result).isFalse();
  }

  @Test
  public void dpadRight_skipsDisabledDay_andFocusesNextValidDay() {
    setupGridView(DayValidator.hide(month, 16));
    int startDay = 15;
    gridView.setSelection(adapter.dayToPosition(startDay));

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_RIGHT,
        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
    ShadowLooper.idleMainLooper();

    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(17));
  }

  @Test
  public void dpadLeft_skipsDisabledDay_andFocusesPreviousValidDay() {
    setupGridView(DayValidator.hide(month, 14));
    int startDay = 15;
    gridView.setSelection(adapter.dayToPosition(startDay));

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
    ShadowLooper.idleMainLooper();

    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(13));
  }

  @Test
  public void dpadRight_onLastDayOfMonth_navigatesToNextMonth() {
    setupGridView();
    int lastDay = 30;
    gridView.setSelection(adapter.dayToPosition(lastDay));

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_RIGHT,
        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
    ShadowLooper.idleMainLooper();

    assertThat(monthNavigationListener.navigatedNext).isTrue();
  }

  @Test
  public void dpadLeft_onFirstDayOfMonth_navigatesToPreviousMonth() {
    setupGridView();
    int firstDay = 1;
    gridView.setSelection(adapter.dayToPosition(firstDay));

    gridView.onKeyDown(
        KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
    ShadowLooper.idleMainLooper();

    assertThat(monthNavigationListener.navigatedPrevious).isTrue();
  }

  @Test
  public void handleVerticalNavigation_dPadDownOnDisabledRow_findsValidDayInNextEnabledRow() {
    // Days 2, 9, 16, 23, 30 of April 2024 are in the same column for US locale.
    int dayInRow1 = 9;
    int dayInRow2 = dayInRow1 + 7; // Day 16
    int dayInRow3 = dayInRow2 + 7; // Day 23
    setupGridView(DayValidator.hide(month, 14, 15, 16, 17, 18, 19, 20)); // Hide days in row 2

    // If user presses DOWN from day 9, GridView would select 16, which is in a disabled row.
    gridView.handleVerticalNavigationOnDisabledDay(
        KeyEvent.KEYCODE_DPAD_DOWN, adapter.dayToPosition(dayInRow2));
    ShadowLooper.idleMainLooper();

    // Handler should skip row 2 and find day 23 in row 3.
    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(dayInRow3));
  }

  @Test
  public void handleVerticalNavigation_dPadUpOnDisabledRow_findsValidDayInPreviousEnabledRow() {
    // Days 2, 9, 16, 23, 30 of April 2024 are in the same column for US locale.
    int dayInRow2 = 16;
    int dayInRow1 = dayInRow2 - 7; // Day 9
    int dayInRow0 = dayInRow1 - 7; // Day 2
    setupGridView(DayValidator.hide(month, 7, 8, 9, 10, 11, 12, 13)); // Hide days in row 1

    // If user presses UP from day 16, GridView would select 9, which is in a disabled row.
    gridView.handleVerticalNavigationOnDisabledDay(
        KeyEvent.KEYCODE_DPAD_UP, adapter.dayToPosition(dayInRow1));
    ShadowLooper.idleMainLooper();

    // Handler should skip row 1 and find day 2 in row 0.
    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(dayInRow0));
  }

  @Test
  public void handleVerticalNavigation_dPadDownWhenAllDaysDisabled_returnsFalse() {
    // Hide all 30 days
    Integer[] daysToHide = new Integer[30];
    for (int i = 0; i < 30; i++) {
      daysToHide[i] = i + 1;
    }
    setupGridView(DayValidator.hide(month, daysToHide));

    boolean result =
        gridView.handleVerticalNavigationOnDisabledDay(
            KeyEvent.KEYCODE_DPAD_DOWN, adapter.dayToPosition(16));

    assertThat(result).isFalse();
  }

  @Test
  public void handleVerticalNavigation_dPadUpWhenAllDaysDisabled_returnsFalse() {
    // Hide all 30 days
    Integer[] daysToHide = new Integer[30];
    for (int i = 0; i < 30; i++) {
      daysToHide[i] = i + 1;
    }
    setupGridView(DayValidator.hide(month, daysToHide));

    boolean result =
        gridView.handleVerticalNavigationOnDisabledDay(
            KeyEvent.KEYCODE_DPAD_UP, adapter.dayToPosition(9));

    assertThat(result).isFalse();
  }

  @Test
  public void handleVerticalNavigation_onDisabledDay_findsValidDayInSameRow() {
    setupGridView(DayValidator.hide(month, 16));

    // If user navigates to day 16 (e.g., from day 9), handleVerticalNavigationOnDisabledDay
    // should be triggered with position 16.
    gridView.handleVerticalNavigationOnDisabledDay(
        KeyEvent.KEYCODE_DPAD_DOWN, adapter.dayToPosition(16));
    ShadowLooper.idleMainLooper();

    // The handler should try to find the nearest valid day in the same row as 16.
    assertThat(gridView.getSelectedItemPosition()).isEqualTo(adapter.dayToPosition(17));
  }

  private void setupGridView() {
    setupGridView(null);
  }

  private void setupGridView(@Nullable DateValidator validator) {
    gridView = new MaterialCalendarGridView(context);
    SingleDateSelector dateSelector = new SingleDateSelector();
    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
    if (validator != null) {
      constraintsBuilder.setValidator(validator);
    }
    CalendarConstraints constraints = constraintsBuilder.build();
    adapter = new MonthAdapter(month, dateSelector, constraints, null);
    gridView.setAdapter(adapter);
    gridView.setNumColumns(7);
    gridView.setOnMonthNavigationListener(monthNavigationListener);
    gridView.measure(
        MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY));
    gridView.layout(0, 0, 1000, 1000);
    ShadowLooper.idleMainLooper();
  }

  /** A {@link DateValidator} that allows disabling specific days. */
  static final class DayValidator implements DateValidator {

    private final List<Long> disabledDays = new ArrayList<>();

    private DayValidator(List<Long> days) {
      disabledDays.addAll(days);
    }

    /** Returns a {@link DateValidator} which disables dates from {@code days}. */
    public static DayValidator hide(Month month, Integer... days) {
      List<Long> dayMillis = new ArrayList<>();
      for (Integer day : days) {
        dayMillis.add(month.getDay(day));
      }
      return new DayValidator(dayMillis);
    }

    @Override
    public boolean isValid(long date) {
      return !disabledDays.contains(date);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeList(disabledDays);
    }

    public static final Parcelable.Creator<DayValidator> CREATOR =
        new Parcelable.Creator<DayValidator>() {
          @Override
          public DayValidator createFromParcel(Parcel source) {
            ArrayList<Long> days = new ArrayList<>();
            source.readList(days, Long.class.getClassLoader());
            return new DayValidator(days);
          }

          @Override
          public DayValidator[] newArray(int size) {
            return new DayValidator[size];
          }
        };
  }

  private static class SpyOnMonthNavigationListener
      implements MaterialCalendar.OnMonthNavigationListener {
    boolean navigatedNext = false;
    boolean navigatedPrevious = false;

    @Override
    public boolean onMonthNavigationNext() {
      navigatedNext = true;
      return true;
    }

    @Override
    public boolean onMonthNavigationPrevious() {
      navigatedPrevious = true;
      return true;
    }
  }
}
