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

import static com.google.android.material.datepicker.MaterialDatePickerTestUtils.findFirstVisibleItem;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MaterialDatePickerPagesTest {

  private static final Month START = Month.create(2000, Calendar.JANUARY);
  private static final Month END = Month.create(2000, Calendar.MAY);
  private static final Month OPEN_AT = Month.create(2000, Calendar.FEBRUARY);

  private ListenerIdlingResource listenerIdlingResource;

  @Rule
  public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  private MaterialDatePicker<Long> dialogFragment;

  @Before
  public void setupDatePickerDialogForSwiping() {
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder()
            .setStart(START.timeInMillis)
            .setEnd(END.timeInMillis)
            .setOpenAt(OPEN_AT.timeInMillis)
            .build();
    FragmentManager fragmentManager = activityTestRule.getActivity().getSupportFragmentManager();
    String tag = "Date DialogFragment";

    dialogFragment =
        MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints).build();
    dialogFragment.show(fragmentManager, tag);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    IdlingRegistry.getInstance()
        .register(
            new RecyclerIdlingResource(
                dialogFragment.getView().findViewWithTag(MaterialCalendar.MONTHS_VIEW_GROUP_TAG)));
    listenerIdlingResource = new ListenerIdlingResource();
  }

  @Test
  public void calendarSwipeCappedAtStart() {
    MaterialDatePickerTestUtils.swipeEarlier(dialogFragment);
    MaterialDatePickerTestUtils.swipeEarlier(dialogFragment);
    MaterialDatePickerTestUtils.swipeEarlier(dialogFragment);
    MaterialDatePickerTestUtils.swipeEarlier(dialogFragment);

    assertEquals(START, findFirstVisibleItem(dialogFragment));
  }

  @Test
  public void calendarSwipeCappedAtEnd() {
    MaterialDatePickerTestUtils.swipeLater(dialogFragment);
    MaterialDatePickerTestUtils.swipeLater(dialogFragment);
    MaterialDatePickerTestUtils.swipeLater(dialogFragment);
    MaterialDatePickerTestUtils.swipeLater(dialogFragment);
    assertEquals(END, findFirstVisibleItem(dialogFragment));
  }

  @Test
  public void calendarOpensOnCurrent() {
    assertEquals(OPEN_AT, findFirstVisibleItem(dialogFragment));
  }

  @Test
  public void swipeChangesMonth() {
    // This listener with idling resource guarantees that the call to getSelection() is not null.
    dialogFragment.addOnPositiveButtonClickListener(
        new MaterialPickerOnPositiveButtonClickListener<Long>() {
          @Override
          public void onPositiveButtonClick(Long selection) {
            listenerIdlingResource.callFromListener();
          }
        });
    Calendar startingTimeOfMonth = Calendar.getInstance();
    startingTimeOfMonth.clear();
    startingTimeOfMonth.set(OPEN_AT.year, OPEN_AT.month, 1);
    MaterialDatePickerTestUtils.swipeEarlier(dialogFragment);
    MaterialDatePickerTestUtils.clickDialogVisibleDay(5);

    MaterialDatePickerTestUtils.clickOk();
    listenerIdlingResource.beginBlocking();
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(dialogFragment.getSelection());

    assertThat(c.getTimeInMillis(), lessThan(startingTimeOfMonth.getTimeInMillis()));
  }

  @Test
  public void changingSelectorToDayMaintainsCurrentMonth() {
    MaterialDatePickerTestUtils.clickNext();
    MaterialDatePickerTestUtils.clickSelectorToggle();
    MaterialDatePickerTestUtils.clickSelectorToggle();
    assertEquals(findFirstVisibleItem(dialogFragment), OPEN_AT.monthsLater(1));
  }
}
