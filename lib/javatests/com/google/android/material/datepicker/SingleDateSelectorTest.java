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
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.internal.ParcelableTestUtils;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class SingleDateSelectorTest {

  private SingleDateSelector singleDateSelector;
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
    singleDateSelector = new SingleDateSelector();
    adapter =
        new MonthAdapter(
            Month.create(2016, Calendar.FEBRUARY),
            singleDateSelector,
            new CalendarConstraints.Builder().build(),
            /* dayViewDecorator= */ null);
    gridView.setAdapter(adapter);
  }

  @Test
  public void dateSelectorMaintainsSelectionAfterParceling() {
    int position = 8;
    assertThat(adapter.withinMonth(position)).isTrue();
    singleDateSelector.select(adapter.getItem(position));
    long expected = adapter.getItem(position);
    SingleDateSelector singleDateSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(singleDateSelector, SingleDateSelector.CREATOR);
    assertThat(singleDateSelectorFromParcel.getSelection()).isEqualTo(expected);
  }

  @Test
  public void nullDateSelectionFromParcel() {
    SingleDateSelector singleDateSelector =
        ParcelableTestUtils.parcelAndCreate(this.singleDateSelector, SingleDateSelector.CREATOR);
    assertThat(singleDateSelector.getSelection()).isNull();
  }

  @Test
  public void setSelectionDirectly() {
    Calendar setTo = UtcDates.getUtcCalendar();
    setTo.set(2004, Calendar.MARCH, 5);
    singleDateSelector.setSelection(setTo.getTimeInMillis());
    Calendar resultCalendar = UtcDates.getUtcCalendar();

    resultCalendar.setTimeInMillis(singleDateSelector.getSelection());

    assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(5);
    assertThat(
            singleDateSelector
                .getSelectedDays()
                .contains(UtcDates.canonicalYearMonthDay(setTo.getTimeInMillis())))
        .isTrue();
    assertThat(
            singleDateSelector
                .getSelectedDays()
                .contains(
                    UtcDates.canonicalYearMonthDay(UtcDates.getTodayCalendar().getTimeInMillis())))
        .isFalse();
  }

  @Test
  public void getSelectionContentDescription_empty_returnsNone() {
    singleDateSelector.setSelection(null);
    String contentDescription = singleDateSelector.getSelectionContentDescription(context);

    String expected =
        res.getString(
            R.string.mtrl_picker_announce_current_selection,
            res.getString(R.string.mtrl_picker_announce_current_selection_none));
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getSelectionContentDescription_notEmpty_returnsDate() {
    Calendar calendar = UtcDates.getUtcCalendar();
    calendar.set(2016, Calendar.FEBRUARY, 1);
    singleDateSelector.setSelection(calendar.getTimeInMillis());
    String contentDescription = singleDateSelector.getSelectionContentDescription(context);

    String expected = res.getString(R.string.mtrl_picker_announce_current_selection, "Feb 1, 2016");
    assertThat(contentDescription).isEqualTo(expected);
  }

  @Test
  public void getError_emptyDate_isNull() {
    assertThat(singleDateSelector.getError()).isNull();
  }

  @Test
  public void getError_validDate_isNull() {
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);
    TextInputLayout textInputLayout = root.findViewById(R.id.mtrl_picker_text_input_date);
    textInputLayout.getEditText().setText("1/1/11");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(singleDateSelector.getError()).isNull();
  }

  @Test
  public void getError_invalidDate_isNotEmpty() {
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);
    TextInputLayout textInputLayout = root.findViewById(R.id.mtrl_picker_text_input_date);
    textInputLayout.getEditText().setText("1/1/");
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(singleDateSelector.getError()).isNotEmpty();
  }

  @Test
  public void getSelectedRanges_isEmpty() {
    Calendar calendar = UtcDates.getUtcCalendar();
    calendar.set(2016, Calendar.FEBRUARY, 1);
    singleDateSelector.setSelection(calendar.getTimeInMillis());

    assertThat(singleDateSelector.getSelectedRanges().isEmpty()).isTrue();
  }

  @Test
  public void textFieldPlaceholder_usesDefaultFormat() {
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);

    TextInputLayout textInputLayout = root.findViewById(R.id.mtrl_picker_text_input_date);

    assertThat(textInputLayout.getPlaceholderText().toString()).isEqualTo("m/d/yy");
  }

  @Test
  public void textFieldPlaceholder_usesCustomFormat() {
    singleDateSelector.setTextInputFormat(new SimpleDateFormat("kk:mm:ss mm/dd/yyyy"));
    View root = getRootView();
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(root);

    TextInputLayout textInputLayout = root.findViewById(R.id.mtrl_picker_text_input_date);

    assertThat(textInputLayout.getPlaceholderText().toString()).isEqualTo("kk:mm:ss mm/dd/yyyy");
  }

  private View getRootView() {
    return singleDateSelector.onCreateTextInputView(
        LayoutInflater.from(context),
        null,
        null,
        new CalendarConstraints.Builder().build(),
        new OnSelectionChangedListener<Long>() {
          @Override
          public void onSelectionChanged(@NonNull Long selection) {}
        });
  }
}
