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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import android.content.Context;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView.BufferType;
import androidx.test.core.app.ApplicationProvider;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class DateRangeGridSelectorTest {

  private DateRangeGridSelector dateRangeGridSelector;
  private MonthAdapter adapter;
  private Context context;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    GridView gridView = new GridView(context);
    dateRangeGridSelector = new DateRangeGridSelector();
    adapter =
        new MonthAdapter(
            Month.create(2016, Calendar.FEBRUARY),
            dateRangeGridSelector,
            new CalendarConstraints.Builder().build());
    gridView.setAdapter(adapter);
  }

  @Test
  public void textInputValid() {
    View root =
        dateRangeGridSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2012", BufferType.EDITABLE);

    assertThat(startTextInput.getError(), nullValue());
    assertThat(endTextInput.getError(), nullValue());
  }

  @Test
  public void textInputFormatError() {
    View root =
        dateRangeGridSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("22/22/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("555-555-5555", BufferType.EDITABLE);

    assertThat(startTextInput.getError(), notNullValue());
    assertThat(endTextInput.getError(), notNullValue());
  }

  @Test
  public void textInputRangeError() {
    dateRangeGridSelector = new DateRangeGridSelector();
    View root =
        dateRangeGridSelector.onCreateTextInputView(
            LayoutInflater.from(context),
            null,
            null,
            new CalendarConstraints.Builder().build(),
            new OnSelectionChangedListener<Pair<Long, Long>>() {
              @Override
              public void onSelectionChanged(Pair<Long, Long> selection) {}
            });
    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);

    startTextInput.getEditText().setText("2/2/2010", BufferType.EDITABLE);
    endTextInput.getEditText().setText("2/2/2008", BufferType.EDITABLE);

    assertThat(
        startTextInput.getError(),
        is((CharSequence) context.getString(R.string.mtrl_picker_invalid_range)));
    assertThat(endTextInput.getError(), notNullValue());
  }

  @Test
  public void dateRangeGridSelectorMaintainsSelectionAfterParceling() {
    int startPosition = 8;
    int endPosition = 15;
    long expectedStart = adapter.getItem(startPosition);
    long expectedEnd = adapter.getItem(endPosition);

    dateRangeGridSelector.select(adapter.getItem(startPosition));
    dateRangeGridSelector.select(adapter.getItem(endPosition));
    DateRangeGridSelector dateRangeGridSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(dateRangeGridSelector, DateRangeGridSelector.CREATOR);

    assertThat(adapter.withinMonth(startPosition), is(true));
    assertThat(adapter.withinMonth(endPosition), is(true));
    assertThat(dateRangeGridSelectorFromParcel.getSelection().first, is(expectedStart));
    assertThat(dateRangeGridSelectorFromParcel.getSelection().second, is(expectedEnd));
  }

  @Test
  public void nullDateSelectionFromParcel() {
    DateRangeGridSelector dateRangeGridSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(dateRangeGridSelector, DateRangeGridSelector.CREATOR);
    assertThat(dateRangeGridSelectorFromParcel.getSelection().first, nullValue());
    assertThat(dateRangeGridSelectorFromParcel.getSelection().second, nullValue());
  }
}
