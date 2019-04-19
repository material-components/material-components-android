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
package com.google.android.material.picker.selector;

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import com.google.android.material.picker.Month;
import com.google.android.material.picker.MonthAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.GridView;
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

  private Context context;
  private DateRangeGridSelector dateRangeGridSelector;
  private MonthAdapter adapter;
  private AdapterView<MonthAdapter> adapterView;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    GridView gridView = new GridView(context);
    dateRangeGridSelector = new DateRangeGridSelector();
    adapter =
        new MonthAdapter(context, Month.create(2016, Calendar.FEBRUARY), dateRangeGridSelector);
    gridView.setAdapter(adapter);
    @SuppressWarnings("unchecked")
    AdapterView<MonthAdapter> adapterView = (AdapterView) gridView;
    this.adapterView = adapterView;
  }

  @Test
  public void dateRangeDrawCell() {
    int startPosition = 8;
    int endPosition = 15;
    assertTrue(adapter.withinMonth(startPosition));
    assertTrue(adapter.withinMonth(endPosition));
    dateRangeGridSelector.changeSelection(
        adapterView, /* view= */ null, startPosition, /* row= */ 1);
    dateRangeGridSelector.changeSelection(adapterView, /* view= */ null, endPosition, /* row= */ 2);

    GridSelectorTestUtils.assertCellColor(
        context, dateRangeGridSelector, adapter, startPosition, DateRangeGridSelector.startColor);
    GridSelectorTestUtils.assertCellColor(
        context,
        dateRangeGridSelector,
        adapter,
        /* position= */ 12,
        DateRangeGridSelector.rangeColor);
    GridSelectorTestUtils.assertCellColor(
        context, dateRangeGridSelector, adapter, endPosition, DateRangeGridSelector.endColor);
    GridSelectorTestUtils.assertCellColor(
        context,
        dateRangeGridSelector,
        adapter,
        /* position= */ 16,
        DateRangeGridSelector.emptyColor);
  }

  @Test
  public void dateRangeGridSelectorMaintainsSelectionAfterParceling() {
    int startPosition = 8;
    int endPosition = 15;
    Calendar expectedStart = adapter.getItem(startPosition);
    Calendar expectedEnd = adapter.getItem(endPosition);

    dateRangeGridSelector.changeSelection(
        adapterView, /* view= */ null, startPosition, /* row= */ 1);
    dateRangeGridSelector.changeSelection(adapterView, /* view= */ null, endPosition, /* row= */ 2);
    DateRangeGridSelector dateRangeGridSelectorFromParcel =
        GridSelectorTestUtils.parcelAndCreate(dateRangeGridSelector, DateRangeGridSelector.CREATOR);

    assertTrue(adapter.withinMonth(startPosition));
    assertTrue(adapter.withinMonth(endPosition));

    assertEquals(expectedStart, dateRangeGridSelectorFromParcel.getStart());
    assertEquals(expectedEnd, dateRangeGridSelectorFromParcel.getEnd());
  }

  @Test
  public void nullDateSelectionFromParcel() {
    DateRangeGridSelector dateRangeGridSelectorFromParcel =
        GridSelectorTestUtils.parcelAndCreate(dateRangeGridSelector, DateRangeGridSelector.CREATOR);
    assertNull(dateRangeGridSelectorFromParcel.getStart());
    assertNull(dateRangeGridSelectorFromParcel.getEnd());
  }
}
