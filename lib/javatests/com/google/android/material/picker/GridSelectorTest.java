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

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
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
public class GridSelectorTest {

  private Context context;

  @Before
  public void setupMonthInYearAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
  }

  @Test
  public void testDateDrawCell() {
    DateGridSelector dateGridSelector = new DateGridSelector();
    GridView gridView = new GridView(context);
    MonthInYearAdapter adapter =
        new MonthInYearAdapter(
            context, MonthInYear.create(2016, Calendar.FEBRUARY), dateGridSelector);

    gridView.setAdapter(adapter);
    @SuppressWarnings("unchecked")
    AdapterView<MonthInYearAdapter> adapterView = (AdapterView) gridView;
    dateGridSelector.onItemClick(adapterView, null, 1, 0);

    assertCellColor(dateGridSelector, adapter, 1, DateGridSelector.selectedColor);
    assertCellColor(dateGridSelector, adapter, 5, DateGridSelector.emptyColor);
  }

  @Test
  public void testDateRangeDrawCell() {
    DateRangeGridSelector dateRangeGridSelector = new DateRangeGridSelector();
    GridView gridView = new GridView(context);
    MonthInYearAdapter adapter =
        new MonthInYearAdapter(
            context, MonthInYear.create(2016, Calendar.FEBRUARY), dateRangeGridSelector);

    gridView.setAdapter(adapter);
    // This is safe, because gridView's adapter is set to a MonthInYearAdapter
    @SuppressWarnings("unchecked")
    AdapterView<MonthInYearAdapter> adapterView = (AdapterView) gridView;
    dateRangeGridSelector.onItemClick(adapterView, null, 1, 0);
    dateRangeGridSelector.onItemClick(adapterView, null, 8, 1);

    assertCellColor(dateRangeGridSelector, adapter, 1, DateRangeGridSelector.startColor);
    assertCellColor(dateRangeGridSelector, adapter, 4, DateRangeGridSelector.rangeColor);
    assertCellColor(dateRangeGridSelector, adapter, 8, DateRangeGridSelector.endColor);
    assertCellColor(dateRangeGridSelector, adapter, 15, DateRangeGridSelector.emptyColor);
  }

  private void assertCellColor(
      GridSelector<?> gridSelector, MonthInYearAdapter adapter, int position, ColorDrawable color) {
    Calendar calendar = adapter.getItem(position);
    View view = new TextView(context);
    gridSelector.drawCell(view, calendar);
    assertEquals(color, view.getBackground());
  }
}
