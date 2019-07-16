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
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import android.app.Activity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.test.espresso.ViewInteraction;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Calendar;
import org.hamcrest.core.IsEqual;

public final class PickerDialogFragmentTestUtils {

  private PickerDialogFragmentTestUtils() {}

  private static final ViewInteraction onViewPager =
      onView(withTagValue(equalTo(MaterialCalendar.VIEW_PAGER_TAG)));

  public static MaterialDatePicker<Long> showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule) {
    return showDatePicker(activityTestRule, 0);
  }

  public static MaterialDatePicker<Long> showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      CalendarConstraints calendarConstraints) {
    return showDatePicker(activityTestRule, 0, calendarConstraints);
  }

  public static MaterialDatePicker<Long> showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule, int themeResId) {

    Month start = Month.create(1900, Calendar.JANUARY);
    Month end = Month.create(2100, Calendar.DECEMBER);
    Month current = Month.create(2018, Calendar.APRIL);
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(start).setEnd(end).setOpening(current).build();

    return showDatePicker(activityTestRule, themeResId, calendarConstraints);
  }

  public static MaterialDatePicker<Long> showDatePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      int themeResId,
      CalendarConstraints calendarConstraints) {
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date DialogFragment";

    MaterialDatePicker<Long> dialogFragment =
        MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints)
            .setTheme(themeResId)
            .build();
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

  public static MaterialDatePicker<Pair<Long, Long>> showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule) {
    return showRangePicker(activityTestRule, 0);
  }

  public static MaterialDatePicker<Pair<Long, Long>> showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      CalendarConstraints calendarConstraints) {
    return showRangePicker(activityTestRule, 0, calendarConstraints);
  }

  public static MaterialDatePicker<Pair<Long, Long>> showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule, int themeResId) {
    Month start = Month.create(1900, Calendar.JANUARY);
    Month end = Month.create(2100, Calendar.DECEMBER);
    Month current = Month.create(2018, Calendar.APRIL);
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(start).setEnd(end).setOpening(current).build();
    return showRangePicker(activityTestRule, themeResId, calendarConstraints);
  }

  static MaterialDatePicker<Pair<Long, Long>> showRangePicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      int themeResId,
      CalendarConstraints calendarConstraints) {
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date Range DialogFragment";

    MaterialDatePicker<Pair<Long, Long>> dialogFragment =
        MaterialDatePicker.Builder.dateRangePicker()
            .setCalendarConstraints(calendarConstraints)
            .setTheme(themeResId)
            .build();
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

  static <S> MaterialDatePicker<S> showPicker(
      ActivityTestRule<? extends AppCompatActivity> activityTestRule,
      MaterialDatePicker.Builder<S> builder) {
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date Range DialogFragment";

    MaterialDatePicker<S> dialogFragment = builder.build();
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

  public static void clickDay(Month month, int day) {
    onView(
            allOf(
                isDescendantOfA(withTagValue(equalTo(MaterialCalendar.VIEW_PAGER_TAG))),
                withTagValue(IsEqual.<Object>equalTo(month)),
                withText(String.valueOf(day))))
        .perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickOk() {
    onView(withTagValue(equalTo(MaterialDatePicker.CONFIRM_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickCancel() {
    onView(withTagValue(equalTo(MaterialDatePicker.CANCEL_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void swipeEarlier(DialogFragment dialogFragment) {
    int orientation =
        ((ViewPager2) dialogFragment.getView().findViewWithTag(MaterialCalendar.VIEW_PAGER_TAG))
            .getOrientation();
    if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
      onViewPager.perform(swipeRight());
    } else {
      onViewPager.perform(swipeDown());
    }
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void swipeLater(DialogFragment dialogFragment) {
    int orientation =
        ((ViewPager2) dialogFragment.getView().findViewWithTag(MaterialCalendar.VIEW_PAGER_TAG))
            .getOrientation();
    if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
      onViewPager.perform(swipeLeft());
    } else {
      onViewPager.perform(swipeUp());
    }
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickHeaderToggle(Fragment fragment) {
    onView(withTagValue(equalTo(MaterialDatePicker.TOGGLE_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    hideAllEditTextCursors(fragment.getView());
  }

  static void setPage(Activity activity, DialogFragment dialogFragment, final int position) {
    ViewPager2 viewPager2 =
        dialogFragment.getView().findViewWithTag(MaterialCalendar.VIEW_PAGER_TAG);
    activity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            viewPager2.setCurrentItem(position, false);
          }
        });
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
