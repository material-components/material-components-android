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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import android.app.Activity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import java.util.Calendar;
import org.hamcrest.core.IsEqual;

public final class MaterialDatePickerTestUtils {

  private static final Month OPEN_AT = Month.create(2018, Calendar.APRIL);
  @VisibleForTesting public static final long OPENING_TIME = OPEN_AT.timeInMillis;

  private MaterialDatePickerTestUtils() {}

  private static final ViewInteraction onMonthsGroup =
      onView(withTagValue(equalTo(MaterialCalendar.MONTHS_VIEW_GROUP_TAG)));

  public static <S> MaterialDatePicker<S> buildAndShow(
      AppCompatActivity activity, MaterialDatePicker.Builder<S> builder) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    String tag = "DialogFragment";
    MaterialDatePicker<S> dialogFragment = builder.build();
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    return dialogFragment;
  }

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
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder()
            .setStart(start.timeInMillis)
            .setEnd(end.timeInMillis)
            .setOpenAt(OPENING_TIME)
            .build();

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
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder()
            .setStart(start.timeInMillis)
            .setEnd(end.timeInMillis)
            .setOpenAt(OPENING_TIME)
            .build();
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

  public static void clickDay(long timeInMillis, int day) {
    clickDay(Month.create(timeInMillis), day);
  }

  public static void clickDay(Month month, int day) {
    onView(
            allOf(
                isDescendantOfA(withTagValue(equalTo(MaterialCalendar.MONTHS_VIEW_GROUP_TAG))),
                withTagValue(IsEqual.<Object>equalTo(month)),
                withText(String.valueOf(day))))
        .perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  public static void clickDialogVisibleDay(int day) {
    onView(
            allOf(
                isDescendantOfA(withTagValue(equalTo(MaterialCalendar.MONTHS_VIEW_GROUP_TAG))),
                isCompletelyDisplayed(),
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

  static void clickNext() {
    onView(withTagValue(equalTo(MaterialCalendar.NAVIGATION_NEXT_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickPrev() {
    onView(withTagValue(equalTo(MaterialCalendar.NAVIGATION_PREV_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  static void clickSelectorToggle() {
    onView(withTagValue(equalTo(MaterialCalendar.SELECTOR_TOGGLE_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  @VisibleForTesting
  public static void swipeEarlier(DialogFragment dialogFragment) {
    if (isHorizontal(dialogFragment)) {
      swipeRight(dialogFragment);
    } else {
      swipeDown(dialogFragment);
    }
  }

  @VisibleForTesting
  public static void swipeLater(DialogFragment dialogFragment) {
    if (isHorizontal(dialogFragment)) {
      swipeLeft(dialogFragment);
    } else {
      swipeUp(dialogFragment);
    }
  }

  @VisibleForTesting
  public static void swipeLeft(DialogFragment dialogFragment) {
    onMonthsGroup.perform(swipeAction(GeneralLocation.CENTER_RIGHT, GeneralLocation.CENTER_LEFT));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  @VisibleForTesting
  public static void swipeRight(DialogFragment dialogFragment) {
    onMonthsGroup.perform(swipeAction(GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  @VisibleForTesting
  public static void swipeUp(DialogFragment dialogFragment) {
    onMonthsGroup.perform(swipeAction(GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  @VisibleForTesting
  public static void swipeDown(DialogFragment dialogFragment) {
    onMonthsGroup.perform(swipeAction(GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER));
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  private static boolean isHorizontal(DialogFragment dialogFragment) {
    return getPagingOrientation(dialogFragment) == LinearLayoutManager.HORIZONTAL;
  }

  private static GeneralSwipeAction swipeAction(
      CoordinatesProvider startCoordinatesProvider, CoordinatesProvider endCoordinatesProvider) {
    return new GeneralSwipeAction(
        Swipe.SLOW, startCoordinatesProvider, endCoordinatesProvider, Press.FINGER);
  }

  static void clickHeaderToggle(Fragment fragment) {
    onView(withTagValue(equalTo(MaterialDatePicker.TOGGLE_BUTTON_TAG))).perform(click());
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    hideAllEditTextCursors(fragment.getView());
  }

  static void setPage(Activity activity, DialogFragment dialogFragment, final int position) {
    RecyclerView recyclerView = getMonthsViewGroup(dialogFragment);
    activity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            recyclerView.scrollToPosition(position);
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

  @VisibleForTesting
  public static long findFirstVisibleItemTime(DialogFragment dialogFragment) {
    return findFirstVisibleItem(dialogFragment).timeInMillis;
  }

  @VisibleForTesting
  public static Month findFirstVisibleItem(DialogFragment dialogFragment) {
    RecyclerView recyclerView =
        dialogFragment.getView().findViewWithTag(MaterialCalendar.MONTHS_VIEW_GROUP_TAG);
    MonthsPagerAdapter monthsPagerAdapter = (MonthsPagerAdapter) recyclerView.getAdapter();
    return monthsPagerAdapter.getPageMonth(
        ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
  }

  private static int getPagingOrientation(DialogFragment dialogFragment) {
    return ((LinearLayoutManager) getMonthsViewGroup(dialogFragment).getLayoutManager())
        .getOrientation();
  }

  private static RecyclerView getMonthsViewGroup(DialogFragment dialogFragment) {
    return dialogFragment.getView().findViewWithTag(MaterialCalendar.MONTHS_VIEW_GROUP_TAG);
  }
}
