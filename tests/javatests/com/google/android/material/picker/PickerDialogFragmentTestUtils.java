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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import java.util.Calendar;
import org.hamcrest.core.IsEqual;

public final class PickerDialogFragmentTestUtils {

  private PickerDialogFragmentTestUtils() {}

  static MaterialDatePickerDialogFragment showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      CalendarBounds calendarBounds) {
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date DialogFragment";

    MaterialDatePickerDialogFragment dialogFragment =
        MaterialDatePickerDialogFragment.newInstance(calendarBounds);
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

  static MaterialDatePickerDialogFragment showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule) {

    Month start = Month.create(1900, Calendar.JANUARY);
    Month end = Month.create(2100, Calendar.DECEMBER);
    Month current = Month.create(2018, Calendar.APRIL);
    CalendarBounds calendarBounds = CalendarBounds.create(start, end, current);

    return showDatePicker(activityTestRule, calendarBounds);
  }

  static MaterialDateRangePickerDialogFragment showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      CalendarBounds calendarBounds) {
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date Range DialogFragment";

    MaterialDateRangePickerDialogFragment dialogFragment =
        MaterialDateRangePickerDialogFragment.newInstance(calendarBounds);
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

  static MaterialDateRangePickerDialogFragment showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule) {
    Month start = Month.create(1900, Calendar.JANUARY);
    Month end = Month.create(2100, Calendar.DECEMBER);
    Month current = Month.create(2018, Calendar.APRIL);
    CalendarBounds calendarBounds = CalendarBounds.create(start, end, current);
    return showRangePicker(activityTestRule, calendarBounds);
  }

  static void clickDay(Month month, int day) {
    onView(
            allOf(
                isDescendantOfA(withTagValue(equalTo(MaterialCalendar.VIEW_PAGER_TAG))),
                withTagValue(IsEqual.<Object>equalTo(month)),
                withText(String.valueOf(day))))
        .perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickOk() {
    onView(withTagValue(equalTo(MaterialPickerDialogFragment.CONFIRM_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickCancel() {
    onView(withTagValue(equalTo(MaterialPickerDialogFragment.CANCEL_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void swipeEarlier() {
    onView(withTagValue(equalTo(MaterialCalendar.VIEW_PAGER_TAG))).perform(swipeRight());
  }

  static void swipeLater() {
    onView(withTagValue(equalTo(MaterialCalendar.VIEW_PAGER_TAG))).perform(swipeLeft());
  }

  static void clickHeaderToggle(Fragment fragment) {
    onView(withTagValue(equalTo(MaterialPickerDialogFragment.TOGGLE_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    hideAllEditTextCursors(fragment.getView());
  }

  static void hideAllEditTextCursors(View view) {
    if (view instanceof EditText) {
      ((EditText) view).setCursorVisible(false);
    } else if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        hideAllEditTextCursors(viewGroup.getChildAt(i));
      }
    }
  }
}
