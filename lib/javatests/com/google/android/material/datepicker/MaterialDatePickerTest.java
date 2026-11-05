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

import com.google.android.material.test.R;

import static com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Calendar.APRIL;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SUNDAY;
import static org.junit.Assert.assertEquals;

import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.annotation.StringRes;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.test.core.app.ApplicationProvider;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/** Test for {@link com.google.android.material.datepicker.MaterialDatePicker}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialDatePickerTest {

  private static final long ONE_DAY_MILLIS = Duration.ofDays(1).toMillis();

  // Tuesday, December 31, 2019 11:59:00 PM GMT-05:00
  private static final long NEW_YORK_TIME_2019_12_31_11_59_00_PM = 1577854740000L;
  // Tuesday, January 1, 2020 09:00:00 AM UTC+13:00
  private static final long NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM = 1577822400000L;
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

  private static final long FEB_2016 = Month.create(2016, FEBRUARY).timeInMillis;
  private static final long MARCH_2016 = Month.create(2016, MARCH).timeInMillis;
  private static final long APRIL_2016 = Month.create(2016, APRIL).timeInMillis;

  private static Calendar setTestLocalTime(long testTimeAsEpochMs, TimeZone timeZone) {
    TimeSource fixedTimeSource = TimeSource.fixed(testTimeAsEpochMs, timeZone);
    UtcDates.setTimeSource(fixedTimeSource);
    Calendar testTimeInUtc = fixedTimeSource.now();
    testTimeInUtc.set(Calendar.HOUR_OF_DAY, 0);
    testTimeInUtc.set(Calendar.MINUTE, 0);
    testTimeInUtc.set(Calendar.SECOND, 0);
    testTimeInUtc.set(Calendar.MILLISECOND, 0);
    testTimeInUtc.setTimeZone(UTC_TIME_ZONE);

    return testTimeInUtc;
  }

  private static void testTodayInUtcMillisecondsForLocalTime(
      long testTimeAsEpochMs, TimeZone timeZone) {
    Calendar firstMomentOfTodayInUtc = setTestLocalTime(testTimeAsEpochMs, timeZone);

    long todayUtcMS = MaterialDatePicker.todayInUtcMilliseconds();
    Calendar outputUtcTodayInCalendar = Calendar.getInstance(UTC_TIME_ZONE);
    outputUtcTodayInCalendar.setTimeInMillis(todayUtcMS);

    // Assert fields finer than a day are stripped.
    assertThat(todayUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert date returned by datepicker is same as first moment of today from UTC calender.
    assertThat(outputUtcTodayInCalendar).isEqualTo(firstMomentOfTodayInUtc);
  }

  private static void testThisMonthInUtcMillisecondsForLocalTime(
      long testTimeAsEpochMs, TimeZone timeZone) {
    Calendar firstMomentOfTodayInUtc = setTestLocalTime(testTimeAsEpochMs, timeZone);

    long thisMonthUtcMS = MaterialDatePicker.thisMonthInUtcMilliseconds();
    Calendar outputUtcThisMonthInCalendar = Calendar.getInstance(UTC_TIME_ZONE);
    outputUtcThisMonthInCalendar.setTimeInMillis(thisMonthUtcMS);

    // Assert fields finer than a day are stripped.
    assertThat(thisMonthUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert this is the first day of this month in UTC.
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.YEAR))
        .isEqualTo(firstMomentOfTodayInUtc.get(Calendar.YEAR));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.MONTH))
        .isEqualTo(firstMomentOfTodayInUtc.get(Calendar.MONTH));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.DATE)).isEqualTo(1);
  }

  @Test
  public void testSelectionAsOpenAt() {
    MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    datePickerBuilder.setCalendarConstraints(calendarConstraints);
    datePickerBuilder.setSelection(APRIL_2016);
    datePickerBuilder.build();
    assertEquals(APRIL_2016, calendarConstraints.getOpenAt().timeInMillis);

    MaterialDatePicker.Builder<Long> datePickerBuilder2 = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints2 =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    datePickerBuilder2.setCalendarConstraints(calendarConstraints2);
    datePickerBuilder2.build();
    assertEquals(FEB_2016, calendarConstraints2.getOpenAt().timeInMillis);

    MaterialDatePicker.Builder<Long> datePickerBuilder3 = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints3 =
        new CalendarConstraints.Builder()
            .setStart(FEB_2016)
            .setEnd(APRIL_2016)
            .setOpenAt(MARCH_2016)
            .build();

    datePickerBuilder3.setCalendarConstraints(calendarConstraints3);
    datePickerBuilder3.setSelection(APRIL_2016);
    datePickerBuilder3.build();
    assertEquals(MARCH_2016, calendarConstraints3.getOpenAt().timeInMillis);

    // A corner case that the selected day is in the same month of the end month.
    MaterialDatePicker.Builder<Long> datePickerBuilder4 = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints4 =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    datePickerBuilder4.setCalendarConstraints(calendarConstraints4);
    datePickerBuilder4.setSelection(new GregorianCalendar(2016, APRIL, 20).getTimeInMillis());
    datePickerBuilder4.build();
    assertEquals(APRIL_2016, calendarConstraints4.getOpenAt().timeInMillis);
  }

  @Test
  public void testTodayInUtcMilliseconds() {
    testTodayInUtcMillisecondsForLocalTime(
        NEW_YORK_TIME_2019_12_31_11_59_00_PM, TimeZone.getTimeZone("America/New_York"));
    testTodayInUtcMillisecondsForLocalTime(
        NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM, TimeZone.getTimeZone("NZ"));
  }

  @Test
  public void testThisMonthInUtcMilliseconds() {
    testThisMonthInUtcMillisecondsForLocalTime(
        NEW_YORK_TIME_2019_12_31_11_59_00_PM, TimeZone.getTimeZone("America/New_York"));
    testThisMonthInUtcMillisecondsForLocalTime(
        NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM, TimeZone.getTimeZone("NZ"));
  }

  @Test
  public void testFirstDayOfWeekAsSunday() {
    MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setFirstDayOfWeek(SUNDAY).build();
    datePickerBuilder.setCalendarConstraints(calendarConstraints);
    datePickerBuilder.build();
    assertEquals(SUNDAY, calendarConstraints.getFirstDayOfWeek());
  }

  @Test
  public void testFirstDayOfWeekAsMonday() {
    MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setFirstDayOfWeek(MONDAY).build();
    datePickerBuilder.setCalendarConstraints(calendarConstraints);
    datePickerBuilder.build();
    assertEquals(MONDAY, calendarConstraints.getFirstDayOfWeek());
  }

  @Test
  public void testFirstDayOfWeekAsDefault() {
    MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints = new CalendarConstraints.Builder().build();
    datePickerBuilder.setCalendarConstraints(calendarConstraints);
    datePickerBuilder.build();
    assertEquals(0, calendarConstraints.getFirstDayOfWeek());
  }

  @Test
  public void testInputModeCalendarAsDefault() {
    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().build();

    assertEquals(INPUT_MODE_CALENDAR, materialDatePicker.getInputMode());
  }

  @Config(sdk = VERSION_CODES.P)
  @Test
  public void monthNavigationButtons_hideTooltipFromAccessibility() {
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder()
            .setStart(FEB_2016)
            .setEnd(APRIL_2016)
            .setOpenAt(MARCH_2016)
            .build();
    MaterialCalendar<Long> materialCalendar = startMaterialCalendar(calendarConstraints);

    View prevButton = materialCalendar.requireView().findViewById(R.id.month_navigation_previous);
    View nextButton = materialCalendar.requireView().findViewById(R.id.month_navigation_next);

    assertMonthNavigationButtonAccessibility(
        prevButton,
        R.string.mtrl_picker_a11y_prev_month,
        R.string.mtrl_picker_prev_month_tooltip,
        /* expectEnabled= */ true);
    assertMonthNavigationButtonAccessibility(
        nextButton,
        R.string.mtrl_picker_a11y_next_month,
        R.string.mtrl_picker_next_month_tooltip,
        /* expectEnabled= */ true);
  }

  @Config(sdk = VERSION_CODES.P)
  @Test
  public void monthNavigationButtons_disabledAtBounds_hideTooltipFromAccessibility() {
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder()
            .setStart(FEB_2016)
            .setEnd(FEB_2016)
            .setOpenAt(FEB_2016)
            .build();
    MaterialCalendar<Long> materialCalendar = startMaterialCalendar(calendarConstraints);

    View prevButton = materialCalendar.requireView().findViewById(R.id.month_navigation_previous);
    View nextButton = materialCalendar.requireView().findViewById(R.id.month_navigation_next);

    assertMonthNavigationButtonAccessibility(
        prevButton,
        R.string.mtrl_picker_a11y_prev_month,
        R.string.mtrl_picker_prev_month_tooltip,
        /* expectEnabled= */ false);
    assertMonthNavigationButtonAccessibility(
        nextButton,
        R.string.mtrl_picker_a11y_next_month,
        R.string.mtrl_picker_next_month_tooltip,
        /* expectEnabled= */ false);
  }

  @Config(sdk = VERSION_CODES.P)
  @Test
  public void headerToggle_accessibilityInfoFollowsInputMode() {
    ActivityController<AppCompatActivity> controller =
        Robolectric.buildActivity(AppCompatActivity.class);
    controller.get().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = controller.setup().get();
    MaterialDatePicker<Long> datePicker =
        MaterialDatePicker.Builder.datePicker().setInputMode(INPUT_MODE_CALENDAR).build();
    datePicker.showNow(activity.getSupportFragmentManager(), "");
    ShadowLooper.idleMainLooper();
    View toggle = datePicker.requireView().findViewById(R.id.mtrl_picker_header_toggle);

    assertHeaderToggleAccessibility(
        toggle,
        R.string.mtrl_picker_toggle_to_text_input_mode,
        R.string.mtrl_picker_toggle_to_text_input_mode_tooltip);

    toggle.performClick();
    ShadowLooper.idleMainLooper();

    assertHeaderToggleAccessibility(
        toggle,
        R.string.mtrl_picker_toggle_to_calendar_input_mode,
        R.string.mtrl_picker_toggle_to_calendar_input_mode_tooltip);
  }

  private static MaterialCalendar<Long> startMaterialCalendar(
      CalendarConstraints calendarConstraints) {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    MaterialCalendar<Long> materialCalendar =
        MaterialCalendar.newInstance(
            new SingleDateSelector(), R.style.Theme_MaterialComponents_Light, calendarConstraints);
    activity
        .getSupportFragmentManager()
        .beginTransaction()
        .add(android.R.id.content, materialCalendar)
        .commitNow();
    ShadowLooper.idleMainLooper();
    return materialCalendar;
  }

  private static void assertMonthNavigationButtonAccessibility(
      View button,
      @StringRes int descriptionRes,
      @StringRes int tooltipRes,
      boolean expectEnabled) {
    Resources res = button.getResources();
    String description = res.getString(descriptionRes);
    String tooltip = res.getString(tooltipRes);
    AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain();
    button.onInitializeAccessibilityNodeInfo(info.unwrap());

    assertThat(button.getContentDescription().toString()).isEqualTo(description);
    assertThat(info.isEnabled()).isEqualTo(expectEnabled);
    AccessibilityActionCompat clickAction = null;
    for (AccessibilityActionCompat action : info.getActionList()) {
      if (action.getId() == AccessibilityNodeInfoCompat.ACTION_CLICK) {
        clickAction = action;
        break;
      }
    }
    if (expectEnabled) {
      assertThat(clickAction).isNotNull();
      assertThat(clickAction.getLabel()).isNull();
    } else {
      assertThat(clickAction).isNull();
    }
    assertThat(info.getTooltipText()).isNull();
    assertThat(button.getTooltipText().toString()).isEqualTo(tooltip);
  }

  private static void assertHeaderToggleAccessibility(
      View toggle, @StringRes int descriptionRes, @StringRes int tooltipRes) {
    Resources res = toggle.getResources();
    String description = res.getString(descriptionRes);
    AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain();
    toggle.onInitializeAccessibilityNodeInfo(info.unwrap());

    assertThat(toggle.getContentDescription().toString()).isEqualTo(description);
    assertThat(info.isClickable()).isTrue();
    for (AccessibilityActionCompat action : info.getActionList()) {
      if (action.getId() == AccessibilityNodeInfoCompat.ACTION_CLICK) {
        assertThat(action.getLabel()).isNull();
      }
    }
    assertThat(info.getTooltipText()).isNull();
    assertThat(info.isCheckable()).isFalse();
    assertThat(toggle.getTooltipText().toString()).isEqualTo(res.getString(tooltipRes));
  }
}
