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

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView.BufferType;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.internal.ParcelableTestUtils;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class RangeDateSelectorTest {

  private RangeDateSelector rangeDateSelector;
  private MonthAdapter adapter;
  private Context context;
  private Resources res;
  private AppCompatActivity activity;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    res = context.getResources();
    GridView gridView = new GridView(context);
    rangeDateSelector = new RangeDateSelector();
    adapter =
        new MonthAdapter(
            Month.create(2016, Calendar.FEBRUARY),
            rangeDateSelector,
            new CalendarConstraints.Builder().build(),
            /* dayViewDecorator= */ null);
    gridView.setAdapter(adapter);
  }

  @Test
  public void textInputValid() {
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2012", BufferType.EDITABLE);

    activity.setContentView(root);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(startTextInput.getError()).isNull();
    assertThat(endTextInput.getError()).isNull();
  }

  @Test
  public void textInputFormatError() {
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);

    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    startTextInput.getEditText().setText("22/22/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("555-555-5555", BufferType.EDITABLE);

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(startTextInput.getError()).isNotNull();
    assertThat(endTextInput.getError()).isNotNull();
  }

  @Test
  public void textFieldFormatPlaceholder() {
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);

    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);

    assertThat(startTextInput.getPlaceholderText().toString()).isEqualTo("m/d/yy");
  }

  @Test
  public void customTextFieldFormatPlaceholder() {
    rangeDateSelector.setTextInputFormat(new SimpleDateFormat("MM/dd/y,ww"));
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);

    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);

    assertThat(startTextInput.getPlaceholderText().toString()).isEqualTo("MM/dd/y,ww");
  }

  @Test
  public void textInputRangeError() {
    rangeDateSelector = new RangeDateSelector();
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2008", BufferType.EDITABLE);

    activity.setContentView(root);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(startTextInput.getError().toString())
        .isEqualTo(context.getString(R.string.mtrl_picker_invalid_range));
    assertThat(endTextInput.getError()).isNotNull();
  }

  @Test
  public void rangeDateSelectorMaintainsStateAfterParceling() {
    int startPosition = 8;
    int endPosition = 15;
    long expectedStart = adapter.getItem(startPosition);
    long expectedEnd = adapter.getItem(endPosition);

    rangeDateSelector.select(adapter.getItem(startPosition));
    rangeDateSelector.select(adapter.getItem(endPosition));
    RangeDateSelector rangeDateSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(rangeDateSelector, RangeDateSelector.CREATOR);

    assertThat(adapter.withinMonth(startPosition)).isTrue();
    assertThat(adapter.withinMonth(endPosition)).isTrue();

    Pair<Long, Long> selection = rangeDateSelectorFromParcel.getSelection();
    assertThat(selection.first).isEqualTo(expectedStart);
    assertThat(selection.second).isEqualTo(expectedEnd);
  }

  @Test
  public void nullDateSelectionFromParcel() {
    RangeDateSelector rangeDateSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(rangeDateSelector, RangeDateSelector.CREATOR);

    Pair<Long, Long> selection = rangeDateSelectorFromParcel.getSelection();

    assertThat(selection.first).isNull();
    assertThat(selection.second).isNull();
  }

  @Test
  public void setSelectionDirectly() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    Calendar setToEnd = UtcDates.getUtcCalendar();
    setToEnd.set(2005, Calendar.FEBRUARY, 1);

    rangeDateSelector.setSelection(
        new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis()));
    Calendar resultCalendarStart = UtcDates.getUtcCalendar();
    Calendar resultCalendarEnd = UtcDates.getUtcCalendar();
    resultCalendarStart.setTimeInMillis(rangeDateSelector.getSelection().first);
    resultCalendarEnd.setTimeInMillis(rangeDateSelector.getSelection().second);

    assertThat(resultCalendarStart.get(Calendar.DAY_OF_MONTH)).isEqualTo(5);
    assertThat(resultCalendarEnd.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    assertThat(rangeDateSelector.getSelectedDays())
        .contains(UtcDates.canonicalYearMonthDay(setToStart.getTimeInMillis()));

    assertThat(rangeDateSelector.getSelectedDays())
        .contains(UtcDates.canonicalYearMonthDay(setToEnd.getTimeInMillis()));

    assertThat(rangeDateSelector.getSelectedDays())
        .doesNotContain(
            UtcDates.canonicalYearMonthDay(UtcDates.getTodayCalendar().getTimeInMillis()));
  }

  @Test
  public void invalidSetThrowsException() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2005, Calendar.FEBRUARY, 1);
    Calendar setToEnd = UtcDates.getUtcCalendar();
    setToEnd.set(2004, Calendar.MARCH, 5);

    // ThrowingRunnable used by assertThrows is not available until gradle 4.13
    try {
      rangeDateSelector.setSelection(
          new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis()));
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail();
  }

  @Test
  @Config(qualifiers = "en-rUS")
  public void textInputHintValidWithUSLocale() {
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    activity.setContentView(root);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(startTextInput.getPlaceholderText().toString()).isEqualTo("m/d/yy");
    assertThat(endTextInput.getPlaceholderText().toString()).isEqualTo("m/d/yy");
  }

  @Test
  @Config(qualifiers = "pt")
  public void textInputHintValidWithPTLocale() {
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    activity.setContentView(root);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    // Some JVMs format PT dates as dd-mm-aaaa, and some as dd/mm/aaaa. Derive the expected result
    // programmatically to account for the difference.
    boolean slashDateFormat = new SimpleDateFormat().toLocalizedPattern().startsWith("dd/");
    String expectedDateFormat = slashDateFormat ? "dd/mm/aaaa" : "dd-mm-aaaa";
    assertThat(startTextInput.getPlaceholderText().toString()).isEqualTo(expectedDateFormat);
    assertThat(endTextInput.getPlaceholderText().toString()).isEqualTo(expectedDateFormat);
  }

  @Test
  public void getSelectionContentDescription_startEmpty_endEmpty_returnsStartAndEndNone() {
    rangeDateSelector.setSelection(new Pair<>(null, null));
    String contentDescription = rangeDateSelector.getSelectionContentDescription(context);

    String expected =
        res.getString(
            R.string.mtrl_picker_announce_current_range_selection,
            res.getString(R.string.mtrl_picker_announce_current_selection_none),
            res.getString(R.string.mtrl_picker_announce_current_selection_none));
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getSelectionContentDescription_startNotEmpty_endEmpty_returnsEndNone() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    rangeDateSelector.setSelection(new Pair<>(setToStart.getTimeInMillis(), null));
    String contentDescription = rangeDateSelector.getSelectionContentDescription(context);

    String expected =
        res.getString(
            R.string.mtrl_picker_announce_current_range_selection,
            "Mar 5, 2004",
            res.getString(R.string.mtrl_picker_announce_current_selection_none));
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getSelectionContentDescription_startEmpty_endNotEmpty_returnsStartNone() {
    Calendar setToEnd = UtcDates.getUtcCalendar();
    setToEnd.set(2005, Calendar.FEBRUARY, 1);
    rangeDateSelector.setSelection(new Pair<>(null, setToEnd.getTimeInMillis()));
    String contentDescription = rangeDateSelector.getSelectionContentDescription(context);

    String expected =
        res.getString(
            R.string.mtrl_picker_announce_current_range_selection,
            res.getString(R.string.mtrl_picker_announce_current_selection_none),
            "Feb 1, 2005");
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getSelectionContentDescription_startNotEmpty_endNotEmpty_returnsStartAndEndDates() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    Calendar setToEnd = UtcDates.getUtcCalendar();
    setToEnd.set(2005, Calendar.FEBRUARY, 1);
    rangeDateSelector.setSelection(
        new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis()));
    String contentDescription = rangeDateSelector.getSelectionContentDescription(context);

    String expected =
        res.getString(
            R.string.mtrl_picker_announce_current_range_selection, "Mar 5, 2004", "Feb 1, 2005");
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getError_emptyDates_isNull() {
    assertThat(rangeDateSelector.getError()).isNull();
  }

  @Test
  public void getError_validStartDate_isNull() {
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    activity.setContentView(root);
    startTextInput.getEditText().setText("1/1/11");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(rangeDateSelector.getError()).isNull();
  }

  @Test
  public void getError_validEndDate_isNull() {
    View root = getRootView();
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    activity.setContentView(root);
    endTextInput.getEditText().setText("1/1/11");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(rangeDateSelector.getError()).isNull();
  }

  @Test
  public void getError_invalidStartDate_isNotEmpty() {
    View root = getRootView();
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    activity.setContentView(root);
    startTextInput.getEditText().setText("1/1/");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(rangeDateSelector.getError()).isNotEmpty();
  }

  @Test
  public void getError_invalidEndDate_isNotEmpty() {
    View root = getRootView();
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    activity.setContentView(root);
    endTextInput.getEditText().setText("1/1/");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(rangeDateSelector.getError()).isNotEmpty();
  }

  @Test
  public void getSelectedRanges_fullRange() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    Calendar setToEnd = UtcDates.getUtcCalendar();
    setToEnd.set(2005, Calendar.FEBRUARY, 1);
    Pair<Long, Long> selection =
        new Pair<>(setToStart.getTimeInMillis(), setToEnd.getTimeInMillis());
    rangeDateSelector.setSelection(selection);

    assertThat(rangeDateSelector.getSelectedRanges()).containsExactly(selection);
  }

  @Test
  public void getSelectedRanges_partialRange() {
    Calendar setToStart = UtcDates.getUtcCalendar();
    setToStart.set(2004, Calendar.MARCH, 5);
    Pair<Long, Long> selection = new Pair<>(setToStart.getTimeInMillis(), null);
    rangeDateSelector.setSelection(selection);

    assertThat(rangeDateSelector.getSelectedRanges()).containsExactly(selection);
  }

  private View getRootView() {
    return rangeDateSelector.onCreateTextInputView(
        LayoutInflater.from(context),
        null,
        null,
        new CalendarConstraints.Builder().build(),
        new OnSelectionChangedListener<Pair<Long, Long>>() {
          @Override
          public void onSelectionChanged(Pair<Long, Long> selection) {}
        });
  }
}
